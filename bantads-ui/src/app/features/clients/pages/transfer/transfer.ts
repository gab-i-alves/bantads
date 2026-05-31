import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Header } from '../../../../shared/components/header/header';
import { AuthService } from '../../../../core/services/auth.service';
import { signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { ClienteService } from '../../../../core/services/cliente.service';
import { ContaService } from '../../../../core/services/conta.service';
import { Cliente } from '../../../../core/models/cliente.model';
import { ContaResumo } from '../../../../core/models/conta.model';

interface TransferRecipient {
  cliente: Cliente;
  conta: ContaResumo;
}

@Component({
  selector: 'app-transfer',
  imports: [Header, FormsModule],
  templateUrl: './transfer.html',
  styleUrl: './transfer.css',
})
export class Transfer implements OnInit {
  private authService = inject(AuthService);
  private clienteService = inject(ClienteService);
  private contaService = inject(ContaService);

  amount: string = '0,00';
  isActive: boolean = false;
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  isLoading = signal(false);

  contaOrigem = signal<ContaResumo | null>(null);
  recipients = signal<TransferRecipient[]>([]);
  selectedRecipient = signal<TransferRecipient | null>(null);

  ngOnInit() {
    this.loadTransferData();
  }

  loadTransferData() {
    const currentUser = this.authService.getUsuarioLogado();

    if (!currentUser?.cpf) {
      this.errorMessage.set('Nao foi possivel identificar o usuario logado.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    forkJoin({
      clientes: this.clienteService.listarClientes(),
      contas: this.contaService.listarContas()
    }).subscribe({
      next: ({ clientes, contas }) => {
        const contaOrigem = contas.find(conta => conta.clienteCpf === currentUser.cpf) || null;
        this.contaOrigem.set(contaOrigem);

        if (!contaOrigem) {
          this.errorMessage.set('Conta de origem nao encontrada.');
          this.recipients.set([]);
          return;
        }

        const destinatarios = clientes
          .filter(cliente => cliente.cpf !== currentUser.cpf)
          .map(cliente => ({
            cliente,
            conta: contas.find(conta => conta.clienteCpf === cliente.cpf)
          }))
          .filter((item): item is TransferRecipient => !!item.conta);

        this.recipients.set(destinatarios);
      },
      error: (error) => {
        console.error('Erro ao carregar dados para transferencia:', error);
        this.errorMessage.set('Nao foi possivel carregar os destinatarios.');
        this.isLoading.set(false);
      },
      complete: () => this.isLoading.set(false)
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
    const destino = this.selectedRecipient();
    const amountNumber = parseFloat(this.amount.replace(',', '.'));

    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (!origem) {
      this.errorMessage.set('Conta de origem nao encontrada.');
      return;
    }

    if (!destino) {
      this.errorMessage.set('Selecione um destinatario.');
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
    this.contaService.transferir(origem.numero, destino.conta.numero, amountNumber).subscribe({
      next: (response) => {
        this.contaOrigem.set({ ...origem, saldo: response.saldo });
        this.amount = '0,00';
        this.selectedRecipient.set(null);
        this.successMessage.set('Transferencia realizada com sucesso.');
      },
      error: (error) => {
        console.error('Erro ao realizar transferencia:', error);
        this.errorMessage.set('Nao foi possivel realizar a transferencia.');
        this.isActive = false;
      },
      complete: () => {
        this.isActive = false;
      }
    });
  }
}
