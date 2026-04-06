import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

type PedidoAutocadastro = {
  idCliente: number;
  cpf: string;
  nome: string;
  salario: number;
  aprovado: boolean | null;
  statusAprovacao: string;
};

@Component({
  selector: 'app-aprove-client',
  imports: [CommonModule],
  templateUrl: './aprove-client.html',
  styleUrl: './aprove-client.css',
})
export class AproveClient {
  @Input() pedido: any = null;
  @Output() fecharModal = new EventEmitter<void>();
  @Output() aprovarPedido = new EventEmitter<PedidoAutocadastro>();
  @Output() recusarPedido = new EventEmitter<PedidoAutocadastro>();

  closeModal() {
    this.fecharModal.emit();
  }

  onAprovar() {
    if (this.pedido) {
      this.aprovarPedido.emit(this.pedido);
    }
  }

  onRecusar() {
    if (this.pedido) {
      this.recusarPedido.emit(this.pedido);
    }
  }
  recusar: boolean = false;
  recurarIsActive() {
    this.recusar = !this.recusar;

  }
}
