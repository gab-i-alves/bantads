import { Component, EventEmitter, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GerenteCreate } from '../../../../core/models/admin.model';

@Component({
  selector: 'app-new-manager',
  imports: [CommonModule, FormsModule],
  templateUrl: './new-manager.html',
  styleUrl: './new-manager.css',
})
export class NewManager {

  @Output() fecharModal = new EventEmitter<void>();
  @Output() adicionarGerente = new EventEmitter<GerenteCreate>();

  nome = signal('');
  cpf = signal('');
  email = signal('');
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

    const novoGerente: GerenteCreate = {
      nome: this.nome(),
      cpf: this.cpf(),
      email: this.email(),
      senha: this.senha(),
      role: 'GERENTE',
    };

    this.adicionarGerente.emit(novoGerente);
  }

  private validarCampos(): boolean {
    return (
      this.nome() !== '' &&
      this.cpf() !== '' &&
      this.email() !== '' &&
      this.senha() !== ''
    );
  }

  private limparFormulario() {
    this.nome.set('');
    this.cpf.set('');
    this.email.set('');
    this.senha.set('');
  }
}
