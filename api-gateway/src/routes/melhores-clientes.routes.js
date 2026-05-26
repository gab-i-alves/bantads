const express = require("express");
const services = require("../config/services");
const auth = require("../middlewares/auth");
const requireRole = require("../middlewares/requireRole");
const axios = require("axios");

const router = express.Router();

router.get("/", auth, requireRole("GERENTE", "ADMINISTRADOR"), async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization }

        const [contasRequest, clientesRequest] = await Promise.all([
            axios.get(`${services.conta}/contas`, { headers, timeout: 3000 }),
            axios.get(`${services.cliente}/clientes`, { headers, timeout: 3000 }),
        ])

        // R14: os melhores clientes são globais, independente do gerente da conta.
        const top3Contas = [...contasRequest.data]
            .sort((a, b) => Number(b.saldo) - Number(a.saldo))
            .slice(0, 3)

        const clientesPorCpf = clientesRequest.data.reduce((acc, cliente) => {
            acc[cliente.cpf] = cliente
            return acc
        }, {})

        const melhoresClientes = top3Contas.map(conta => {
            const cliente = clientesPorCpf[conta.clienteCpf] || {}
            const endereco = cliente.endereco || {}
            return {
                cpf: cliente.cpf || conta.clienteCpf,
                nome: cliente.nome || null,
                salario: cliente.salario || 0,
                cidade: endereco.cidade || null,
                estado: endereco.estado || null,
                gerenteCpf: conta.gerenteCpf,
                saldo: conta.saldo,
                limite: conta.limite,
            }
        })

        response.json(melhoresClientes)

    } catch (error) {
        if (error.response) {
            let statusCode = error.response.status
            return response.status(statusCode).json({
                "message": "erro ao buscar melhores clientes",
                "data": error.response.data
            })
        }
        next(error);
    }
})

module.exports = router;
