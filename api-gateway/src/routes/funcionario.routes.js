const express = require("express");
const services = require("../config/services");
const auth = require("../middlewares/auth");
const axios = require("axios");

const router = express.Router();


router.get("/", auth, async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization }
        const funcionariosRequest = await axios.get(
            `${services.funcionario}/gerentes/`,
            { headers, timout: 3000 }
        )

        // todo: findall das contas pra fazer um map com os gerentes
        //axios.get(
        //    `${services.conta}/contas/`,
        //    { headers, timout: 3000 }
        //)

        // placeholder enquanto o endpoint de conta não existe
        const funcionarios = funcionariosRequest.data.map(
            funcionario => ({
                ...funcionario,
                clientes: []
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

module.exports = router;
