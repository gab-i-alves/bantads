import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
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
  // Loading da edicao controlado pelo pai, para travar o botao enquanto a chamada esta em voo.
  @Input() salvando = false;
  @Output() fecharModal = new EventEmitter<void>();
  @Output() salvarGerente = new EventEmitter<{ cpf: string; gerente: GerenteUpdate }>();

  // R20: a senha pode ser trocada na edicao; fica vazia quando o admin nao quer alterar.
  senha = signal('');
  erro = signal('');

  closeModal() {
    this.fecharModal.emit();
  }

  salvarEdicao() {
    if (this.salvando || !this.gerente) {
      return;
    }

    const erro = this.validarCampos();
    if (erro) {
      this.erro.set(erro);
      return;
    }

    this.erro.set('');

    // R20: nao enviamos role/tipo, o papel do gerente nao e editavel aqui.
    const gerenteAtualizado: GerenteUpdate = {
      nome: this.gerente.nome.trim(),
      email: this.gerente.email.trim(),
      telefone: (this.gerente.telefone || '').replace(/\D/g, ''),
    };

    const novaSenha = this.senha().trim();
    if (novaSenha) {
      gerenteAtualizado.senha = novaSenha;
    }

    this.salvarGerente.emit({
      cpf: this.gerente.cpf,
      gerente: gerenteAtualizado,
    });
  }

  // NF16: mascara de telefone aplicada na digitacao.
  formatTelefone(value: string) {
    if (!this.gerente) {
      return;
    }

    this.gerente.telefone = value.replace(/\D/g, '')
      .slice(0, 11)
      .replace(/(\d{2})(\d)/, '($1) $2')
      .replace(/(\d{4,5})(\d{4})$/, '$1-$2');
  }

  // NF14: campos obrigatorios e formato de email.
  private validarCampos(): string {
    if (!this.gerente?.nome?.trim() || !this.gerente?.email?.trim()) {
      return 'Preencha nome e email.';
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.gerente.email.trim())) {
      return 'Email invalido.';
    }

    return '';
  }
}
