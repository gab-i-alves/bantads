const express = require("express");
const { createProxyMiddleware } = require("http-proxy-middleware");
const axios = require("axios");
const services = require("../config/services");
const auth = require("../middlewares/auth");

const router = express.Router();

router.get("/:numero/extrato", auth, async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization };
        const { numero } = request.params;

        const extratoRequest = await axios.get(
            `${services.conta}/contas/${numero}/extrato`,
            { headers, params: request.query, timeout: 3000 });
        const extrato = extratoRequest.data;

        const transferencias = (extrato.movimentacoes ?? []).filter(m => m.origem && m.destino);

        if (transferencias.length) {
            const [contasRequest, clientesRequest] = await Promise.all([
                axios.get(`${services.conta}/contas`, { headers, timeout: 3000 }),
                axios.get(`${services.cliente}/clientes`, { headers, timeout: 3000 }),
            ]);

            const cpfPorConta = new Map(contasRequest.data.map(c => [c.numero, c.clienteCpf]));
            const nomePorCpf = new Map(clientesRequest.data.map(c => [c.cpf, c.nome]));
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
}));

module.exports = router;