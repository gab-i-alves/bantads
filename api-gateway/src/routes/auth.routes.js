const express = require("express");
const axios = require("axios");
const services = require("../config/services");
const auth = require("../middlewares/auth");
const { revogar } = require("../middlewares/revoked");

const router = express.Router();

// POST /login: o testador manda {login, senha}; o ms-auth espera {email, password}.
// A resposta do ms-auth (access_token, token_type, tipo, usuario{cpf,email}) é
// repassada como veio.
router.post("/login", express.json(), async (request, response, next) => {
    try {
        const { login, senha } = request.body;
        const authRequest = await axios.post(
            `${services.auth}/auth/login`,
            { email: login, password: senha },
            { timeout: 3000 }
        );
        response.status(authRequest.status).json(authRequest.data);
    } catch (error) {
        if (error.response) {
            return response.status(error.response.status).json(error.response.data);
        }
        next(error);
    }
});

// POST /logout: stateless do lado do ms-auth, então o gateway revoga o token
// atual (enunciado exige que ele pare de valer) e devolve o e-mail do dono.
router.post("/logout", auth, (request, response) => {
    const token = request.headers.authorization.split(" ")[1];
    revogar(token);
    response.json({ email: request.user.email });
});

module.exports = router;
