const cors = require("cors");

const corsOptions = {
    origin: [
        "http://localhost:4200", // frontend local
        "http://localhost:3000"  // gateway
    ],
    methods: ["GET", "POST", "PUT", "DELETE"],
    allowedHeaders: ["Content-Type", "Authorization"],
    credentials: true
};

module.exports = cors(corsOptions);