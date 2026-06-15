import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
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

  // Loading da criacao controlado pelo pai, para travar o botao enquanto a chamada esta em voo.
  @Input() salvando = false;
  @Output() fecharModal = new EventEmitter<void>();
  @Output() adicionarGerente = new EventEmitter<GerenteCreate>();

  nome = signal('');
  cpf = signal('');
  email = signal('');
  telefone = signal('');
  senha = signal('');
  erro = signal('');

  closeModal() {
    this.fecharModal.emit();
    this.limparFormulario();
  }

  salvarGerente() {
    if (this.salvando) {
      return;
    }

    const erro = this.validarCampos();
    if (erro) {
      this.erro.set(erro);
      return;
    }

    this.erro.set('');

    const novoGerente: GerenteCreate = {
      nome: this.nome().trim(),
      cpf: this.onlyDigits(this.cpf()),
      email: this.email().trim(),
      telefone: this.onlyDigits(this.telefone()),
      senha: this.senha(),
      role: 'GERENTE',
    };

    this.adicionarGerente.emit(novoGerente);
  }

  // NF16: mascara de CPF aplicada na digitacao.
  formatCpf(value: string) {
    this.cpf.set(this.onlyDigits(value)
      .slice(0, 11)
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2'));
  }

  // NF16: mascara de telefone aplicada na digitacao.
  formatTelefone(value: string) {
    this.telefone.set(this.onlyDigits(value)
      .slice(0, 11)
      .replace(/(\d{2})(\d)/, '($1) $2')
      .replace(/(\d{4,5})(\d{4})$/, '$1-$2'));
  }

  // NF14: campos obrigatorios e formato de email/CPF.
  private validarCampos(): string {
    if (!this.nome().trim() || !this.cpf() || !this.email().trim() || !this.senha()) {
      return 'Preencha todos os campos obrigatorios.';
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.email().trim())) {
      return 'Email invalido.';
    }

    if (this.onlyDigits(this.cpf()).length !== 11) {
      return 'CPF deve conter 11 digitos.';
    }

    const telefone = this.onlyDigits(this.telefone());
    if (telefone && telefone.length < 10) {
      return 'Telefone deve conter DDD + numero.';
    }

    return '';
  }

  private onlyDigits(value: string): string {
    return value.replace(/\D/g, '');
  }

  private limparFormulario() {
    this.nome.set('');
    this.cpf.set('');
    this.email.set('');
    this.telefone.set('');
    this.senha.set('');
    this.erro.set('');
  }
}
