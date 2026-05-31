export interface ContaResumo {
  numero: string;
  clienteCpf: string;
  gerenteCpf: string;
  saldo: number;
  limite: number;
  dataCriacao?: string;
}

export interface SaldoConta {
  cliente: string;
  conta: string;
  saldo: number;
}

export interface OperacaoContaResponse {
  conta: string;
  data: string;
  saldo: number;
}

export interface TransferenciaResponse {
  conta: string;
  data: string;
  destino: string;
  saldo: number;
  valor: number;
}

export interface MovimentacaoExtrato {
  data: string;
  tipo: string;
  origem: string | null;
  destino: string | null;
  valor: number;
}

export interface SaldoDiario {
  data: string;
  saldo: number;
}

export interface Extrato {
  conta: string;
  saldo: number;
  movimentacoes: MovimentacaoExtrato[];
  saldosDiarios: SaldoDiario[];
}
