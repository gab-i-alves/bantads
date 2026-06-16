const express = require("express");

const authRoutes = require("./auth.routes");
const rebootRoutes = require("./reboot.routes");
const clienteRoutes = require("./cliente.routes");
const contaRoutes = require("./conta.routes");
const funcionarioRoutes = require("./funcionario.routes");
const adminRoutes = require("./admin.routes");

const router = express.Router();

// endpoints públicos de sessão e reset (sem prefixo)
router.use("/", authRoutes);
router.use("/", rebootRoutes);

// recursos no plural, como o contrato do testador espera
router.use("/clientes", clienteRoutes);
router.use("/contas", contaRoutes);
router.use("/gerentes", funcionarioRoutes);

// agregações da UI fora do contrato do testador (ex: dashboard do admin)
router.use("/admin", adminRoutes);

module.exports = router;
