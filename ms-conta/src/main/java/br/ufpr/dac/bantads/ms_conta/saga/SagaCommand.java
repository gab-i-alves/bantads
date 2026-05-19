package br.ufpr.dac.bantads.ms_conta.saga;

public record SagaCommand(String sagaId, String sagaType, String step, String payload) {}
