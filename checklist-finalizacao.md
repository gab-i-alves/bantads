# BANTADS — Checklist de finalização

## 1 — Desbloqueio integração front ↔ back

- [ ] Gateway: condicionar `app.use(express.json())` em `api-gateway/src/server.js:13` (hoje quebra o proxy com HTTP 408 em todo POST)
- [ ] Frontend: registrar `provideHttpClient(withInterceptors([reqInterceptor]))` em `bantads-ui/src/app/app.config.ts` (sem isso o `inject(HttpClient)` quebra em runtime)
- [ ] Frontend: preencher `core/interceptors/req.interceptor.ts` (arquivo vazio) injetando `Authorization: Bearer <token>` quando houver token
- [ ] Frontend: preencher `core/guards/auth.guard.ts` (arquivo vazio) com `CanActivate` baseado em `AuthService.isAuthenticated()`
- [ ] Frontend: trocar URLs hardcoded (`localhost:8082` em `auth.service.ts`, `localhost:8081` em `cliente.service.ts`) por `http://localhost:3000` (gateway)
- [ ] Frontend: corrigir mapping de roles em `auth.service.ts:32-35` (`USER`/`ADMIN` → `CLIENTE`/`GERENTE`/`ADMINISTRADOR`)
- [ ] Frontend: criar `ContaService`, `FuncionarioService` e `MovimentacaoService` espelhando o padrão de `cliente.service.ts`

---

## 2 — Orquestrador SAGA (`ms-saga`)

- [ ] `ms-saga`: criar `RabbitConfig` (exchange `saga.exchange` + fila `saga.reply.orchestrator`) e listener da fila de replies
- [ ] `ms-saga`: state machine simples — pode ser tabela Postgres `saga_log(sagaId, type, currentStep, status, payload)` ou cache in-memory (basta funcionar)
- [ ] **SAGA Autocadastro (R1 + R10) E2E**:
  - publish `CRIAR_CLIENTE` em `saga.cmd.cliente`
  - on reply ok → `CONSULTAR_GERENTE_MENOS_CLIENTES` em `saga.cmd.funcionario`
  - on reply ok → `CRIAR_AUTH_CLIENTE` em `saga.cmd.auth`
  - on reply ok → `CRIAR_CONTA` em `saga.cmd.conta` (precisa fila nova, item abaixo)
  - on qualquer falha → compensação (delete cliente, delete auth, etc.)
- [ ] Adicionar listener `saga.cmd.conta` no ms-conta (hoje só tem RabbitConfig do CQRS interno)
- [ ] Plugar step real `CRIAR_AUTH_CLIENTE` no `SagaCommandListener` do ms-auth (gerar senha aleatória, persistir hash, retornar email + senha pra orquestrador disparar e-mail)
- [ ] Plugar step real `CONSULTAR_GERENTE_MENOS_CLIENTES` no `SagaCommandListener` do ms-funcionario (count via consulta no banco do conta — provavelmente via composição HTTP)
- [ ] Remover `// TODO pendente: saga deve criar conta...` do `ClienteService.aprovar()` (depois que R10 estiver fluindo via SAGA)

---

## 3 — Demais SAGAs

- [ ] **SAGA Alteração de Perfil (R4)**: Cliente atualiza → Conta recalcula limite. Regra dura: se novo limite < saldo negativo, limite = saldo negativo
- [ ] **SAGA Inserção de Gerente (R17)**: consulta gerente com **mais** contas → insere novo gerente → reatribui 1 conta
- [ ] **SAGA Remoção de Gerente (R18)**: bloquear se for o último gerente → consulta gerente com **menos** contas → reatribui todas as contas → remove

---

## 4 — API Composition (R14, R15, R16)

- [ ] **R14 — 3 melhores clientes**: junta `top-3 saldos` (ms-conta) + dados (ms-cliente). Hoje existe stub em `ClienteController.java:34` sem corpo real
- [ ] **R15 — Dashboard admin**: para cada gerente devolver `nº de clientes, soma saldos positivos, soma saldos negativos`, ordenado por positivos desc
- [ ] **R16 — Relatório completo**: lista de `cpf, nome, email, salário, nº conta, saldo, limite, cpf gerente, nome gerente`, ordenado por nome do cliente

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