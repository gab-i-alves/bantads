import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Header } from '../../../../shared/components/header/header';
import { AuthService } from '../../../../core/services/auth.service';
import { MovimentacaoService } from '../../../../core/services/movimentacao.service';
import { Movimentacao } from '../../../../core/models/movimentacao.model';

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

@Component({
  selector: 'app-transaction-history',
  imports: [Header, FormsModule],
  templateUrl: './transaction-history.html',
  styleUrl: './transaction-history.css',
})
export class TransactionHistory {

  private authService = inject(AuthService);
  private movService = inject(MovimentacaoService);

  groupedTransactions: GroupedDay[] = [];
  dataInicio: string = '';
  dataFim: string = '';

  private idCliente!: number;

  ngOnInit() {
    const user = this.authService.getUsuarioLogado();
    if (user) {
      this.idCliente = user.idCliente;
    }
    this.loadAll();
  }

  loadAll() {
    const movs = this.movService.getByClienteId(this.idCliente);
    this.groupTransactions(movs);
  }

  filtrar() {
    if (!this.dataInicio || !this.dataFim) {
      this.loadAll();
      return;
    }

    const [anoI, mesI, diaI] = this.dataInicio.split('-').map(Number);
    const inicio = new Date(anoI, mesI - 1, diaI);

    const [anoF, mesF, diaF] = this.dataFim.split('-').map(Number);
    const fim = new Date(anoF, mesF - 1, diaF);

    const movs = this.movService.getByDateRange(this.idCliente, inicio, fim);
    this.groupTransactions(movs);
  }

  limparFiltro() {
    this.dataInicio = '';
    this.dataFim = '';
    this.loadAll();
  }

  private parseData(str: string): Date {
    const [datePart, timePart] = str.split(' ');
    const [dia, mes, ano] = datePart.split('/').map(Number);
    if (timePart) {
      const [hora, minuto] = timePart.split(':').map(Number);
      return new Date(ano, mes - 1, dia, hora, minuto);
    }
    return new Date(ano, mes - 1, dia);
  }

  private toView(mov: Movimentacao): TransactionView {
    // determina se eh entrada ou saida para o cliente logado
    let tipo: 'entrada' | 'saida';
    let clienteRelacionado: string | null = null;

    if (mov.tipo === 'DEPOSITO') {
      tipo = 'entrada';
    } else if (mov.tipo === 'SAQUE') {
      tipo = 'saida';
    } else {
      // TRANSFERENCIA
      if (mov.idClienteOrigem === this.idCliente) {
        tipo = 'saida';
        clienteRelacionado = mov.nomeClienteDestino || null;
      } else {
        tipo = 'entrada';
        clienteRelacionado = mov.nomeClienteOrigem;
      }
    }

    // icon e description derivados do tipo
    let icon: string;
    let description: string;
    let operacao: string;

    switch (mov.tipo) {
      case 'DEPOSITO':
        icon = 'fa-solid fa-building-columns';
        description = 'Depósito em Conta';
        operacao = 'DEPOSITO';
        break;
      case 'SAQUE':
        icon = 'fa-solid fa-money-bill-1-wave';
        description = 'Saque em Conta';
        operacao = 'SAQUE';
        break;
      case 'TRANSFERENCIA':
        icon = tipo === 'entrada' ? 'fa-solid fa-arrow-down-long' : 'fa-solid fa-arrow-up-long';
        description = tipo === 'entrada' ? 'Transferência Recebida' : 'Transferência Enviada';
        operacao = 'TRANSFERENCIA';
        break;
    }

    return {
      id: mov.id,
      operacao,
      description,
      clienteRelacionado,
      dataHora: mov.dataHora,
      amount: mov.valor.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
      tipo,
      icon
    };
  }

  private groupTransactions(movs: Movimentacao[]) {
    // ordena por data decrescente
    const sorted = [...movs].sort((a, b) =>
      this.parseData(b.dataHora).getTime() - this.parseData(a.dataHora).getTime()
    );

    const groups: Record<string, { date: Date; items: TransactionView[] }> = {};

    sorted.forEach(m => {
      const dt = this.parseData(m.dataHora);
      const dateKey = dt.toISOString().split('T')[0];

      if (!groups[dateKey]) {
        groups[dateKey] = { date: dt, items: [] };
      }
      groups[dateKey].items.push(this.toView(m));
    });

    this.groupedTransactions = Object.entries(groups).map(([_, group]) => {
      const today = new Date();
      const yesterday = new Date();
      yesterday.setDate(today.getDate() - 1);

      let label: string;
      if (group.date.toDateString() === today.toDateString()) {
        label = 'Hoje';
      } else if (group.date.toDateString() === yesterday.toDateString()) {
        label = 'Ontem';
      } else {
        label = group.date.toLocaleDateString('pt-BR');
      }

      // saldo consolidado do dia: soma entradas - soma saidas
      let saldoDia = 0;
      group.items.forEach(item => {
        const val = parseFloat(item.amount.replace(/\./g, '').replace(',', '.'));
        if (item.tipo === 'entrada') {
          saldoDia += val;
        } else {
          saldoDia -= val;
        }
      });

      return {
        label,
        date: group.date,
        items: group.items,
        dailyBalance: saldoDia.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
      };
    });
  }

  labelOperacao(tipo: string): string {
    switch (tipo) {
      case 'DEPOSITO': return 'Depósito';
      case 'SAQUE': return 'Saque';
      case 'TRANSFERENCIA': return 'Transferência';
      default: return tipo;
    }
  }
}
