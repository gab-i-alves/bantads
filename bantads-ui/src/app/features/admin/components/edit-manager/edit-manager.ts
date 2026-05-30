import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Gerente, GerenteUpdate } from '../../../../core/models/admin.model';


@Component({
  selector: 'app-edit-manager',
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-manager.html',
  styleUrl: './edit-manager.css',
})
export class EditManager {
  @Input() gerente: Gerente | null = null;
  @Output() fecharModal = new EventEmitter<void>();
  @Output() salvarGerente = new EventEmitter<{ cpf: string; gerente: GerenteUpdate }>();

  closeModal() {
    this.fecharModal.emit();
  }

  salvarEdicao() {
    if (!this.gerente?.nome || !this.gerente.email) {
      alert('Por favor, preencha todos os campos!');
      return;
    }

    const gerenteAtualizado = {
      nome: this.gerente.nome,
      email: this.gerente.email,
      role: this.gerente.role || 'GERENTE',
    };

    this.salvarGerente.emit({
      cpf: this.gerente.cpf,
      gerente: gerenteAtualizado,
    });
  }
}
