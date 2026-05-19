export interface Cliente {
  cpf: string;
  nome: string;
  cidade: string;
  uf: string;
  salario: number;
  saldo: number;
  credito: number;
  aprovado: boolean | null;
}

export interface ClienteCreate {
  cpf: string;
  nome: string;
  cidade: string;
  uf: string;
  salario: number;
}

export interface ClienteUpdate {
  nome?: string;
  cidade?: string;
  uf?: string;
  salario?: number;
  aprovado?: boolean | null;
}


export interface ClienteUpdate {
  nome?: string;
  cidade?: string;
  uf?: string;
  salario?: number;
  aprovado?: boolean | null;
}
