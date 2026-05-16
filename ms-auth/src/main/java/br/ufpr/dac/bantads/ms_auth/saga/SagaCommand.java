package br.ufpr.dac.bantads.ms_auth.saga;

// comando que o ms-saga envia pro ms-auth executar um step local
public record SagaCommand(String sagaId, String sagaType, String step, String payload) {}
