import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { AuthService } from '../../../../core/services/auth.service';
import { ContaService } from '../../../../core/services/conta.service';
import { ContaResumo } from '../../../../core/models/conta.model';

@Component({
  selector: 'app-cash-balance',
  imports: [],
  templateUrl: './cash-balance.html',
  styleUrl: './cash-balance.css',
})
export class CashBalance implements OnInit {
  private authService = inject(AuthService);
  private contaService = inject(ContaService);

  usuario = signal(this.authService.getUsuarioLogado());
  conta = signal<ContaResumo | null>(null);
  isLoadingConta = signal(false);

  saldoFormatado = computed(() => this.formatarMoeda(this.conta()?.saldo ?? 0));
  limiteFormatado = computed(() => this.formatarMoeda(this.conta()?.limite ?? 0));
  saldoNegativo = computed(() => (this.conta()?.saldo ?? 0) < 0);

  ngOnInit() {
    const cpf = this.usuario()?.cpf;
    if (!cpf) {
      return;
    }

    this.isLoadingConta.set(true);
    this.contaService.buscarContaPorClienteCpf(cpf).subscribe({
      next: (conta) => {
        this.conta.set(conta);
        this.isLoadingConta.set(false);
      },
      error: (error) => {
        console.error('Erro ao buscar conta do cliente:', error);
        this.isLoadingConta.set(false);
      }
    });
  }

  private formatarMoeda(valor: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(valor);
  }
}
