const express = require("express");
const axios = require("axios");
const services = require("../config/services");
const auth = require("../middlewares/auth");
const requireRole = require("../middlewares/requireRole");

const router = express.Router();

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

// tolerância a falhas na composição: fonte de enriquecimento indisponível devolve
// o fallback em vez de derrubar a resposta inteira (resposta parcial)
async function opcional(promise, fallback) {
    try {
        return await promise;
    } catch (e) {
        console.warn("composition: fonte de enriquecimento indisponivel, seguindo parcial:", e.message);
        return fallback;
    }
}

// espera a saga R17 reatribuir uma conta ao gerente recém-criado
async function esperarGerenteComConta(cpf, headers, tentativas = 30) {
    for (let i = 0; i < tentativas; i++) {
        const { data } = await axios.get(`${services.conta}/contas`, { headers, timeout: 3000 });
        if (data.some((c) => c.gerenteCpf === cpf)) return;
        await sleep(500);
    }
}

// o ms-funcionario expõe o campo como `role`; o contrato do testador usa `tipo`
function comTipo(funcionario) {
    return {
        cpf: funcionario.cpf,
        nome: funcionario.nome,
        email: funcionario.email,
        telefone: funcionario.telefone,
        tipo: funcionario.role,
    };
}

// GET /gerentes: com ?filtro=dashboard monta o agregado do admin (R15);
// sem filtro lista os gerentes (R19).
router.get("/", auth, requireRole("ADMINISTRADOR"), async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization };

        if (request.query.filtro === "dashboard") {
            // gerente é a âncora do dashboard; conta é enriquecimento (listas e
            // somas). Se ms-conta cair, mostra os gerentes com listas vazias e
            // saldos zerados em vez de derrubar a tela toda (resposta parcial).
            const gerentesRequest = await axios.get(`${services.funcionario}/gerentes`, { headers, timeout: 3000 });
            const contas = await opcional(
                axios.get(`${services.conta}/contas`, { headers, timeout: 3000 }).then((r) => r.data), []);
            const gerentes = gerentesRequest.data.filter((g) => g.role === "GERENTE");

            const dashboard = gerentes.map((gerente) => {
                const contasDoGerente = contas.filter((c) => c.gerenteCpf === gerente.cpf);
                const saldoPositivo = contasDoGerente.reduce(
                    (s, c) => (Number(c.saldo) >= 0 ? s + Number(c.saldo) : s), 0);
                const saldoNegativo = contasDoGerente.reduce(
                    (s, c) => (Number(c.saldo) < 0 ? s + Number(c.saldo) : s), 0);
                return {
                    gerente: { cpf: gerente.cpf, nome: gerente.nome },
                    clientes: contasDoGerente,
                    saldo_positivo: saldoPositivo,
                    saldo_negativo: saldoNegativo,
                };
            });
            // R15: ordenado pela soma dos saldos positivos, decrescente
            dashboard.sort((a, b) => b.saldo_positivo - a.saldo_positivo);
            return response.json(dashboard);
        }

        const { data } = await axios.get(`${services.funcionario}/gerentes`, { headers, timeout: 3000 });
        // R19: lista de gerentes ordenada por nome (ASC), independente da ordem
        // que o ms-funcionario devolve.
        const gerentes = data
            .filter((g) => g.role === "GERENTE")
            .map(comTipo)
            .sort((a, b) => String(a.nome).localeCompare(String(b.nome), "pt-BR", { sensitivity: "base" }));
        response.json(gerentes);
    } catch (error) {
        if (error.response) {
            return response.status(error.response.status).json({
                message: "erro ao buscar gerentes", data: error.response.data,
            });
        }
        next(error);
    }
});

router.get("/:cpf", auth, requireRole("ADMINISTRADOR"), async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization };
        const { data } = await axios.get(
            `${services.funcionario}/gerentes/${request.params.cpf}`, { headers, timeout: 3000 });
        response.json(comTipo(data));
    } catch (error) {
        if (error.response) {
            return response.status(error.response.status).json(error.response.data);
        }
        next(error);
    }
});

// R17: o corpo do testador usa `tipo`; o ms-funcionario espera `role`.
router.post("/", auth, requireRole("ADMINISTRADOR"), express.json(), async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization, "Content-Type": "application/json" };
        const body = { ...request.body, role: request.body.tipo ?? request.body.role };
        const r = await axios.post(`${services.funcionario}/gerentes`, body, { headers, timeout: 3000 });
        // a saga R17 cria o auth do gerente e reatribui 1 conta a ele de forma
        // assíncrona; só responde quando a conta já apareceu, pra consulta seguinte
        // (dashboard) e o login do novo gerente já enxergarem o efeito.
        await esperarGerenteComConta(r.data.cpf, headers);
        response.status(201).json(comTipo(r.data));
    } catch (error) {
        if (error.response) {
            return response.status(error.response.status).json(error.response.data);
        }
        next(error);
    }
});

// R20: atualiza nome/email/senha; o tipo (role) não muda.
router.put("/:cpf", auth, requireRole("ADMINISTRADOR"), express.json(), async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization, "Content-Type": "application/json" };
        const r = await axios.put(
            `${services.funcionario}/gerentes/${request.params.cpf}`, request.body, { headers, timeout: 3000 });
        response.status(200).json(comTipo(r.data));
    } catch (error) {
        if (error.response) {
            return response.status(error.response.status).json(error.response.data);
        }
        next(error);
    }
});

// R18: remoção assíncrona via saga. O ms-funcionario aceita o pedido (202);
// o testador espera 200 e depois consulta até o gerente sumir.
router.delete("/:cpf", auth, requireRole("ADMINISTRADOR"), async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization };
        const r = await axios.delete(
            `${services.funcionario}/gerentes/${request.params.cpf}`, { headers, timeout: 3000 });
        response.status(200).json(r.data);
    } catch (error) {
        if (error.response) {
            return response.status(error.response.status).json(error.response.data);
        }
        next(error);
    }
});

module.exports = router;
