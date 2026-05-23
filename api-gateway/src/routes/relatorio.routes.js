const express = require("express");
const services = require("../config/services");
const auth = require("../middlewares/auth");
const requireRole = require("../middlewares/requireRole");
const axios = require("axios");

const router = express.Router();


router.get("/", auth, requireRole("ADMINISTRADOR"), async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization }

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

        // monta uma linha por conta; campos faltantes viram "" pra não quebrar a defesa
        const relatorio = contasRequest.data.map(conta => {
            const cliente = clientesPorCpf.get(conta.clienteCpf) || {}
            const gerente = gerentesPorCpf.get(conta.gerenteCpf) || {}
            return {
                cpf: cliente.cpf || "",
                nome: cliente.nome || "",
                email: cliente.email || "",
                salario: cliente.salario ?? "",
                numeroConta: conta.numero,
                saldo: conta.saldo,
                limite: conta.limite,
                cpfGerente: gerente.cpf || "",
                nomeGerente: gerente.nome || "",
            }
        })

        // ordena por nome do cliente ASC, case-insensitive, pt-BR
        relatorio.sort((a, b) =>
            a.nome.localeCompare(b.nome, "pt-BR", { sensitivity: "base" })
        )

        response.json(relatorio)

    } catch (error) {
        if (error.response) {
            let statusCode = error.response.status
            return response.status(statusCode).json({
                "message": "erro ao gerar relatorio",
                "data": error.response.data
            })
        }
        next(error);
    }

})

module.exports = router;
