package br.dac.bantads.ms_saga.saga;

// resposta que os MSs participantes devolvem ao ms-saga apos cada step
// success=false aciona compensacao
public record SagaReply(String sagaId, String step, boolean success, String payload, String error) {}
