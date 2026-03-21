SELECT 'CREATE DATABASE bantads'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'bantads')\gexec

\c bantads

CREATE SCHEMA IF NOT EXISTS schema_cliente;