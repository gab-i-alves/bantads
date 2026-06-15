const express = require("express");
const axios = require("axios");
const services = require("../config/services");
const auth = require("../middlewares/auth");

const router = express.Router();

// O testador manda o endereço como string e CEP/cidade/estado soltos; o
// ms-cliente espera endereco aninhado. Esta função adapta o corpo do
// autocadastro e da alteração de perfil pro contrato do ms-cliente.
function adaptarCliente(body) {
    const e = typeof body.endereco === "object" && body.endereco ? body.endereco : {};
    return {
        nome: body.nome,
        email: body.email,
        cpf: body.cpf,
        telefone: body.telefone,
        salario: body.salario,
        endereco: {
            logradouro: typeof body.endereco === "string" ? body.endereco : (e.logradouro ?? ""),
            numero: body.numero ?? e.numero ?? "S/N",
            complemento: body.complemento ?? e.complemento ?? null,
            cep: String(body.CEP ?? body.cep ?? e.cep ?? "").replace(/\D/g, ""),
            cidade: body.cidade ?? e.cidade ?? "",
            estado: body.estado ?? e.estado ?? "",
        },
    };
}

// junta os dados de conta (numero, saldo, limite, gerente) ao cliente
function comporClienteConta(cliente, contaPorCpf) {
    const conta = contaPorCpf.get(cliente.cpf);
    if (!conta) return cliente;
    return {
        ...cliente,
        conta: conta.numero,
        saldo: conta.saldo,
        limite: conta.limite,
        gerente: conta.gerenteCpf,
    };
}

async function buscarContaPorCpf(headers) {
    const { data } = await axios.get(`${services.conta}/contas`, { headers, timeout: 3000 });
    return new Map(data.map((c) => [c.clienteCpf, c]));
}

// Tolerância a falhas na API Composition: um serviço de ENRIQUECIMENTO fora do ar
// não pode derrubar a resposta inteira. Resolve a promise ou devolve o fallback,
// permitindo resposta parcial (ex: relatorio sem nome do gerente em vez de 500).
async function opcional(promise, fallback) {
    try {
        return await promise;
    } catch (e) {
        console.warn("composition: fonte de enriquecimento indisponivel, seguindo parcial:", e.message);
        return fallback;
    }
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

// A criação/recálculo da conta acontece de forma assíncrona via saga, mas o
// testador consulta o cliente logo após aprovar/alterar. Estas esperas curtas
// fazem o gateway só responder quando a saga já refletiu, evitando a corrida.
async function esperarConta(cpf, headers, tentativas = 30) {
    for (let i = 0; i < tentativas; i++) {
        const conta = (await buscarContaPorCpf(headers)).get(cpf);
        if (conta) return conta;
        await sleep(500);
    }
    return null;
}

async function esperarLimiteMudar(cpf, headers, limiteAntigo, tentativas = 20) {
    for (let i = 0; i < tentativas; i++) {
        const conta = (await buscarContaPorCpf(headers)).get(cpf);
        if (conta && String(conta.limite) !== String(limiteAntigo)) return;
        await sleep(500);
    }
}

// R1: autocadastro, sem login. Retorna 201 {cpf, email}; cpf duplicado -> 409.
router.post("/", express.json(), async (request, response, next) => {
    try {
        const clienteRequest = await axios.post(
            `${services.cliente}/clientes`,
            adaptarCliente(request.body),
            { timeout: 3000 }
        );
        const r = clienteRequest.data;
        response.status(201).json({ cpf: r.cpf, email: r.email });
    } catch (error) {
        if (error.response) {
            return response.status(error.response.status).json(error.response.data);
        }
        next(error);
    }
});

// GET /clientes roteado por ?filtro=:
//  para_aprovar (gerente/adm), melhores_clientes (gerente/adm),
//  adm_relatorio_clientes (só adm), sem filtro -> clientes do gerente logado.
router.get("/", auth, async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization };
        const { role, cpf } = request.user;
        const filtro = request.query.filtro;

        if (filtro === "adm_relatorio_clientes") {
            if (role !== "ADMINISTRADOR") {
                return response.status(403).json({ error: "acesso negado" });
            }
            return response.json(await relatorioClientes(headers));
        }

        if (filtro === "melhores_clientes") {
            if (role !== "GERENTE" && role !== "ADMINISTRADOR") {
                return response.status(403).json({ error: "acesso negado" });
            }
            return response.json(await melhoresClientes(headers));
        }

        if (filtro === "para_aprovar") {
            if (role !== "GERENTE" && role !== "ADMINISTRADOR") {
                return response.status(403).json({ error: "acesso negado" });
            }
            const { data } = await axios.get(`${services.cliente}/clientes`, {
                headers, params: { filtro: "para_aprovar" }, timeout: 3000,
            });
            return response.json(data);
        }

        // sem filtro: tela de clientes do gerente (R12) — só os atendidos por ele.
        // cliente é a âncora; conta é enriquecimento (saldo/limite + escopo do
        // gerente) com fallback, pra não derrubar a tela se ms-conta oscilar.
        if (role !== "GERENTE") {
            return response.status(403).json({ error: "acesso negado" });
        }
        const clientesRequest = await axios.get(`${services.cliente}/clientes`, { headers, timeout: 3000 });
        const contaPorCpf = await opcional(buscarContaPorCpf(headers), new Map());
        const meusClientes = clientesRequest.data
            .filter((c) => contaPorCpf.get(c.cpf)?.gerenteCpf === cpf)
            .map((c) => comporClienteConta(c, contaPorCpf))
            .sort((a, b) => String(a.nome).localeCompare(String(b.nome), "pt-BR", { sensitivity: "base" }));
        response.json(meusClientes);
    } catch (error) {
        if (error.response) {
            return response.status(error.response.status).json(error.response.data);
        }
        next(error);
    }
});

