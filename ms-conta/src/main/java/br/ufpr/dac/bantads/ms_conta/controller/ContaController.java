package br.ufpr.dac.bantads.ms_conta.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.ufpr.dac.bantads.ms_conta.dto.ExtratoResponseDTO;
import br.ufpr.dac.bantads.ms_conta.dto.OperacaoRequestDTO;
import br.ufpr.dac.bantads.ms_conta.dto.OperacaoResponseDTO;
import br.ufpr.dac.bantads.ms_conta.dto.SaldoResponseDTO;
import br.ufpr.dac.bantads.ms_conta.dto.TransferenciaRequestDTO;
import br.ufpr.dac.bantads.ms_conta.dto.TransferenciaResponseDTO;
import br.ufpr.dac.bantads.ms_conta.service.ContaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    // R3: saldo pra tela inicial do cliente
    @GetMapping("/{numero}/saldo")
    public ResponseEntity<SaldoResponseDTO> saldo(@PathVariable String numero) {
        try {
            return ResponseEntity.ok(contaService.consultarSaldo(numero));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // R8: extrato com filtro opcional de data (inicio e fim)
    // ex: GET /contas/1291/extrato?inicio=2020-01-01&fim=2020-12-31
    @GetMapping("/{numero}/extrato")
    public ResponseEntity<ExtratoResponseDTO> extrato(
            @PathVariable String numero,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        try {
            return ResponseEntity.ok(contaService.consultarExtrato(numero, inicio, fim));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // R5: deposito
    @PostMapping("/{numero}/depositar")
    public ResponseEntity<OperacaoResponseDTO> depositar(
            @PathVariable String numero,
            @Valid @RequestBody OperacaoRequestDTO request) {
        try {
            return ResponseEntity.ok(contaService.depositar(numero, request.valor()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // R6: saque
    @PostMapping("/{numero}/sacar")
    public ResponseEntity<OperacaoResponseDTO> sacar(
            @PathVariable String numero,
            @Valid @RequestBody OperacaoRequestDTO request) {
        try {
            return ResponseEntity.ok(contaService.sacar(numero, request.valor()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // R7: transferencia
    @PostMapping("/{numero}/transferir")
    public ResponseEntity<TransferenciaResponseDTO> transferir(
            @PathVariable String numero,
            @Valid @RequestBody TransferenciaRequestDTO request) {
        try {
            return ResponseEntity.ok(
                    contaService.transferir(numero, request.destino(), request.valor()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
