export interface Movimentacao {
  id: number;
  dataHora: string;
  tipo: 'DEPOSITO' | 'SAQUE' | 'TRANSFERENCIA';
  idClienteOrigem: number;
  nomeClienteOrigem: string;
  idClienteDestino?: number;
  nomeClienteDestino?: string;
  valor: number;
}
