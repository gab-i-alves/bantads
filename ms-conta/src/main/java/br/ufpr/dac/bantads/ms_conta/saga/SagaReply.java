package br.ufpr.dac.bantads.ms_conta.saga;

public record SagaReply(String sagaId, String step, boolean success, String payload, String error) {}
