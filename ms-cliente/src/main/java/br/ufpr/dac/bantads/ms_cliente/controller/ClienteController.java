package br.ufpr.dac.bantads.ms_cliente.controller;

import br.ufpr.dac.bantads.ms_cliente.dto.*;
import br.ufpr.dac.bantads.ms_cliente.saga.SagaPublisher;
import br.ufpr.dac.bantads.ms_cliente.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;
    private final SagaPublisher sagaPublisher;

    // R1: autocadastro - cliente se cadastra sem login, status começa como PENDENTE
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> criar(@Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO response = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) String busca) {
        if ("para_aprovar".equals(filtro)) {
            return ResponseEntity.ok(service.listarPendentes());
        }
        return ResponseEntity.ok(service.listarTodos(busca));
    }

    // R13: consultar cliente por cpf
    @GetMapping("/{cpf}")
    public ResponseEntity<ClienteResponseDTO> buscarPorCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(service.buscarPorCpf(cpf));
    }

    // R4: alterar perfil. Atualiza dados síncrono e dispara saga pra recalcular
    // limite no ms-conta (regra: limite >= |saldo negativo| se for o caso).
    @PutMapping("/{cpf}")
    public ResponseEntity<ClienteResponseDTO> atualizar(
            @PathVariable String cpf,
            @Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO response = service.atualizar(cpf, dto);
        sagaPublisher.dispararAlteracaoPerfil(cpf, dto);
        return ResponseEntity.ok(response);
    }

    // R10: gerente aprova cliente. Status muda síncrono (UX imediata) e a saga
    // cuida do resto: cria auth, cria conta vinculada ao gerente menos cheio,
    // envia senha por e-mail (mock). Em caso de falha, compensa via REVERTER_APROVACAO.
    @PostMapping("/{cpf}/aprovar")
    public ResponseEntity<ClienteResponseDTO> aprovar(@PathVariable String cpf) {
        ClienteResponseDTO response = service.aprovar(cpf);
        sagaPublisher.dispararAutocadastro(cpf);
        return ResponseEntity.ok(response);
    }

    // R11: gerente rejeita cliente - motivo recebido mas nao persistido aqui
    // TODO pendente: enviar motivo por email via rabbitmq
    @PostMapping("/{cpf}/rejeitar")
    public ResponseEntity<ClienteResponseDTO> rejeitar(
            @PathVariable String cpf,
            @RequestBody RejeitarRequestDTO dto) {
        return ResponseEntity.ok(service.rejeitar(cpf, dto));
    }

    // --- exceptions ---

    @ExceptionHandler(ClienteService.ClienteNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ClienteService.ClienteNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ClienteService.ClienteJaCadastradoException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ClienteService.ClienteJaCadastradoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}
