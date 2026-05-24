const express = require("express");
const { createProxyMiddleware } = require("http-proxy-middleware");
const services = require("../config/services");
const auth = require("../middlewares/auth");

const router = express.Router();

router.post("/", createProxyMiddleware({
    target: services.cliente,
    changeOrigin: true,
    pathRewrite: () => "/clientes"
}));

router.use("/", auth, createProxyMiddleware({
    target: services.cliente,
    changeOrigin: true,
    pathRewrite: (path) => {
        if (path === "/" || path.startsWith("/?")) {
            return "/clientes" + path.slice(1);
        }
        return "/clientes" + path;
    },
}));

module.exports = router;
