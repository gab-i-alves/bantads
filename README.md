# BANTADS - Internet Banking do TADS

Sistema de Internet Banking desenvolvido para a disciplina DS152 - Desenvolvimento de Aplicações Corporativas (UFPR - TADS).

O BANTADS é um sistema bancário com três perfis de acesso (Cliente, Gerente e Administrador), construído sobre uma arquitetura de microsserviços com comunicação assíncrona via mensageria.

---

## Sumário

- [Arquitetura](#arquitetura)
- [Equipe](#equipe)

---

## Arquitetura

O sistema segue uma arquitetura de microsserviços com os seguintes componentes:

```
                                    ┌─────────────────┐
                                    │    RabbitMQ      │
                                    │  (mensageria)    │
                                    └────────┬────────┘
                                             │
┌──────────┐    HTTP     ┌──────────────┐    │    ┌──────────────┐
│          │────────────>│              │────────>│  MS Cliente   │──> PostgreSQL (schema_cliente)
│ Frontend │             │  API Gateway │────────>│  MS Conta     │──> PostgreSQL (schema_conta_cud / schema_conta_read)
│ (SPA)    │<────────────│  (Node.js)   │────────>│  MS Gerente   │──> PostgreSQL (schema_gerente)
│          │             │              │────────>│  MS Auth      │──> MongoDB (db_auth)
└──────────┘             └──────────────┘    │    └──────────────┘
                                             │
                                    ┌────────┴────────┐
                                    │ SAGA Orquestrador│
                                    │  (coordenação)   │
                                    └─────────────────┘
```

O frontend se comunica **exclusivamente** com o API Gateway via HTTP-REST. Os microsserviços se comunicam entre si via RabbitMQ. Transações distribuídas são coordenadas pelo orquestrador de SAGAs.

---

## Equipe

Trabalho desenvolvido para a disciplina DS152 - DAC, UFPR - TADS, 2026/1.

| Membro | Responsabilidade |
|---|---|
| Thiago | Frontend (Angular + TypeScript) + API Gateway |
| Mafe | MS Auth + MS Gerente + DevOps |
| Gabi | MS Cliente + MS Conta (CQRS) + SAGA Orquestrador |
