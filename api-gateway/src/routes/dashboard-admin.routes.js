const express = require("express");
const services = require("../config/services");
const auth = require("../middlewares/auth");
const requireRole = require("../middlewares/requireRole");
const axios = require("axios");

const router = express.Router();


router.get("/", auth, requireRole("ADMINISTRADOR"), async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization }

        // busca gerentes e contas em paralelo pra montar o agregado
        const [gerentesRequest, contasRequest] = await Promise.all([
            axios.get(`${services.funcionario}/gerentes`, { headers, timeout: 3000 }),
            axios.get(`${services.conta}/contas`, { headers, timeout: 3000 }),
        ])

        // o endpoint /gerentes/ devolve ADMINISTRADOR junto — filtra só GERENTE
        const gerentes = gerentesRequest.data.filter(g => g.role === "GERENTE")
        const contas = contasRequest.data

        // pra cada gerente, agrega clientes e somas de saldos das contas dele
        const dashboard = gerentes.map(gerente => {
            const contasDoGerente = contas.filter(c => c.gerenteCpf === gerente.cpf)

            const saldosPositivos = contasDoGerente.reduce(
                (soma, conta) => conta.saldo >= 0 ? soma + conta.saldo : soma,
                0
            )
            const saldosNegativos = contasDoGerente.reduce(
                (soma, conta) => conta.saldo < 0 ? soma + conta.saldo : soma,
                0
            )

            return {
                cpf: gerente.cpf,
                nome: gerente.nome,
                email: gerente.email,
                clientes: contasDoGerente.length,
                saldosPositivos,
                saldosNegativos,
            }
        })

        // R15: ordenado por soma de saldos positivos DESC
        dashboard.sort((a, b) => b.saldosPositivos - a.saldosPositivos)

        response.json(dashboard)

    } catch (error) {
        if (error.response) {
            let statusCode = error.response.status
            return response.status(statusCode).json({
                "message": "erro ao montar dashboard do admin",
                "data": error.response.data
            })
        }
        next(error);
    }

})

module.exports = router;
