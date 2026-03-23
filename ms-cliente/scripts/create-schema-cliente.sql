CREATE TABLE IF NOT EXISTS schema_cliente.endereco (
    id         BIGSERIAL    PRIMARY KEY,
    logradouro VARCHAR(150) NOT NULL,
    numero     VARCHAR(20)  NOT NULL,
    complemento VARCHAR(50),
    cep        VARCHAR(8)   NOT NULL,
    cidade     VARCHAR(100) NOT NULL,
    estado     VARCHAR(2)   NOT NULL
);

CREATE TABLE IF NOT EXISTS schema_cliente.cliente (
    id          BIGSERIAL      PRIMARY KEY,
    nome        VARCHAR(100)   NOT NULL,
    email       VARCHAR(100)   NOT NULL UNIQUE,
    cpf         VARCHAR(11)    NOT NULL UNIQUE,
    telefone    VARCHAR(20)    NOT NULL,
    salario     DECIMAL(15,2)  NOT NULL,
    status      VARCHAR(20)    NOT NULL,
    endereco_id BIGINT         NOT NULL UNIQUE,
    CONSTRAINT fk_cliente_endereco
        FOREIGN KEY (endereco_id) REFERENCES schema_cliente.endereco(id)
        ON DELETE CASCADE
);
