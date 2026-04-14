interface TransactionView {
  id: number;
  operacao: string;
  description: string;
  clienteRelacionado: string | null;
  dataHora: string;
  amount: string;
  tipo: 'entrada' | 'saida';
  icon: string;
}

interface GroupedDay {
  label: string;
  date: Date;
  items: TransactionView[];
  dailyBalance: string;
}

interface ActivityItem {
  label: string;
  sublabel: string;
  amount: string;
  category: string;
  isPositive: boolean;
}

interface Movimentacao {
  id: number;
  dataHora: string;
  tipo: 'DEPOSITO' | 'SAQUE' | 'TRANSFERENCIA';
  idClienteOrigem: number;
  nomeClienteOrigem: string;
  idClienteDestino?: number;
  nomeClienteDestino?: string;
  valor: number;
}

interface Cliente {
  idCliente: number;
  nome: string;
  email: string;
  [key: string]: any;
}
export interface Endereco {
  rua: string;
  numero: string;
  bairro: string;
  cidade: string;
  estado: string;
  cep: string;
}

export interface Gerente {
  id: string;
  nome: string;
  email: string;
  cpf: string;
  telefone: string;

  cargo: string; // ex: "Gerente de TI", "Gerente Comercial"
  departamento: string;

  salario?: number;
  dataAdmissao: Date;

  ativo: boolean;

  endereco?: Endereco;

  permissoes: string[]; // ex: ["USUARIOS_READ", "USUARIOS_WRITE"]

  subordinadosIds?: string[]; // IDs dos funcionários que ele gerencia

  ultimoAcesso?: Date;
}