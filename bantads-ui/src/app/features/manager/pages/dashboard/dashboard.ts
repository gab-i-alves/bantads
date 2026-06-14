import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { HeaderManager } from '../../../../shared/components/header-manager/header-manager';
import { AproveClient } from '../../components/aprove-client/aprove-client';
import { AuthService } from '../../../../core/services/auth.service';
import { ClienteService } from '../../../../core/services/cliente.service';
import { Cliente } from '../../../../core/models/cliente.model';

type PedidoAutocadastro = Cliente;

@Component({
  selector: 'app-dashboard',
  imports: [HeaderManager, AproveClient],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})

export class DashboardManager implements OnInit {
  clientesPendentes = signal<PedidoAutocadastro[]>([]);
  selectedPedido = signal<PedidoAutocadastro | null>(null);
  isModalOpen = signal(false);
  isLoadingPendentes = signal(false);
  errorMessage = signal('');

  authService = inject(AuthService);
  private router = inject(Router);
  private clienteService = inject(ClienteService);

  ngOnInit() {
    this.carregarClientesPendentes();
  }

  carregarClientesPendentes() {
    this.isLoadingPendentes.set(true);
    this.errorMessage.set('');

    this.clienteService.listarClientesPendentes().subscribe({
      next: (clientes) => {
        this.clientesPendentes.set(clientes);
        this.isLoadingPendentes.set(false);
      },
      error: (error) => {
        console.error('Erro ao buscar clientes pendentes:', error);
        this.errorMessage.set('Nao foi possivel carregar os clientes pendentes.');
        this.isLoadingPendentes.set(false);
      }
    });
  }

  abrirModal(pedido: PedidoAutocadastro) {
    this.selectedPedido.set(pedido);
    this.isModalOpen.set(true);
  }

  fecharModal() {
    this.isModalOpen.set(false);
    this.selectedPedido.set(null);
  }

  aprovarPedido(pedido: PedidoAutocadastro) {
    this.clienteService.aprovarCliente(pedido.cpf).subscribe({
      next: () => {
        this.clientesPendentes.update(clientes =>
          clientes.filter(cliente => cliente.cpf !== pedido.cpf)
        );
        this.fecharModal();
        alert('Cliente aprovado. O login e a conta serão criados automaticamente.');
      },
      error: (error) => {
        console.error('Erro ao aprovar cliente:', error);
        alert('Não foi possível aprovar o cliente. Tente novamente em instantes.');
      }
    });
  }

  recusarPedido(evento: { pedido: PedidoAutocadastro; motivo: string }) {
    this.clienteService.rejeitarCliente(evento.pedido.cpf, evento.motivo).subscribe({
      next: () => {
        this.clientesPendentes.update(clientes =>
          clientes.filter(cliente => cliente.cpf !== evento.pedido.cpf)
        );
        this.fecharModal();
      },
      error: (error) => {
        console.error('Erro ao rejeitar cliente:', error);
        alert('Nao foi possivel rejeitar o cliente.');
      }
    });
  }

  navegarPara(rota: string) {
    this.router.navigate([rota]);
  }
}
