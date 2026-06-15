const express = require("express");

const authRoutes = require("./auth.routes");
const rebootRoutes = require("./reboot.routes");
const clienteRoutes = require("./cliente.routes");
const contaRoutes = require("./conta.routes");
const funcionarioRoutes = require("./funcionario.routes");

const router = express.Router();

// endpoints públicos de sessão e reset (sem prefixo)
router.use("/", authRoutes);
router.use("/", rebootRoutes);

// recursos no plural, como o contrato do testador espera
router.use("/clientes", clienteRoutes);
router.use("/contas", contaRoutes);
router.use("/gerentes", funcionarioRoutes);

module.exports = router;
