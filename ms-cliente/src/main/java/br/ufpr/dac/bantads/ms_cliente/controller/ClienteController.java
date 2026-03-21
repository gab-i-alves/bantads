package br.ufpr.dac.bantads.ms_cliente.controller;

import br.ufpr.dac.bantads.ms_cliente.dto.*;
import br.ufpr.dac.bantads.ms_cliente.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    // R1: autocadastro - cliente se cadastra sem login, status começa como PENDENTE
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> criar(@Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO response = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // R12: listar todos os clientes (aprovados)
    // R9: filtro=para_aprovar - lista pendentes pro gerente aprovar
    // TODO R14: filtro=melhores_clientes - pendente integracao com ms-conta
    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) String filtro) {
        if ("para_aprovar".equals(filtro)) {
            return ResponseEntity.ok(service.listarPendentes());
        }
        if ("melhores_clientes".equals(filtro)) {
            return ResponseEntity.ok(Map.of("message", "necessario integrar com ms-conta ainda"));
        }
        return ResponseEntity.ok(service.listarTodos());
    }

    // R13: consultar cliente por cpf
    @GetMapping("/{cpf}")
    public ResponseEntity<ClienteResponseDTO> buscarPorCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(service.buscarPorCpf(cpf));
    }

    // TODO R4: alterar perfil - tudo menos cpf. pendente: recalcular limite no ms-conta
    @PutMapping("/{cpf}")
    public ResponseEntity<ClienteResponseDTO> atualizar(
            @PathVariable String cpf,
            @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(cpf, dto));
    }

    // TODO R10: gerente aprova cliente - pendente: saga cria conta no ms-conta via rabbitmq
    @PostMapping("/{cpf}/aprovar")
    public ResponseEntity<ClienteResponseDTO> aprovar(@PathVariable String cpf) {
        return ResponseEntity.ok(service.aprovar(cpf));
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
