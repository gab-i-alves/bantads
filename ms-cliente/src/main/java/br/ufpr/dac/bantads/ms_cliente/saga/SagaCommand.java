package br.ufpr.dac.bantads.ms_cliente.saga;

// comando que o ms-saga envia pro ms-cliente executar um step local
// (ex APROVAR_CLIENTE durante a saga de autocadastro)
public record SagaCommand(String sagaId, String sagaType, String step, String payload) {}
