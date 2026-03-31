import { Component } from '@angular/core';
import { Header } from '../../../../shared/components/header/header';

@Component({
  selector: 'app-transaction-history',
  imports: [Header],
  templateUrl: './transaction-history.html',
  styleUrl: './transaction-history.css',
})



export class TransactionHistory {

  transactions: any[] = [
    {
      id: 1,
      operacao: 'SAQUE',
      description: 'Saque Terminal 24h',
      clienteRelacionado: null,
      dataHora: '30/03/2026 14:20',
      amount: '249,90',
      tipo: 'saida',
      icon: 'fa-solid fa-money-bill-1-wave',
      date: new Date('2026-03-30')
    },
    {
      id: 2,
      operacao: 'TRANSFERENCIA',
      description: 'PIX Recebido',
      clienteRelacionado: 'Lucas Silva',
      dataHora: '30/03/2026 09:12',
      amount: '1.200,00',
      tipo: 'entrada',
      icon: 'fa-solid fa-arrow-down-long',
      date: new Date('2026-03-30')
    },
    {
      id: 3,
      operacao: 'TRANSFERENCIA',
      description: 'Pagamento Aluguel',
      clienteRelacionado: 'Imobiliária Central',
      dataHora: '29/03/2026 20:45',
      amount: '86,40',
      tipo: 'saida',
      icon: 'fa-solid fa-arrow-up-long',
      date: new Date('2026-03-29')
    },
    {
      id: 4,
      operacao: 'DEPOSITO',
      description: 'Depósito em Dinheiro',
      clienteRelacionado: null,
      dataHora: '29/03/2026 11:30',
      amount: '312,15',
      tipo: 'entrada',
      icon: 'fa-solid fa-building-columns',
      date: new Date('2026-03-29')
    },
    {
      id: 5,
      operacao: 'TRANSFERENCIA',
      description: 'Uber Trip',
      clienteRelacionado: 'Uber do Brasil',
      dataHora: '28/03/2026 18:10',
      amount: '42,90',
      tipo: 'saida',
      icon: 'fa-solid fa-car',
      date: new Date('2026-03-28')
    }
  ];
  groupedTransactions: any[] = [];

  ngOnInit() {
    this.groupTransactions(this.transactions);
  }

  filterDays(days: number) {
    const now = new Date();

    const filtered = this.transactions.filter(t => {
      const diff = (now.getTime() - new Date(t.date).getTime()) / (1000 * 60 * 60 * 24);
      return diff <= days;
    });

    this.groupTransactions(filtered);
  }

  groupTransactions(data: any[]) {
    const groups: any = {};

    data.forEach(t => {
      const date = new Date(t.date);
      const today = new Date();

      let label = '';

      if (date.toDateString() === today.toDateString()) {
        label = 'Hoje';
      } else {
        const yesterday = new Date();
        yesterday.setDate(today.getDate() - 1);

        if (date.toDateString() === yesterday.toDateString()) {
          label = 'Ontem';
        } else {
          label = date.toLocaleDateString('pt-BR');
        }
      }

      if (!groups[label]) groups[label] = [];
      groups[label].push(t);
    });

    this.groupedTransactions = Object.keys(groups).map(label => ({
      label,
      items: groups[label]
    }));
  }
}
