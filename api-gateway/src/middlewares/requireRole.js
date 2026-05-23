module.exports = (...rolesPermitidos) => (req, res, next) => {
    const role = req.user && req.user.role;
    if (!role) {
        return res.status(401).json({ error: "Token sem role" });
    }
    if (!rolesPermitidos.includes(role)) {
        return res.status(403).json({
            error: "Acesso negado",
            required: rolesPermitidos,
            actual: role,
        });
    }
    next();
};
