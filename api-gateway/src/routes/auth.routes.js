const express = require("express");
const { createProxyMiddleware } = require("http-proxy-middleware");
const services = require("../config/services");

const router = express.Router();

router.use("/", createProxyMiddleware({
    target: services.auth,
    changeOrigin: true,
    pathRewrite: {
        "^/": "/auth/"
    }
}));

module.exports = router;