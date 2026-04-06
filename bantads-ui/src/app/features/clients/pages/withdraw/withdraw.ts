import { Component, inject } from '@angular/core';
import { Header } from '../../../../shared/components/header/header';
import { AuthService } from '../../../../core/services/auth.service';
import { ClientService } from '../../../../core/services/client.service';

@Component({
  selector: 'app-withdraw',
  imports: [Header],
  templateUrl: './withdraw.html',
  styleUrl: './withdraw.css',
})
export class Withdraw {


  clientService = inject(ClientService);
  authService = inject(AuthService);

  amount: string = '0,00';
  isActive: boolean = false;

  formatCurrency(value: string) {
    let numeric = value.replace(/\D/g, '');

    if (numeric === '') numeric = '0';

    let number = (parseInt(numeric) / 100).toFixed(2);

    this.amount = number.replace('.', ',');
  }

  triggerWithdraw() {
    if (this.amount === '0,00') {
      alert('Por favor, insira um valor para o saque.');
      return;
    }

    this.isActive = true;

    this.clientService.sacarDinheiro(this.authService.getUsuarioLogado().idCliente, parseFloat(this.amount.replace(',', '.')));

    setTimeout(() => {
      console.log('Saque processado! valor sacado: R$ ' + this.amount);
      this.isActive = false;
    }, 3000);
  }
}
