const express = require("express");
const services = require("../config/services");
const auth = require("../middlewares/auth");
const axios = require("axios");

const router = express.Router();

// R14: gerente quer ver os 3 clientes dele com maior saldo na conta
router.get("/", auth, async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization }

        // 1. Determinar gerenteCpf: query param tem prioridade; senão, lookup por email do token
        let gerenteCpf = request.query.gerenteCpf

        if (!gerenteCpf) {
            const email = request.user && request.user.email
            if (!email) {
                return response.status(400).json({
                    message: "gerenteCpf não informado e email do token ausente"
                })
            }

            const gerentesRequest = await axios.get(
                `${services.funcionario}/gerentes`,
                { headers, timeout: 3000 }
            )
            const gerente = gerentesRequest.data.find(g => g.email === email)
            if (!gerente) {
                return response.status(404).json({
                    message: "gerente não encontrado para o email do token",
                    data: { email }
                })
            }
            gerenteCpf = gerente.cpf
        }

        // 2. Buscar contas e clientes em paralelo (uma chamada só pra clientes, filtra em memória)
        const [contasRequest, clientesRequest] = await Promise.all([
            axios.get(`${services.conta}/contas`, { headers, timeout: 3000 }),
            axios.get(`${services.cliente}/clientes`, { headers, timeout: 3000 }),
        ])

        // 3. Filtrar contas do gerente, ordenar por saldo desc e pegar top 3
        const top3Contas = contasRequest.data
            .filter(conta => conta.gerenteCpf === gerenteCpf)
            .sort((a, b) => b.saldo - a.saldo)
            .slice(0, 3)

        // 4. Index de clientes por cpf pra lookup O(1)
        const clientesPorCpf = clientesRequest.data.reduce((acc, cliente) => {
            acc[cliente.cpf] = cliente
            return acc
        }, {})

        // 5. Montar payload final
        const melhoresClientes = top3Contas.map(conta => {
            const cliente = clientesPorCpf[conta.clienteCpf] || {}
            return {
                cpf: cliente.cpf || conta.clienteCpf,
                nome: cliente.nome || null,
                email: cliente.email || null,
                salario: cliente.salario || null,
                saldo: conta.saldo,
                limite: conta.limite,
                numeroConta: conta.numero
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
