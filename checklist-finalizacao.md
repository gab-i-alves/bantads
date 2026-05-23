# BANTADS — Checklist de finalização

## 1 — Desbloqueio integração front ↔ back

- [x] Gateway: condicionar `app.use(express.json())` em `api-gateway/src/server.js:13` — já comentado em commit anterior, POST agora passa pelo proxy
- [x] Frontend: registrar `provideHttpClient` em `bantads-ui/src/app/app.config.ts` — feito (usa `HTTP_INTERCEPTORS` clássico + `withInterceptorsFromDi()`)
- [x] Frontend: preencher `core/interceptors/req.interceptor.ts` — injeta `Authorization: Bearer <token>` quando há token em `localStorage`
- [x] Frontend: preencher `core/guards/auth.guard.ts` — `CanActivateFn` com mapping de rota → role (`/client`→CLIENTE, `/admin`→ADMINISTRADOR, `/manager`→GERENTE)
- [x] Frontend: trocar URLs hardcoded em `auth.service.ts` por `http://localhost:3000` (gateway) — feito; `cliente.service.ts` ainda em `localhost:8081` (pendente)
- [x] Frontend: corrigir mapping de roles em `auth.service.ts` — `CLIENTE`/`GERENTE`/`ADMINISTRADOR` agora coerentes com `auth.guard.ts`
- [ ] Frontend: criar `ContaService`, `FuncionarioService` e `MovimentacaoService` espelhando o padrão de `cliente.service.ts` — pendente; só existem `auth.service.ts` e `cliente.service.ts`

---

## 2 — Orquestrador SAGA (`ms-saga`)

- [x] `ms-saga`: criar `RabbitConfig` (exchange `saga.exchange` + fila `saga.reply.orchestrator`) e listener da fila de replies
- [x] `ms-saga`: state machine simples — `SagaState` in-memory via `ConcurrentHashMap` (suficiente pro escopo; produção pediria tabela Postgres)
- [x] **SAGA Autocadastro (R1 + R10) E2E**:
  - R1 fica síncrono (POST `/clientes` cria PENDENTE)
  - R10 publica `saga.start.autocadastro` → `APROVAR_CLIENTE` → `CONSULTAR_GERENTE_MENOS_CONTAS` → `BUSCAR_DADOS_GERENTE` → `CRIAR_AUTH_CLIENTE` (senha aleatória) → `CRIAR_CONTA` → log mock e-mail
  - Compensação: `REMOVER_CONTA` → `REMOVER_AUTH_CLIENTE` → `REVERTER_APROVACAO` (ordem reversa dos steps já completados)
- [x] Adicionar listener `saga.cmd.conta` no ms-conta — `CRIAR_CONTA`, `REMOVER_CONTA`, `RECALCULAR_LIMITE`, `REATRIBUIR_TODAS_CONTAS` adicionados ao `SagaCommandListener`
- [x] Plugar step real `CRIAR_AUTH_CLIENTE` no `SagaCommandListener` do ms-auth — gera senha aleatória (8 chars), BCrypt, retorna `senhaTemporaria` no reply pra orquestrador montar mock de e-mail
- [x] Plugar step real `BUSCAR_DADOS_GERENTE` no ms-funcionario — retorna nome+email do gerente escolhido pelo ms-conta
- [x] Remover `// TODO pendente: saga deve criar conta...` do `ClienteService.aprovar()` (R10 agora dispara saga via `SagaPublisher` no controller)

---

## 3 — Demais SAGAs

- [x] **SAGA Alteração de Perfil (R4)**: `saga.start.alteracao_perfil` → `ATUALIZAR_CLIENTE` (ms-cliente) → `RECALCULAR_LIMITE` (ms-conta, regra `limite >= |saldo|` se saldo < 0)
- [x] **SAGA Inserção de Gerente (R17)**: já existente — `CONSULTAR_GERENTE_MAIS_CONTAS` → `CRIAR_AUTH_GERENTE` → `REATRIBUIR_CONTA`
- [x] **SAGA Remoção de Gerente (R18)**: `saga.start.remocao_gerente` → `VERIFICAR_ULTIMO_GERENTE` (bloqueia se ≤1) → `CONSULTAR_GERENTE_MENOS_CONTAS` (com `excluirCpf` do alvo) → `REATRIBUIR_TODAS_CONTAS` → `REMOVER_GERENTE` → `REMOVER_AUTH_GERENTE`

---

## 4 — API Composition (R14, R15, R16)

Implementadas no api-gateway (padrão Promise.all + agregação, igual ao `funcionario.routes.js`).

- [x] **R14 — 3 melhores clientes**: `GET /melhores-clientes?gerenteCpf=...` (ou via email do token). Top 3 saldos das contas do gerente + dados de cliente
- [x] **R15 — Dashboard admin**: `GET /admin/dashboard`. Pra cada gerente: nº clientes, soma saldos positivos, soma negativos. Ordenado por positivos desc. Admin-only (403 senão)
- [x] **R16 — Relatório completo**: `GET /admin/relatorio`. Lista cpf/nome/email/salário/nº conta/saldo/limite/cpf gerente/nome gerente. Ordenado por nome ASC pt-BR. Admin-only

---

## 5 — Frontend ligado aos services

- [ ] Telas **Cliente** (R3, R5, R6, R7, R8): dashboard com saldo (vermelho se negativo) · depósito · saque · transferência · extrato (com saldo consolidado por dia, vermelho saída / azul entrada)
- [ ] Telas **Gerente** (R9, R12, R13): aprovar/rejeitar com motivo · listar todos · consultar por CPF
- [ ] Telas **Admin** (R15, R16, R17, R18, R19, R20): dashboard · relatório · CRUD gerentes
- [ ] Validações + máscaras pt-BR — CPF, telefone, CEP, moeda `R$ X,XX`, datas `dd/mm/yyyy`. Angular Forms + Angular Material já estão no `package.json`

---

## 6 — Entrega

- [ ] Envio de e-mail mock — R1 falha, R10 senha pós-aprovação, R11 motivo rejeição
- [ ] Decisão SHA256+SALT vs BCrypt — confirmar com o prof antes da defesa
- [ ] Testar end-to-end com a app `test_dac` (https://github.com/razeranthom/test_dac), é o que o prof roda na hora
- [ ] Gravar vídeo ≤ 20 min mostrando cada requisito numerado
- [ ] Empacotar .zip ≤ 50 MB sem `node_modules/`, `target/`, `.git/`