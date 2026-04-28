import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { HeaderManager } from '../../../../shared/components/header-manager/header-manager';
import { AproveClient } from '../../components/aprove-client/aprove-client';
import { AuthService } from '../../../../core/services/auth.service';

type PedidoAutocadastro = {
  cpf: string;
  nome: string;
  salario: number;
  aprovado: boolean | null;
};

@Component({
  selector: 'app-dashboard',
  imports: [HeaderManager, AproveClient],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})

export class DashboardManager {
  selectedPedido = signal<PedidoAutocadastro | null>(null);
  isModalOpen = signal(false);

  authService = inject(AuthService);
  private router = inject(Router);


  abrirModal(pedido: PedidoAutocadastro) {
    this.selectedPedido.set(pedido);
    this.isModalOpen.set(true);
  }

  fecharModal() {
    this.isModalOpen.set(false);
    this.selectedPedido.set(null);
  }

  aprovarPedido(pedido: any) {
    this.fecharModal();
  }

  recusarPedido(pedido: any) {
    this.fecharModal();}

  navegarPara(rota: string) {
    this.router.navigate([rota]);
  }
}
