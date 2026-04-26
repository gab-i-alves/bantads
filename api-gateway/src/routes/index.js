const express = require("express");

const authRoutes = require("./auth.routes");
const clienteRoutes = require("./cliente.routes");
const contaRoutes = require("./conta.routes");
const funcionarioRoutes = require("./funcionario.routes")

const router = express.Router();

router.use("/auth", authRoutes);
router.use("/cliente", clienteRoutes);
router.use("/conta", contaRoutes);
router.use("/gerentes", funcionarioRoutes)

module.exports = router;
