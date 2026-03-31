import { Component } from '@angular/core';
import { Header } from '../../../../shared/components/header/header';

@Component({
  selector: 'app-transfer',
  imports: [Header],
  templateUrl: './transfer.html',
  styleUrl: './transfer.css',
})
export class Transfer {
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

    setTimeout(() => {
      console.log('Depósito processado! valor depositado: R$ ' + this.amount);
      this.isActive = false;
    }, 3000);
  }
}
