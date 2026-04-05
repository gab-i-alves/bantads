import { Component, signal } from '@angular/core';
import { Header } from '../../../../shared/components/header/header';
import { HeaderManager } from '../../../../shared/components/header-manager/header-manager';
import { AproveClient } from '../../components/aprove-client/aprove-client';

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
  
  pedidos: PedidoAutocadastro[] = [
    {
      cpf: "123.456.789-00",
      nome: "João Silva",
      salario: 5000,
      aprovado: null
    },
    {
      cpf: "987.654.321-11",
      nome: "Maria Oliveira",
      salario: 12450,
      aprovado: null
    },
    {
      cpf: "456.123.789-99",
      nome: "Ricardo Costa",
      salario: 3200,
      aprovado: null
    },
    {
      cpf: "321.654.987-22",
      nome: "Ana Amaral",
      salario: 8900,
      aprovado: null
    },
    {
      cpf: "741.852.963-00",
      nome: "Fernando Pereira",
      salario: 21000,
      aprovado: null
    }
  ];

  abrirModal(pedido: PedidoAutocadastro) {
    this.selectedPedido.set(pedido);
    this.isModalOpen.set(true);
  }

  fecharModal() {
    this.isModalOpen.set(false);
    this.selectedPedido.set(null);
  }

  aprovarPedido(pedido: PedidoAutocadastro) {
    pedido.aprovado = true;
    this.fecharModal();
  }

  recusarPedido(pedido: PedidoAutocadastro) {
    pedido.aprovado = false;
    this.fecharModal();
  }
}
