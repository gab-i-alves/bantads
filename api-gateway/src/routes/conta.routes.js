const express = require("express");
const { createProxyMiddleware } = require("http-proxy-middleware");
const services = require("../config/services");
const auth = require("../middlewares/auth");

const router = express.Router();

router.use("/", auth, createProxyMiddleware({
    target: services.conta,
    changeOrigin: true,
    pathRewrite: (path) => {
        if (path === "/" || path.startsWith("/?")) {
            return "/contas" + path.slice(1);
        }
        return "/contas" + path;
    },
}));

module.exports = router;