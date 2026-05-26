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
