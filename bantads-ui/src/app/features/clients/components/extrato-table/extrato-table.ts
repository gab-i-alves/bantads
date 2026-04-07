import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { MovimentacaoService } from '../../../../core/services/movimentacao.service';
import { Movimentacao } from '../../../../core/models/movimentacao.model';

interface ActivityItem {
  label: string;
  sublabel: string;
  amount: string;
  category: string;
  isPositive: boolean;
}

@Component({
  selector: 'app-extrato-table',
  imports: [RouterLink],
  templateUrl: './extrato-table.html',
  styleUrl: './extrato-table.css',
})
export class ExtratoTable {

  private authService = inject(AuthService);
  private movService = inject(MovimentacaoService);

  items: ActivityItem[] = [];

  ngOnInit() {
    const user = this.authService.getUsuarioLogado();
    if (!user) return;

    const movs = this.movService.getByClienteId(user.idCliente);

    // ultimas 3 movimentacoes (mais recentes primeiro)
    const sorted = [...movs].sort((a, b) =>
      this.parseData(b.dataHora).getTime() - this.parseData(a.dataHora).getTime()
    );

    this.items = sorted.slice(0, 3).map(m => this.toActivity(m, user.idCliente));
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

  private toActivity(mov: Movimentacao, idCliente: number): ActivityItem {
    let isPositive: boolean;
    let label: string;
    let category: string;

    if (mov.tipo === 'DEPOSITO') {
      isPositive = true;
      label = 'Depósito';
      category = 'Depósito';
    } else if (mov.tipo === 'SAQUE') {
      isPositive = false;
      label = 'Saque';
      category = 'Saque';
    } else {
      // TRANSFERENCIA
      if (mov.idClienteOrigem === idCliente) {
        isPositive = false;
        label = 'Transferência para ' + (mov.nomeClienteDestino || '');
        category = 'Transferência';
      } else {
        isPositive = true;
        label = 'Transferência de ' + mov.nomeClienteOrigem;
        category = 'Transferência';
      }
    }

    const valor = mov.valor.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

    return {
      label,
      sublabel: mov.dataHora,
      amount: (isPositive ? '+ ' : '- ') + 'R$ ' + valor,
      category,
      isPositive
    };
  }
}
