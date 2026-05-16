package br.ufpr.dac.bantads.ms_auth.saga;

// resposta que o ms-auth devolve pro ms-saga após executar (ou recusar) um step
public record SagaReply(String sagaId, String step, boolean success, String payload, String error) {}
