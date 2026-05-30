const express = require("express");
const services = require("../config/services");
const auth = require("../middlewares/auth");
const requireRole = require("../middlewares/requireRole");
const axios = require("axios");

const router = express.Router();


router.get("/", auth, requireRole("ADMINISTRADOR"), async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization }

        const [funcionariosRequest, contasRequest] = await Promise.all([
            axios.get(`${services.funcionario}/gerentes`, { headers, timeout: 3000 }),
            axios.get(`${services.conta}/contas`, { headers, timeout: 3000 }),
        ])

        // agrupa contas por gerenteCpf pra anexar a cada gerente a lista
        // de clientes que ele atende
        const contasPorGerente = contasRequest.data.reduce((acc, conta) => {
            const cpf = conta.gerenteCpf
            if (!acc[cpf]) acc[cpf] = []
            acc[cpf].push(conta)
            return acc
        }, {})

        const funcionarios = funcionariosRequest.data.map(
            funcionario => ({
                ...funcionario,
                clientes: contasPorGerente[funcionario.cpf] || []
            })
        )

        response.json(funcionarios)

    } catch (error) {
        if (error.response) {
            let statusCode = error.response.status
            return response.status(statusCode).json({
                "message": "erro ao buscar gerentes",
                "data": error.response.data
            })
        }
        next(error);
    }

})

router.post("/", auth, requireRole("ADMINISTRADOR"), express.json(), async (request, response, next) => {
    try {
        const headers = {
            Authorization: request.headers.authorization,
            "Content-Type": "application/json",
        }

        const funcionarioRequest = await axios.post(
            `${services.funcionario}/gerentes`,
            request.body,
            { headers, timeout: 3000 }
        )

        response.status(funcionarioRequest.status).json(funcionarioRequest.data)
    } catch (error) {
        if (error.response) {
            let statusCode = error.response.status
            return response.status(statusCode).json({
                "message": "erro ao criar gerente",
                "data": error.response.data
            })
        }
        next(error);
    }
})

router.put("/:cpf", auth, requireRole("ADMINISTRADOR"), express.json(), async (request, response, next) => {
    try {
        const headers = {
            Authorization: request.headers.authorization,
            "Content-Type": "application/json",
        }

        const funcionarioRequest = await axios.put(
            `${services.funcionario}/gerentes/${request.params.cpf}`,
            request.body,
            { headers, timeout: 3000 }
        )

        response.status(funcionarioRequest.status).json(funcionarioRequest.data)
    } catch (error) {
        if (error.response) {
            let statusCode = error.response.status
            return response.status(statusCode).json({
                "message": "erro ao atualizar gerente",
                "data": error.response.data
            })
        }
        next(error);
    }
})

router.delete("/:cpf", auth, requireRole("ADMINISTRADOR"), async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization }

        const funcionarioRequest = await axios.delete(
            `${services.funcionario}/gerentes/${request.params.cpf}`,
            { headers, timeout: 3000 }
        )

        response.status(funcionarioRequest.status).json(funcionarioRequest.data)
    } catch (error) {
        if (error.response) {
            let statusCode = error.response.status
            return response.status(statusCode).json({
                "message": "erro ao remover gerente",
                "data": error.response.data
            })
        }
        next(error);
    }
})

module.exports = router;
