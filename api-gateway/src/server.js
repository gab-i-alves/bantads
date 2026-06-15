const express = require("express");
const cors = require("cors");
const helmet = require("helmet");
const morgan = require("morgan");
const routes = require("./routes");
const { PORT } = require("./config/env");

const app = express();

app.use(helmet());
app.use(cors());
app.use(morgan("combined"));
app.use(routes);

// erro não tratado vira JSON 500, em vez do HTML padrão do express
app.use((err, req, res, next) => {
    console.error("erro nao tratado no gateway:", err.message);
    if (res.headersSent) {
        return next(err);
    }
    res.status(500).json({ error: "erro interno no gateway" });
});

app.listen(PORT, () => {
    console.log(`Gateway rodando na porta ${PORT}`);
});