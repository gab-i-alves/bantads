const express = require("express");
const axios = require("axios");
const services = require("../config/services");
const auth = require("../middlewares/auth");
const requireRole = require("../middlewares/requireRole");

const router = express.Router();

// resolve a promise ou devolve o fallback, pra uma fonte de enriquecimento fora
// do ar não derrubar a resposta inteira (mesmo padrão do cliente.routes).
async function opcional(promise, fallback) {
    try {
        return await promise;
    } catch (e) {
        console.warn("composition: fonte de enriquecimento indisponivel, seguindo parcial:", e.message);
        return fallback;
    }
}

// Dashboard do admin (R15): uma linha por gerente com o número de clientes que
// ele atende e a soma dos saldos positivos e negativos das contas. Net-new, fora
// do contrato do testador. gerente é a âncora (uma linha por gerente); conta é
// enriquecimento com fallback pra lista vazia se o ms-conta oscilar.
router.get("/dashboard", auth, requireRole("ADMINISTRADOR"), async (request, response, next) => {
    try {
        const headers = { Authorization: request.headers.authorization };
        const [gerentes, contas] = await Promise.all([
            axios.get(`${services.funcionario}/gerentes`, { headers, timeout: 3000 }).then((r) => r.data),
            opcional(axios.get(`${services.conta}/contas`, { headers, timeout: 3000 }).then((r) => r.data), []),
        ]);

        const porGerente = new Map(
            gerentes.map((g) => [g.cpf, {
                cpf: g.cpf,
                nome: g.nome,
                email: g.email ?? "",
                clientes: 0,
                saldosPositivos: 0,
                saldosNegativos: 0,
            }]));

        for (const conta of contas) {
            const linha = porGerente.get(conta.gerenteCpf);
            if (!linha) continue;
            linha.clientes += 1;
            const saldo = Number(conta.saldo) || 0;
            if (saldo >= 0) {
                linha.saldosPositivos += saldo;
            } else {
                linha.saldosNegativos += saldo;
            }
        }

        const resultado = [...porGerente.values()]
            .sort((a, b) => b.saldosPositivos - a.saldosPositivos);
        response.json(resultado);
    } catch (error) {
        if (error.response) {
            return response.status(error.response.status).json(error.response.data);
        }
        next(error);
    }
});

module.exports = router;
