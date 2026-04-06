import { Component, inject } from '@angular/core';
import { Header } from '../../../../shared/components/header/header';
import { AuthService } from '../../../../core/services/auth.service';
import { ClientService } from '../../../../core/services/client.service';

@Component({
  selector: 'app-deposit',
  imports: [Header],
  templateUrl: './deposit.html',
  styleUrl: './deposit.css',
})
export class Deposit {

  authService = inject(AuthService);
  clientService = inject(ClientService);
  userLogado = this.authService.getUsuarioLogado();





  amount: string = '0,00';
  isActive: boolean = false;

  formatCurrency(value: string) {
    let numeric = value.replace(/\D/g, '');

    if (numeric === '') numeric = '0';

    let number = (parseInt(numeric) / 100).toFixed(2);

    this.amount = number.replace('.', ',');
  }

  triggerDeposit() {
    if (this.amount === '0,00') {
      alert('Por favor, insira um valor para o depósito.');
      return;
    }

    this.isActive = true;

    const valor = parseFloat(this.amount.replace(',', '.'));
    console.log('Iniciando depósito! valor a ser depositado: R$ ' + valor);

    this.clientService.depositarDinheiro(this.userLogado.idCliente, valor);

    setTimeout(() => {
      console.log('Depósito processado! valor depositado: R$ ' + this.amount);
      this.isActive = false;
    }, 3000);
  }
}
