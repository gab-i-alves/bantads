const express = require("express");
const { createProxyMiddleware } = require("http-proxy-middleware");
const services = require("../config/services");
const auth = require("../middlewares/auth");

const router = express.Router();

router.use("/", auth, createProxyMiddleware({
    target: services.cliente,
    changeOrigin: true,
    pathRewrite: {
        "^/": "/clientes/"
    }
}));

module.exports = router;