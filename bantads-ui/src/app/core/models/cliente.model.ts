export interface Cliente {
  id?: number;
  cpf: string;
  nome: string;
  email?: string;
  telefone?: string;
  salario: number;
  status?: string;
  cidade?: string;
  uf?: string;
  conta?: string;
  saldo?: number;
  limite?: number;
  aprovado?: boolean | null;
  endereco?: {
    logradouro: string;
    numero: string;
    complemento?: string | null;
    cep: string;
    cidade: string;
    estado: string;
  };
}

export interface ClienteCreate {
  cpf: string;
  nome: string;
  email: string;
  telefone: string;
  salario: number;
  endereco: {
    logradouro: string;
    numero: string;
    complemento?: string | null;
    cep: string;
    cidade: string;
    estado: string;
  };
}

export interface ClienteUpdate {
  cpf: string;
  nome: string;
  email: string;
  telefone: string;
  salario: number;
  endereco: {
    logradouro: string;
    numero: string;
    complemento?: string | null;
    cep: string;
    cidade: string;
    estado: string;
  };
}

export interface MelhorCliente {
  cpf: string;
  nome: string | null;
  salario: number;
  cidade: string | null;
  estado: string | null;
  gerenteCpf: string;
  saldo: number;
  limite: number;
}
