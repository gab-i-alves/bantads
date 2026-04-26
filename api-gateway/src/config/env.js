require("dotenv").config();

module.exports = {
    PORT: process.env.PORT || 3000,
    JWT_SECRET: process.env.JWT_SECRET,

    SERVICES: {
        AUTH: process.env.MS_AUTH_URL,
        CLIENTE: process.env.MS_CLIENTE_URL,
        CONTA: process.env.MS_CONTA_URL,
        FUNCIONARIO: process.env.MS_FUNCIONARIO_URL
    }
};