// R13: consulta um cliente com os dados de conta agregados. Cliente rejeitado
// não é mais consultável (404).
router.get("/:cpf", auth, async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization };
        const clienteRequest = await axios.get(
            `${services.cliente}/clientes/${request.params.cpf}`, { headers, timeout: 3000 });
        const cliente = clienteRequest.data;

        if (cliente.status === "REJEITADO") {
            return response.status(404).json({ error: "cliente nao encontrado" });
        }

        const contaPorCpf = await buscarContaPorCpf(headers);
        response.json(comporClienteConta(cliente, contaPorCpf));
    } catch (error) {
        if (error.response) {
            return response.status(error.response.status).json(error.response.data);
        }
        next(error);
    }
});

// R4: alteração de perfil. Repassa o corpo adaptado; o ms-cliente já dispara a
// saga de recálculo de limite.
router.put("/:cpf", auth, express.json(), async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization, "Content-Type": "application/json" };
        const cpf = request.params.cpf;
        const limiteAntigo = (await buscarContaPorCpf(headers)).get(cpf)?.limite;
        const clienteRequest = await axios.put(
            `${services.cliente}/clientes/${cpf}`,
            adaptarCliente(request.body),
            { headers, timeout: 3000 }
        );
        // espera a saga recalcular o limite antes de devolver (R4)
        if (limiteAntigo !== undefined) {
            await esperarLimiteMudar(cpf, headers, limiteAntigo);
        }
        response.status(clienteRequest.status).json(clienteRequest.data);
    } catch (error) {
        if (error.response) {
            return response.status(error.response.status).json(error.response.data);
        }
        next(error);
    }
});

// R10: aprovação (só gerente). R11: rejeição (só gerente).
router.post("/:cpf/aprovar", auth, express.json(), proxiarOperacaoGerente("aprovar", true));
router.post("/:cpf/rejeitar", auth, express.json(), proxiarOperacaoGerente("rejeitar", false));

