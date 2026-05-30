export interface DashboardGerente {
  cpf: string;
  nome: string;
  email: string;
  clientes: number;
  saldosPositivos: number;
  saldosNegativos: number;
}

export interface RelatorioCliente {
  cpf: string;
  nome: string;
  email: string;
  salario: number | string | null;
  cidade?: string;
  estado?: string;
  numeroConta: string;
  saldo: number;
  limite: number;
  cpfGerente: string;
  nomeGerente: string;
}
