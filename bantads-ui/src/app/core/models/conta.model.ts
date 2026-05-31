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

export interface TransferenciaResponse {
  conta: string;
  data: string;
  destino: string;
  saldo: number;
  valor: number;
}
