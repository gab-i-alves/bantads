const express = require("express");

const authRoutes = require("./auth.routes");
const clienteRoutes = require("./cliente.routes");
const contaRoutes = require("./conta.routes");
const funcionarioRoutes = require("./funcionario.routes")
const melhoresClientesRoutes = require("./melhores-clientes.routes")
const dashboardAdminRoutes = require("./dashboard-admin.routes")
const relatorioRoutes = require("./relatorio.routes")

const router = express.Router();

router.use("/auth", authRoutes);
router.use("/cliente", clienteRoutes);
router.use("/conta", contaRoutes);
router.use("/gerentes", funcionarioRoutes)

// API Composition (R14, R15, R16) agrega dados de 2+ MSs no gateway
router.use("/melhores-clientes", melhoresClientesRoutes)
router.use("/admin/dashboard", dashboardAdminRoutes)
router.use("/admin/relatorio", relatorioRoutes)

module.exports = router;
