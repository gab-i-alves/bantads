import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Header } from '../../../../shared/components/header/header';
import { AuthService } from '../../../../core/services/auth.service';

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

  groupedTransactions: GroupedDay[] = [];
  dataInicio: string = '';
  dataFim: string = '';

  private idCliente!: number;

  ngOnInit() {

  }


  filtrar() {
    if (!this.dataInicio || !this.dataFim) {
     
      return;
    }

    const [anoI, mesI, diaI] = this.dataInicio.split('-').map(Number);
    const inicio = new Date(anoI, mesI - 1, diaI);

    const [anoF, mesF, diaF] = this.dataFim.split('-').map(Number);
    const fim = new Date(anoF, mesF - 1, diaF);

  }

  limparFiltro() {
    this.dataInicio = '';
    this.dataFim = '';
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

}
