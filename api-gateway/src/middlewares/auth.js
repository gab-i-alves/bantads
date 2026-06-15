const jwt = require("jsonwebtoken");
const { JWT_SECRET } = require("../config/env");
const { estaRevogado } = require("./revoked");

module.exports = (req, res, next) => {
    const authHeader = req.headers.authorization;

    if (!authHeader) {
        return res.status(401).json({ error: "Token não fornecido" });
    }

    const token = authHeader.split(" ")[1];

    // token revogado no logout não vale mais, mesmo dentro da validade
    if (!token || estaRevogado(token)) {
        return res.status(401).json({ error: "Token inválido" });
    }

    try {
        const decoded = jwt.verify(token, JWT_SECRET);
        req.user = decoded;
        next();
    } catch (err) {
        return res.status(401).json({ error: "Token inválido" });
    }
};