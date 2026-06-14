import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

type PedidoAutocadastro = {
  id?: number;
  cpf: string;
  nome: string;
  salario: number;
  status?: string;
};

@Component({
  selector: 'app-aprove-client',
  imports: [CommonModule, FormsModule],
  templateUrl: './aprove-client.html',
  styleUrl: './aprove-client.css',
})
export class AproveClient {
  @Input() pedido: PedidoAutocadastro | null = null;
  @Output() fecharModal = new EventEmitter<void>();
  @Output() aprovarPedido = new EventEmitter<PedidoAutocadastro>();
  @Output() recusarPedido = new EventEmitter<{ pedido: PedidoAutocadastro; motivo: string }>();

  motivoRecusa = '';

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
      this.recusarPedido.emit({
        pedido: this.pedido,
        motivo: this.motivoRecusa.trim() || 'Cadastro recusado pelo gerente.'
      });
    }
  }
  recusar: boolean = false;
  recurarIsActive() {
    this.recusar = !this.recusar;

  }
}
