package br.ufpr.dac.bantads.ms_cliente.saga;

// resposta que o ms-cliente devolve pro ms-saga apos executar (ou recusar) um step
public record SagaReply(String sagaId, String step, boolean success, String payload, String error) {}
