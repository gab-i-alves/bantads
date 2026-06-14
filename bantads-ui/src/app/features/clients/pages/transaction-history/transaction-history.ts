import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Header } from '../../../../shared/components/header/header';
import { AuthService } from '../../../../core/services/auth.service';
import { ContaService } from '../../../../core/services/conta.service';
import { ContaResumo, Extrato, MovimentacaoExtrato } from '../../../../core/models/conta.model';

interface TransactionView {
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
  date: string;
  items: TransactionView[];
  dailyBalance: string | null;
  semMovimentacao: boolean;
}

@Component({
  selector: 'app-transaction-history',
  imports: [Header, FormsModule],
  templateUrl: './transaction-history.html',
  styleUrl: './transaction-history.css',
})
export class TransactionHistory implements OnInit {

  private authService = inject(AuthService);
  private contaService = inject(ContaService);

  conta = signal<ContaResumo | null>(null);
  groupedTransactions = signal<GroupedDay[]>([]);
  errorMessage = signal<string | null>(null);
  isLoading = signal(false);

  dataInicio: string = '';
  dataFim: string = '';

  ngOnInit() {
    this.carregarConta();
  }

  carregarConta() {
    const usuario = this.authService.getUsuarioLogado();

    if (!usuario?.cpf) {
      this.errorMessage.set('Nao foi possivel identificar o usuario logado.');
      return;
    }

    this.contaService.buscarContaPorClienteCpf(usuario.cpf).subscribe({
      next: (conta) => {
        this.conta.set(conta);
        if (conta) {
          this.carregarExtrato();
        } else {
          this.errorMessage.set('Conta nao encontrada.');
        }
      },
      error: (error) => {
        console.error('Erro ao buscar conta para extrato:', error);
        this.errorMessage.set('Nao foi possivel carregar a conta.');
      }
    });
  }

  filtrar() {
    if (!this.dataInicio || !this.dataFim) {
      this.errorMessage.set('Informe data de inicio e fim para filtrar.');
      return;
    }
    this.carregarExtrato(this.dataInicio, this.dataFim);
  }

  limparFiltro() {
    this.dataInicio = '';
    this.dataFim = '';
    this.carregarExtrato();
  }

  private carregarExtrato(inicio?: string, fim?: string) {
    const conta = this.conta();
    if (!conta) {
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.contaService.consultarExtrato(conta.numero, inicio, fim).subscribe({
      next: (extrato) => {
        this.groupedTransactions.set(this.montarGrupos(extrato));
      },
      error: (error) => {
        console.error('Erro ao carregar extrato:', error);
        this.errorMessage.set('Nao foi possivel carregar o extrato.');
      },
      complete: () => this.isLoading.set(false)
    });
  }

  private montarGrupos(extrato: Extrato): GroupedDay[] {
    const movsPorDia = new Map<string, TransactionView[]>();
    for (const mov of extrato.movimentacoes ?? []) {
      const dia = mov.data.split('T')[0];
      const lista = movsPorDia.get(dia) ?? [];
      lista.push(this.toView(mov, extrato.conta));
      movsPorDia.set(dia, lista);
    }

    const saldos = extrato.saldosDiarios ?? [];

    if (!saldos.length) {
      return Array.from(movsPorDia.entries())
        .sort((a, b) => b[0].localeCompare(a[0]))
        .map(([dia, items]) => ({
          label: this.formatarDia(dia),
          date: dia,
          items: items.sort((a, b) => b.dataHora.localeCompare(a.dataHora)),
          dailyBalance: null,
          semMovimentacao: false,
        }));
    }

    const diasOrdenados = [...saldos].sort((a, b) => a.data.localeCompare(b.data));
    const entries: GroupedDay[] = [];
    let inicioVazio: string | null = null;
    let fimVazio: string | null = null;
    let saldoVazio = 0;

    const fecharIntervaloVazio = () => {
      if (!inicioVazio) {
        return;
      }
      entries.push({
        label: inicioVazio === fimVazio
          ? this.formatarDia(inicioVazio)
          : `${this.formatarDia(inicioVazio)} - ${this.formatarDia(fimVazio!)}`,
        date: inicioVazio,
        items: [],
        dailyBalance: this.formatarMoeda(saldoVazio),
        semMovimentacao: true,
      });
      inicioVazio = null;
      fimVazio = null;
    };

    for (const s of diasOrdenados) {
      const items = movsPorDia.get(s.data);
      if (items?.length) {
        fecharIntervaloVazio();
        entries.push({
          label: this.formatarDia(s.data),
          date: s.data,
          items: items.sort((a, b) => b.dataHora.localeCompare(a.dataHora)),
          dailyBalance: this.formatarMoeda(s.saldo),
          semMovimentacao: false,
        });
      } else {
        if (!inicioVazio) {
          inicioVazio = s.data;
        }
        fimVazio = s.data;
        saldoVazio = s.saldo;
      }
    }
    fecharIntervaloVazio();

    return entries.reverse();
  }

  private toView(mov: MovimentacaoExtrato, contaNumero: string): TransactionView {
    const operacao = this.normalizarOperacao(mov.tipo);
    const isSaque = operacao === 'SAQUE';
    const isTransferenciaSaida = operacao === 'TRANSFERENCIA' && mov.origem === contaNumero;
    const tipo: 'entrada' | 'saida' = isSaque || isTransferenciaSaida ? 'saida' : 'entrada';

    let clienteRelacionado: string | null = null;
    let description = '';
    if (operacao === 'TRANSFERENCIA') {
      clienteRelacionado = isTransferenciaSaida
        ? mov.nomeDestino ?? mov.destino
        : mov.nomeOrigem ?? mov.origem;
      description = isTransferenciaSaida
        ? `Para conta ${mov.destino}`
        : `De conta ${mov.origem}`;
    }

    return {
      operacao,
      description,
      clienteRelacionado,
      dataHora: this.formatarDataHora(mov.data),
      amount: this.formatarMoeda(Math.abs(mov.valor)),
      tipo,
      icon: '',
    };
  }

  private normalizarOperacao(tipo: string): string {
    return tipo
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '')
      .toUpperCase();
  }

  private formatarDia(dia: string): string {
    const [ano, mes, d] = dia.split('-');
    return `${d}/${mes}/${ano}`;
  }

  private formatarDataHora(iso: string): string {
    const [data, hora] = iso.split('T');
    const [ano, mes, dia] = data.split('-');
    const horaMinuto = hora ? hora.slice(0, 5) : '';
    return `${dia}/${mes}/${ano} ${horaMinuto}`.trim();
  }

  private formatarMoeda(valor: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(valor);
  }
}
