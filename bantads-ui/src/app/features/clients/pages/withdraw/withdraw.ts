import { Component, OnInit, inject, signal } from '@angular/core';
import { Header } from '../../../../shared/components/header/header';
import { AuthService } from '../../../../core/services/auth.service';
import { ContaService } from '../../../../core/services/conta.service';
import { ContaResumo } from '../../../../core/models/conta.model';

@Component({
  selector: 'app-withdraw',
  imports: [Header],
  templateUrl: './withdraw.html',
  styleUrl: './withdraw.css',
})
export class Withdraw implements OnInit {


  authService = inject(AuthService);
  private contaService = inject(ContaService);

  amount: string = '0,00';
  isActive: boolean = false;
  animMs = 1300;
  conta = signal<ContaResumo | null>(null);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

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
        console.error('Erro ao buscar conta para saque:', error);
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

  triggerWithdraw() {
    if (this.isActive) return;

    const conta = this.conta();
    const valor = parseFloat(this.amount.replace(',', '.'));

    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (!conta) {
      this.errorMessage.set('Conta nao encontrada.');
      return;
    }

    if (!Number.isFinite(valor) || valor <= 0) {
      this.errorMessage.set('Informe um valor positivo para sacar.');
      return;
    }

    if (valor > conta.saldo + conta.limite) {
      this.errorMessage.set('Saldo e limite insuficientes.');
      return;
    }

    this.isActive = true;
    const inicio = Date.now();
    this.contaService.sacar(conta.numero, valor).subscribe({
      next: (response) => this.finalizar(inicio, () => {
        this.conta.set({ ...conta, saldo: response.saldo });
        this.amount = '0,00';
        this.successMessage.set('Saque realizado com sucesso.');
      }),
      error: (error) => this.finalizar(inicio, () => {
        console.error('Erro ao realizar saque:', error);
        this.errorMessage.set('Nao foi possivel realizar o saque.');
      }),
    });
  }

  finalizar(inicio: number, acao: () => void) {
    const restante = Math.max(0, this.animMs - (Date.now() - inicio));
    setTimeout(() => {
      acao();
      this.isActive = false;
    }, restante);
  }
}
