import { Component, OnInit, inject, signal } from '@angular/core';
import { AuthService } from '../../../../core/services/auth.service';
import { ContaService } from '../../../../core/services/conta.service';
import { ContaResumo } from '../../../../core/models/conta.model';

@Component({
  selector: 'app-small-card',
  imports: [],
  templateUrl: './small-card.html',
  styleUrl: './small-card.css',
})
export class SmallCard implements OnInit {

  private authService = inject(AuthService);
  private contaService = inject(ContaService);

  usuario = signal(this.authService.getUsuarioLogado());
  conta = signal<ContaResumo | null>(null);

  ngOnInit() {
    const cpf = this.usuario()?.cpf;
    if (!cpf) {
      return;
    }

    this.contaService.buscarContaPorClienteCpf(cpf).subscribe({
      next: conta => this.conta.set(conta),
      error: error => console.error('Erro ao buscar dados da conta:', error)
    });
  }
}
