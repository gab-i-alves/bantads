import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Header } from '../../../../shared/components/header/header';
import { AuthService } from '../../../../core/services/auth.service';
import { ContaService } from '../../../../core/services/conta.service';
import { ContaResumo } from '../../../../core/models/conta.model';

@Component({
  selector: 'app-transfer',
  imports: [Header, FormsModule],
  templateUrl: './transfer.html',
  styleUrl: './transfer.css',
})
export class Transfer implements OnInit {
  private authService = inject(AuthService);
  private contaService = inject(ContaService);

  amount: string = '0,00';
  destino: string = '';
  isActive: boolean = false;
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  contaOrigem = signal<ContaResumo | null>(null);

  ngOnInit() {
    const usuario = this.authService.getUsuarioLogado();

    if (!usuario?.cpf) {
      this.errorMessage.set('Nao foi possivel identificar o usuario logado.');
      return;
    }

    this.contaService.buscarContaPorClienteCpf(usuario.cpf).subscribe({
      next: (conta) => {
        this.contaOrigem.set(conta);
        if (!conta) {
          this.errorMessage.set('Conta de origem nao encontrada.');
        }
      },
      error: (error) => {
        console.error('Erro ao buscar conta de origem:', error);
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

  transferir() {
    const origem = this.contaOrigem();
    const destino = this.destino.trim();
    const amountNumber = parseFloat(this.amount.replace(',', '.'));

    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (!origem) {
      this.errorMessage.set('Conta de origem nao encontrada.');
      return;
    }

    if (!destino) {
      this.errorMessage.set('Informe o numero da conta destino.');
      return;
    }

    if (destino === origem.numero) {
      this.errorMessage.set('Nao e possivel transferir para a propria conta.');
      return;
    }

    if (!Number.isFinite(amountNumber) || amountNumber <= 0) {
      this.errorMessage.set('Informe um valor positivo para transferir.');
      return;
    }

    if (amountNumber > origem.saldo) {
      this.errorMessage.set('O valor nao pode ser maior que o saldo atual.');
      return;
    }

    this.isActive = true;
    this.contaService.transferir(origem.numero, destino, amountNumber).subscribe({
      next: (response) => {
        this.contaOrigem.set({ ...origem, saldo: response.saldo });
        this.amount = '0,00';
        this.destino = '';
        this.successMessage.set('Transferencia realizada com sucesso.');
      },
      error: (error) => {
        console.error('Erro ao realizar transferencia:', error);
        this.errorMessage.set('Nao foi possivel realizar a transferencia. Verifique o numero da conta destino.');
        this.isActive = false;
      },
      complete: () => {
        this.isActive = false;
      }
    });
  }
}
