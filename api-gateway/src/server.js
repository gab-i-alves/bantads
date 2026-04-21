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
app.use(express.json());
app.use(routes);

app.listen(PORT, () => {
    console.log(`Gateway rodando na porta ${PORT}`);
});