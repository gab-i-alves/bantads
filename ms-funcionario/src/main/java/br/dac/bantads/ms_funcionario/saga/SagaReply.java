package br.dac.bantads.ms_funcionario.saga;

// resposta que o ms-funcionario devolve pro ms-saga após executar (ou recusar) um step
public record SagaReply(String sagaId, String step, boolean success, String payload, String error) {}
