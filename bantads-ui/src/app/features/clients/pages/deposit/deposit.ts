import { Component, OnInit, inject, signal } from '@angular/core';
import { Header } from '../../../../shared/components/header/header';
import { AuthService } from '../../../../core/services/auth.service';
import { ContaService } from '../../../../core/services/conta.service';
import { ContaResumo } from '../../../../core/models/conta.model';

@Component({
  selector: 'app-deposit',
  imports: [Header],
  templateUrl: './deposit.html',
  styleUrl: './deposit.css',
})
export class Deposit implements OnInit {

  authService = inject(AuthService);
  private contaService = inject(ContaService);

  conta = signal<ContaResumo | null>(null);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  amount: string = '0,00';
  isActive: boolean = false;

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
        if (!conta) {
          this.errorMessage.set('Conta nao encontrada.');
        }
      },
      error: (error) => {
        console.error('Erro ao buscar conta para deposito:', error);
        this.errorMessage.set('Nao foi possivel carregar a conta.');
      }
    });
  }

  formatCurrency(value: string) {
    let numeric = value.replace(/\D/g, '');

    if (numeric === '') numeric = '0';

    let number = (parseInt(numeric) / 100).toFixed(2);

    this.amount = number.replace('.', ',');
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }

  triggerDeposit() {
    const conta = this.conta();
    const valor = parseFloat(this.amount.replace(',', '.'));

    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (!conta) {
      this.errorMessage.set('Conta nao encontrada.');
      return;
    }

    if (!Number.isFinite(valor) || valor <= 0) {
      this.errorMessage.set('Informe um valor maior que zero para depositar.');
      return;
    }

    this.isActive = true;
    this.contaService.depositar(conta.numero, valor).subscribe({
      next: (response) => {
        this.conta.set({ ...conta, saldo: response.saldo });
        this.amount = '0,00';
        this.successMessage.set('Deposito realizado com sucesso.');
      },
      error: (error) => {
        console.error('Erro ao realizar deposito:', error);
        this.errorMessage.set('Nao foi possivel realizar o deposito.');
        this.isActive = false;
      },
      complete: () => {
        this.isActive = false;
      }
    });
  }
}
