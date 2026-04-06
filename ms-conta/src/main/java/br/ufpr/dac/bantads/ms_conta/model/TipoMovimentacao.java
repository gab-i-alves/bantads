package br.ufpr.dac.bantads.ms_conta.model;

// valores conforme swagger do prof
public enum TipoMovimentacao {
    DEPOSITO("depósito"),
    SAQUE("saque"),
    TRANSFERENCIA("transferência");

    private final String descricao;

    TipoMovimentacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
