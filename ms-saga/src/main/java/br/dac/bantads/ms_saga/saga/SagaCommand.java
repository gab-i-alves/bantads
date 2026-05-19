package br.dac.bantads.ms_saga.saga;

public record SagaCommand(String sagaId, String sagaType, String step, String payload) {}
