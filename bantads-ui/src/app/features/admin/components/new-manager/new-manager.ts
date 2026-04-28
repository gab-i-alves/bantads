import { Component, EventEmitter, inject, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

type Gerente = {
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  senha: string;
};

@Component({
  selector: 'app-new-manager',
  imports: [CommonModule, FormsModule],
  templateUrl: './new-manager.html',
  styleUrl: './new-manager.css',
})
export class NewManager {

  @Output() fecharModal = new EventEmitter<void>();
  @Output() adicionarGerente = new EventEmitter<Gerente>();

  nome = signal('');
  cpf = signal('');
  email = signal('');
  telefone = signal('');
  senha = signal('');

  closeModal() {
    this.fecharModal.emit();
    this.limparFormulario();
  }

  salvarGerente() {
    if (!this.validarCampos()) {
      alert('Por favor, preencha todos os campos!');
      return;
    }

    const novoGerente: Gerente = {
      nome: this.nome(),
      cpf: this.cpf(),
      email: this.email(),
      telefone: this.telefone(),
      senha: this.senha(),
    };

    //this.manageService.createGerente(novoGerente);
    this.limparFormulario();
    window.location.reload();
    this.fecharModal.emit();
  }

  private validarCampos(): boolean {
    return (
      this.nome() !== '' &&
      this.cpf() !== '' &&
      this.email() !== '' &&
      this.telefone() !== '' &&
      this.senha() !== ''
    );
  }

  private limparFormulario() {
    this.nome.set('');
    this.cpf.set('');
    this.email.set('');
    this.telefone.set('');
    this.senha.set('');
  }
}
