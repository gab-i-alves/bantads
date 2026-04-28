import { Component, EventEmitter, Input, Output, signal, effect, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'app-edit-manager',
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-manager.html',
  styleUrl: './edit-manager.css',
})
export class EditManager {
  @Input() gerente: any;
  @Output() fecharModal = new EventEmitter<void>();



  closeModal() {
    this.fecharModal.emit();
  }


  salvarEdicao() {
    const gerenteAtualizado = {
      nome: this.gerente.nome,
      cpf: this.gerente.cpf,
      email: this.gerente.email,
      telefone: this.gerente.telefone,
      senha: this.gerente.senha
    };

    // this.managerService.updateGerente(
    //   this.gerente.gerenteId,
    //   gerenteAtualizado
    // );

    this.closeModal();
    window.location.reload();
  }
}
