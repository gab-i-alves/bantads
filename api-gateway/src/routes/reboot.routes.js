const express = require("express");
const axios = require("axios");
const services = require("../config/services");

const router = express.Router();

// GET /reboot: reseta a base de cada microsserviço. É público (o testador chama
// antes de qualquer login). Usa allSettled pra disparar os 4 em paralelo e só
// falha se algum reboot não responder 2xx.
router.get("/reboot", async (request, response, next) => {
    const alvos = [services.auth, services.cliente, services.conta, services.funcionario];

    const resultados = await Promise.allSettled(
        alvos.map((base) => axios.get(`${base}/reboot`, { timeout: 15000 }))
    );

    const falhas = resultados.filter((r) => r.status === "rejected");
    if (falhas.length) {
        return response.status(502).json({
            error: "falha ao resetar um ou mais serviços",
            falhas: falhas.length,
        });
    }

    response.json({ status: "ok" });
});

module.exports = router;
