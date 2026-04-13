SELECT 'CREATE DATABASE bantads'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'bantads')\gexec

\c bantads

CREATE SCHEMA IF NOT EXISTS schema_cliente;
CREATE SCHEMA IF NOT EXISTS schema_conta_cud;
CREATE SCHEMA IF NOT EXISTS schema_conta_read;
CREATE SCHEMA IF NOT EXISTS schema_gerente;
CREATE SCHEMA IF NOT EXISTS schema_funcionario;