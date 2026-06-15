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

export interface Gerente {
  cpf: string;
  nome: string;
  email: string;
  telefone?: string;
  role: string;
  clientes?: unknown[];
}

export interface GerenteCreate {
  cpf: string;
  nome: string;
  email: string;
  telefone?: string;
  senha: string;
  role?: string;
}

export interface GerenteUpdate {
  nome: string;
  email: string;
  telefone?: string;
  // Senha opcional: so e enviada quando o admin troca a senha do gerente (R20).
  senha?: string;
}
