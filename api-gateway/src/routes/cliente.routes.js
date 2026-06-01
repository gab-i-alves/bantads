const express = require("express");
const { createProxyMiddleware } = require("http-proxy-middleware");
const axios = require("axios");
const services = require("../config/services");
const auth = require("../middlewares/auth");
const requireRole = require("../middlewares/requireRole");

const router = express.Router();

router.post("/", createProxyMiddleware({
    target: services.cliente,
    changeOrigin: true,
    pathRewrite: () => "/clientes"
}));

router.get("/:cpf/gerente", auth, requireRole("CLIENTE"), async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization }
        const { cpf } = request.params

        const [contasRequest, gerentesRequest] = await Promise.all([
            axios.get(`${services.conta}/contas`, { headers, timeout: 3000 }),
            axios.get(`${services.funcionario}/gerentes`, { headers, timeout: 3000 }),
        ])

        const conta = contasRequest.data.find(c => c.clienteCpf === cpf)
        if (!conta) {
            return response.status(404).json({ error: "conta nao encontrada" })
        }

        const gerente = gerentesRequest.data.find(g => g.cpf === conta.gerenteCpf)

        response.json({
            cpf: conta.gerenteCpf,
            nome: gerente ? gerente.nome : null,
        })

    } catch (error) {
        if (error.response) {
            return response.status(error.response.status).json({
                message: "erro ao buscar gerente do cliente",
                data: error.response.data,
            })
        }
        next(error);
    }
});

router.use("/", auth, createProxyMiddleware({
    target: services.cliente,
    changeOrigin: true,
    pathRewrite: (path) => {
        if (path === "/" || path.startsWith("/?")) {
            return "/clientes" + path.slice(1);
        }
        return "/clientes" + path;
    },
}));

module.exports = router;
