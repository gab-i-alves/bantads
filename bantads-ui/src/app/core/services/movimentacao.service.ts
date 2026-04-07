import { Injectable } from '@angular/core';
import { Movimentacao } from '../models/movimentacao.model';

@Injectable({
  providedIn: 'root',
})
export class MovimentacaoService {

  private key = 'movimentacoes';

  constructor() {
    this.initMock();
  }

  private initMock() {
    if (!localStorage.getItem(this.key)) {
      // dados pre-cadastrados conforme spec do trabalho
      // mapeamento: 1=Catharyna, 2=Cleuddônio, 3=Catianna, 4=Cutardo, 5=Coândrya
      const seed: Movimentacao[] = [
        { id: 1,  dataHora: '01/01/2020 10:00', tipo: 'DEPOSITO',       idClienteOrigem: 1, nomeClienteOrigem: 'Catharyna',  valor: 1000.00 },
        { id: 2,  dataHora: '01/01/2020 11:00', tipo: 'DEPOSITO',       idClienteOrigem: 1, nomeClienteOrigem: 'Catharyna',  valor: 900.00 },
        { id: 3,  dataHora: '01/01/2020 12:00', tipo: 'SAQUE',          idClienteOrigem: 1, nomeClienteOrigem: 'Catharyna',  valor: 550.00 },
        { id: 4,  dataHora: '01/01/2020 13:00', tipo: 'SAQUE',          idClienteOrigem: 1, nomeClienteOrigem: 'Catharyna',  valor: 350.00 },
        { id: 5,  dataHora: '10/01/2020 15:00', tipo: 'DEPOSITO',       idClienteOrigem: 1, nomeClienteOrigem: 'Catharyna',  valor: 2000.00 },
        { id: 6,  dataHora: '15/01/2020 08:00', tipo: 'SAQUE',          idClienteOrigem: 1, nomeClienteOrigem: 'Catharyna',  valor: 500.00 },
        { id: 7,  dataHora: '20/01/2020 12:00', tipo: 'TRANSFERENCIA',  idClienteOrigem: 1, nomeClienteOrigem: 'Catharyna',  idClienteDestino: 2, nomeClienteDestino: 'Cleuddônio', valor: 1700.00 },
        { id: 8,  dataHora: '01/01/2025 12:00', tipo: 'DEPOSITO',       idClienteOrigem: 2, nomeClienteOrigem: 'Cleuddônio', valor: 1000.00 },
        { id: 9,  dataHora: '02/01/2025 10:00', tipo: 'DEPOSITO',       idClienteOrigem: 2, nomeClienteOrigem: 'Cleuddônio', valor: 5000.00 },
        { id: 10, dataHora: '10/01/2025 10:00', tipo: 'SAQUE',          idClienteOrigem: 2, nomeClienteOrigem: 'Cleuddônio', valor: 200.00 },
        { id: 11, dataHora: '05/02/2025 10:00', tipo: 'DEPOSITO',       idClienteOrigem: 2, nomeClienteOrigem: 'Cleuddônio', valor: 7000.00 },
        { id: 12, dataHora: '05/05/2025 00:00', tipo: 'DEPOSITO',       idClienteOrigem: 3, nomeClienteOrigem: 'Catianna',   valor: 1000.00 },
        { id: 13, dataHora: '06/05/2025 00:00', tipo: 'SAQUE',          idClienteOrigem: 3, nomeClienteOrigem: 'Catianna',   valor: 2000.00 },
        { id: 14, dataHora: '01/06/2025 00:00', tipo: 'DEPOSITO',       idClienteOrigem: 4, nomeClienteOrigem: 'Cutardo',    valor: 150000.00 },
        { id: 15, dataHora: '01/07/2025 00:00', tipo: 'DEPOSITO',       idClienteOrigem: 5, nomeClienteOrigem: 'Coândrya',   valor: 1500.00 },
      ];

      localStorage.setItem(this.key, JSON.stringify(seed));
    }
  }

  // utils
  private getDB(): Movimentacao[] {
    return JSON.parse(localStorage.getItem(this.key) || '[]');
  }

  private saveDB(movs: Movimentacao[]) {
    localStorage.setItem(this.key, JSON.stringify(movs));
  }

  private parseData(str: string): Date {
    // formato: dd/MM/yyyy HH:mm
    const [datePart, timePart] = str.split(' ');
    const [dia, mes, ano] = datePart.split('/').map(Number);
    if (timePart) {
      const [hora, minuto] = timePart.split(':').map(Number);
      return new Date(ano, mes - 1, dia, hora, minuto);
    }
    return new Date(ano, mes - 1, dia);
  }

  // =========================

  getAll(): Movimentacao[] {
    return this.getDB();
  }

  getByClienteId(idCliente: number): Movimentacao[] {
    return this.getDB().filter(m =>
      m.idClienteOrigem === idCliente || m.idClienteDestino === idCliente
    );
  }

  getByDateRange(idCliente: number, dataInicio: Date, dataFim: Date): Movimentacao[] {
    // ajusta dataFim para fim do dia
    const fimDoDia = new Date(dataFim);
    fimDoDia.setHours(23, 59, 59, 999);

    return this.getByClienteId(idCliente).filter(m => {
      const dataMov = this.parseData(m.dataHora);
      return dataMov >= dataInicio && dataMov <= fimDoDia;
    });
  }

  addMovimentacao(mov: Omit<Movimentacao, 'id'>): Movimentacao {
    const movs = this.getDB();

    const novoId = movs.length > 0
      ? Math.max(...movs.map(m => m.id)) + 1
      : 1;

    const nova: Movimentacao = { id: novoId, ...mov };
    movs.push(nova);
    this.saveDB(movs);

    return nova;
  }

  // helper para formatar data/hora atual no padrao brasileiro
  agora(): string {
    const now = new Date();
    const dia = String(now.getDate()).padStart(2, '0');
    const mes = String(now.getMonth() + 1).padStart(2, '0');
    const ano = now.getFullYear();
    const hora = String(now.getHours()).padStart(2, '0');
    const min = String(now.getMinutes()).padStart(2, '0');
    return `${dia}/${mes}/${ano} ${hora}:${min}`;
  }
}
