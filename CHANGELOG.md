# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.5.0] - 2026-04-10

### Added
- **ms-conta**: padrão CQRS com dois schemas PostgreSQL (`schema_conta_cud` e `schema_conta_read`)
- **ms-conta**: `RabbitConfig` com exchange DIRECT, fila `conta.sync` e JSON converter
- **ms-conta**: `ContaEvent` record para eventos de sincronização
- **ms-conta**: `ContaEventPublisher` publica eventos no RabbitMQ após cada operação de escrita
- **ms-conta**: `ContaEventListener` consome eventos da fila e atualiza o schema de leitura
- **ms-conta**: entidades de leitura `ContaRead` e `MovimentacaoRead` (schema_conta_read)
- **ms-conta**: repositórios de leitura `ContaReadRepository` e `MovimentacaoReadRepository`

### Changed
- **ms-conta**: `ContaService` separado em leitura (schema read) e escrita (schema cud + publish)
- **ms-conta**: `DataSeeder` popula ambos os schemas (CUD e Read) no seed e no reboot
- **ms-conta**: entidades `Conta` e `Movimentacao` com `schema = "schema_conta_cud"` explícito
- **ms-conta**: `application.yaml` com `default_schema: schema_conta_cud`
- **init-postgres.sql**: `schema_conta` substituído por `schema_conta_cud` + `schema_conta_read`

---

## [0.4.0] - 2026-04-06

### Added
- **bantads-ui**: telas do gerente (dashboard, consultar clientes, consultar cliente, top 3)
- **bantads-ui**: telas do admin (dashboard, relatório de clientes, gerenciar gerentes)
- **bantads-ui**: mock funcional para autocadastro e edição de perfil
- **bantads-ui**: services com localStorage para clientes, gerentes, admins e autenticação
- **bantads-ui**: `MovimentacaoService` e `Movimentacao` model com seed de 15 registros da spec
- **bantads-ui**: filtro por data início/fim e saldo consolidado por dia no extrato (R8)
- **bantads-ui**: registro automático de movimentações em depósito, saque e transferência
- **bantads-ui**: arquivos base para auth guard, interceptor, models e money pipe

### Changed
- **bantads-ui**: navegação funcional no dashboard do gerente (R9, R12, R13)
- **bantads-ui**: `consult-all-clients` com filtro de pendentes e link para detalhe via CPF

---

## [0.3.1] - 2026-04-03

### Added
- **ms-cliente**: endpoint `GET /reboot` para reset dos dados de clientes

---

## [0.3.0] - 2026-04-03

### Added
- **ms-conta**: microsserviço completo com 6 endpoints REST
  - `GET /contas/{numero}/saldo` — consulta saldo (R3)
  - `POST /contas/{numero}/depositar` — depósito (R5)
  - `POST /contas/{numero}/sacar` — saque com validação de limite (R6)
  - `POST /contas/{numero}/transferir` — transferência entre contas (R7)
  - `GET /contas/{numero}/extrato` — extrato com filtro de data opcional (R8)
  - `GET /reboot` — reset do banco pro estado inicial da spec
- **ms-conta**: seed com 5 contas e 15 movimentações pré-cadastradas (seção 4 spec)
- **ms-conta**: Dockerfile multi-stage (JDK build → JRE runtime)
- **ms-funcionario**: microsserviço de gerentes/funcionários com CRUD (Mafe)
- **start.sh**: script de build e execução com banner ASCII, steps visuais e tratamento de erros
- **init-postgres.sql**: schemas para cliente, conta e gerente

### Changed
- **compose.yaml**: adicionado ms-conta (porta 8083) e ms-funcionario
- **compose.yaml**: RabbitMQ trocado para imagem `management-alpine` (UI na porta 15672)
- **pom.xml**: adicionados módulos ms-conta e ms-funcionario

### Fixed
- RabbitMQ: variáveis de ambiente corrigidas (`RABBITMQ_DEFAULT_USER` em vez de `RABBIT_DEFAULT_USER`)
- ms-conta: bloqueio de transferência para a mesma conta

---

## [0.2.0] - 2026-03-30

### Added
- **ms-auth**: endpoint `POST /auth/login` com JWT (Auth0 + BCrypt)
- **ms-auth**: endpoint `GET /reboot` para reset de dados de teste
- **ms-auth**: 9 contas seed (5 clientes, 3 gerentes, 1 admin)
- **ms-auth**: Dockerfile multi-stage
- **ms-gerente**: projeto scaffolded (Spring Initializr)
- **bantads-ui**: telas de cliente (dashboard, depósito, saque, transferência, extrato)
- **bantads-ui**: tela de login com toggle cadastro/login
- **compose.yaml**: adicionado ms-auth (porta 8082)

### Changed
- **compose.yaml**: renomeado docker-compose.yml
- **env.example**: renomeado e atualizado

---

## [0.1.0] - 2026-03-21

### Added
- **ms-cliente**: microsserviço CRUD completo
  - Autocadastro com verificação de CPF duplicado (R1)
  - Listagem de clientes aprovados (R12)
  - Listagem de pendentes para aprovação (R9)
  - Consulta por CPF (R13)
  - Alteração de perfil (R4)
  - Aprovação e rejeição de clientes (R10, R11)
- **ms-cliente**: seed com 5 clientes pré-cadastrados
- **ms-cliente**: Dockerfile multi-stage
- **ms-auth**: projeto scaffolded (Spring Initializr)
- **bantads-ui**: projeto Angular 21 inicial
- **Docker Compose**: PostgreSQL 17, MongoDB 7, RabbitMQ 4.2
- **init-postgres.sql**: criação do banco e schema_cliente
- **README.md**: documentação inicial com diagrama de arquitetura

---

## [0.0.1] - 2026-02-21

### Added
- Initial commit
- Estrutura do repositório
