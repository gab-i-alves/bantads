package br.dac.bantads.ms_funcionario.saga;

// comando que o ms-saga envia pro ms-funcionario executar um step local
public record SagaCommand(String sagaId, String sagaType, String step, String payload) {}