function proxiarOperacaoGerente(acao, esperarCriacaoConta) {
    return async (request, response, next) => {
        if (request.user.role !== "GERENTE") {
            return response.status(403).json({ error: "acesso negado" });
        }
        try {
            const headers = { Authorization: request.headers.authorization, "Content-Type": "application/json" };
            const cpf = request.params.cpf;
            const r = await axios.post(
                `${services.cliente}/clientes/${cpf}/${acao}`,
                request.body ?? {},
                { headers, timeout: 3000 }
            );
            // a saga cria a conta vinculada de forma assíncrona; só responde
            // quando ela existe, pra consulta seguinte do testador já vê-la (R10)
            if (esperarCriacaoConta) {
                await esperarConta(cpf, headers);
            }
            response.status(r.status).json(r.data);
        } catch (error) {
            if (error.response) {
                return response.status(error.response.status).json(error.response.data);
            }
            next(error);
        }
    };
}

// R14: 3 melhores clientes do banco, por saldo (global, independe do gerente).
// conta é a âncora (define o ranking); cliente é enriquecimento (nome/cidade):
// se ms-cliente cair, devolve o ranking com nome/cidade nulos em vez de 500.
async function melhoresClientes(headers) {
    const contasRequest = await axios.get(`${services.conta}/contas`, { headers, timeout: 3000 });
    const clientesData = await opcional(
        axios.get(`${services.cliente}/clientes`, { headers, timeout: 3000 }).then((r) => r.data), []);
    const clientesPorCpf = new Map(clientesData.map((c) => [c.cpf, c]));
    return [...contasRequest.data]
        .sort((a, b) => Number(b.saldo) - Number(a.saldo))
        .slice(0, 3)
        .map((conta) => {
            const cliente = clientesPorCpf.get(conta.clienteCpf) || {};
            const endereco = cliente.endereco || {};
            return {
                cpf: cliente.cpf || conta.clienteCpf,
                nome: cliente.nome || null,
                salario: cliente.salario || 0,
                cidade: endereco.cidade || null,
                estado: endereco.estado || null,
                gerenteCpf: conta.gerenteCpf,
                saldo: conta.saldo,
                limite: conta.limite,
            };
        });
}

// R16: relatório de clientes do admin, uma linha por conta, ordenado por nome.
// conta é a âncora (uma linha por conta); cliente e gerente são enriquecimento,
// com fallback pra resposta parcial se uma das fontes estiver fora.
async function relatorioClientes(headers) {
    const contasRequest = await axios.get(`${services.conta}/contas`, { headers, timeout: 3000 });
    const [clientesData, gerentesData] = await Promise.all([
        opcional(axios.get(`${services.cliente}/clientes`, { headers, timeout: 3000 }).then((r) => r.data), []),
        opcional(axios.get(`${services.funcionario}/gerentes`, { headers, timeout: 3000 }).then((r) => r.data), []),
    ]);
    const clientesPorCpf = new Map(clientesData.map((c) => [c.cpf, c]));
    const gerentesPorCpf = new Map(gerentesData.map((g) => [g.cpf, g]));
    return contasRequest.data
        .map((conta) => {
            const cliente = clientesPorCpf.get(conta.clienteCpf) || {};
            const gerente = gerentesPorCpf.get(conta.gerenteCpf) || {};
            return {
                cpf: cliente.cpf || "",
                nome: cliente.nome || "",
                email: cliente.email || "",
                salario: cliente.salario ?? "",
                cidade: cliente.endereco?.cidade || "",
                estado: cliente.endereco?.estado || "",
                numeroConta: conta.numero,
                saldo: conta.saldo,
                limite: conta.limite,
                cpfGerente: gerente.cpf || "",
                nomeGerente: gerente.nome || "",
            };
        })
        .sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR", { sensitivity: "base" }));
}

module.exports = router;
