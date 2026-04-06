import { Component, inject, signal } from '@angular/core';
import { HeaderManager } from '../../../../shared/components/header-manager/header-manager';
import { AproveClient } from '../../components/aprove-client/aprove-client';
import { ClientService } from '../../../../core/services/client.service';
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

  clientService = inject(ClientService);
  authService = inject(AuthService);

  clientesPendentes: any;
  ngOnInit() {
    this.clientesPendentes = this.clientService.getClientesByGerente(this.authService.getUsuarioLogado().gerenteId);
  }

  abrirModal(pedido: PedidoAutocadastro) {
    this.selectedPedido.set(pedido);
    this.isModalOpen.set(true);
  }

  fecharModal() {
    this.isModalOpen.set(false);
    this.selectedPedido.set(null);
  }

  aprovarPedido(pedido: any) {
    console.log(pedido.idCliente);
    alert('pedido aprovado');
    this.clientService.aprovarSolicitacao(pedido.idCliente);
    this.fecharModal();
  }

  recusarPedido(pedido: any) {
    alert('pedido recusado');
    this.clientService.recusarSolicitacao(pedido.idCliente);
    this.fecharModal();
  }
}
