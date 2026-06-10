const express = require("express");
const services = require("../config/services");
const auth = require("../middlewares/auth");
const requireRole = require("../middlewares/requireRole");
const axios = require("axios");

const router = express.Router();

router.post("/", auth, requireRole("GERENTE", "ADMINISTRADOR"), express.json(), async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization }
        const { gerenteCpf } = request.body

        if (!gerenteCpf) {
            return response.status(400).json({ error: "gerenteCpf obrigatorio" })
        }

        // busca em paralelo as 3 fontes de dados
        const [contasRequest, clientesRequest, gerentesRequest] = await Promise.all([
            axios.get(`${services.conta}/contas`, { headers, timeout: 3000 }),
            axios.get(`${services.cliente}/clientes`, { headers, timeout: 3000 }),
            axios.get(`${services.funcionario}/gerentes`, { headers, timeout: 3000 }),
        ])

        // indexa por cpf pra lookup O(1) ao montar cada linha
        const clientesPorCpf = new Map(
            clientesRequest.data.map(c => [c.cpf, c])
        )
        const gerentesPorCpf = new Map(
            gerentesRequest.data.map(g => [g.cpf, g])
        )

        // monta uma linha por conta com dados do cliente, conta e gerente
        const clientes = contasRequest.data.map(conta => {
            const cliente = clientesPorCpf.get(conta.clienteCpf) || {}
            const gerente = gerentesPorCpf.get(conta.gerenteCpf) || {}
            const endereco = cliente.endereco || {}

            return {
                cpf: cliente.cpf || conta.clienteCpf,
                nome: cliente.nome || "",
                email: cliente.email || "",
                telefone: cliente.telefone || "",
                status: cliente.status || "",
                salario: cliente.salario || 0,
                cidade: endereco.cidade || "",
                uf: endereco.estado || "",
                endereco,
                numeroConta: conta.numero,
                gerenteCpf: conta.gerenteCpf,
                gerenteNome: gerente.nome || "",
                saldo: conta.saldo,
                limite: conta.limite,
                credito: conta.limite,
            }
        })

        const clientesPorGerente = clientes.filter(cliente => cliente.gerenteCpf === gerenteCpf)

        response.json(clientesPorGerente)

    } catch (error) {
        if (error.response) {
            let statusCode = error.response.status
            return response.status(statusCode).json({
                "message": "erro ao buscar clientes do gerente",
                "data": error.response.data
            })
        }
        next(error);
    }
})

module.exports = router;
