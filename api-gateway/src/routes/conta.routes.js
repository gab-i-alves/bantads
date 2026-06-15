const express = require("express");
const { createProxyMiddleware } = require("http-proxy-middleware");
const axios = require("axios");
const services = require("../config/services");
const auth = require("../middlewares/auth");

const router = express.Router();

// repassa cpf e papel do token pro ms-conta checar dono da conta (R5/R6/R7)
function headersUsuario(request) {
    return {
        Authorization: request.headers.authorization,
        "x-user-cpf": request.user?.cpf ?? "",
        "x-user-role": request.user?.role ?? "",
    };
}

router.get("/:numero/extrato", auth, async (request, response, next) => {
    try {
        const headers = headersUsuario(request);
        const { numero } = request.params;

        const extratoRequest = await axios.get(
            `${services.conta}/contas/${numero}/extrato`,
            { headers, params: request.query, timeout: 3000 });
        const extrato = extratoRequest.data;

        const transferencias = (extrato.movimentacoes ?? []).filter(m => m.origem && m.destino);

        if (transferencias.length) {
            // os nomes origem/destino são enriquecimento: se conta ou cliente
            // oscilar, o extrato volta sem os nomes em vez de falhar inteiro.
            const opcional = async (p, fb) => { try { return await p; } catch { return fb; } };
            const [contasData, clientesData] = await Promise.all([
                opcional(axios.get(`${services.conta}/contas`, { headers, timeout: 3000 }).then(r => r.data), []),
                opcional(axios.get(`${services.cliente}/clientes`, { headers, timeout: 3000 }).then(r => r.data), []),
            ]);

            const cpfPorConta = new Map(contasData.map(c => [c.numero, c.clienteCpf]));
            const nomePorCpf = new Map(clientesData.map(c => [c.cpf, c.nome]));
            const nomeConta = (numero) => nomePorCpf.get(cpfPorConta.get(numero)) ?? null;

            for (const m of transferencias) {
                m.nomeOrigem = nomeConta(m.origem);
                m.nomeDestino = nomeConta(m.destino);
            }
        }

        response.json(extrato);
    } catch (error) {
        if (error.response) {
            return response.status(error.response.status).json({
                message: "erro ao buscar extrato",
                data: error.response.data,
            });
        }
        next(error);
    }
});

router.use("/", auth, createProxyMiddleware({
    target: services.conta,
    changeOrigin: true,
    pathRewrite: (path) => {
        if (path === "/" || path.startsWith("/?")) {
            return "/contas" + path.slice(1);
        }
        return "/contas" + path;
    },
    on: {
        // injeta cpf/papel do token pro ms-conta autorizar o dono (R5/R6/R7)
        proxyReq: (proxyReq, request) => {
            proxyReq.setHeader("x-user-cpf", request.user?.cpf ?? "");
            proxyReq.setHeader("x-user-role", request.user?.role ?? "");
        },
    },
}));

module.exports = router;