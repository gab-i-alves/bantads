import { Component, inject, OnInit, signal } from '@angular/core';
import { Header } from '../../../../shared/components/header/header';
import { AuthService } from '../../../../core/services/auth.service';
import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../../../core/services/cliente.service';
import { Cliente, ClienteUpdate } from '../../../../core/models/cliente.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-profile',
  imports: [Header, FormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {

  authService = inject(AuthService);
  private clienteService = inject(ClienteService);

  meuDados = signal<Cliente | null>(null);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  isSaving = signal(false);
  step = signal(1);

  // Dados editáveis
  editavelData = {
    nome: '',
    email: '',
    salario: 0,
    funcao: '',
    telefone: '',
    cep: '',
    cidade: '',
    uf: '',
    rua: '',
    numero: '',
    complemento: ''
  };

  ngOnInit() {
    this.carregarDados();
  }

  carregarDados() {
    const usuarioLogado = this.authService.getUsuarioLogado();
    this.meuDados.set(usuarioLogado);

    if (!usuarioLogado?.cpf) {
      this.errorMessage.set('Usuario nao identificado.');
      return;
    }

    this.preencherFormulario(usuarioLogado);

    this.clienteService.buscarCliente(usuarioLogado.cpf).subscribe({
      next: (cliente) => {
        this.meuDados.set(cliente);
        localStorage.setItem('usuario', JSON.stringify(cliente));
        this.preencherFormulario(cliente);
      },
      error: (error) => {
        console.error('Erro ao carregar perfil do cliente:', error);
        this.errorMessage.set('Nao foi possivel carregar os dados atualizados.');
      }
    });
  }

  preencherFormulario(cliente: Cliente) {
    this.editavelData = {
      nome: cliente.nome || '',
      email: cliente.email || '',
      salario: cliente.salario || 0,
      funcao: '',
      telefone: cliente.telefone || '',
      cep: cliente.endereco?.cep || '',
      cidade: cliente.endereco?.cidade || '',
      uf: cliente.endereco?.estado || '',
      rua: cliente.endereco?.logradouro || '',
      numero: cliente.endereco?.numero || '',
      complemento: cliente.endereco?.complemento || ''
    };
  }

  salvarDados() {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    // Validação básica
    if (!this.editavelData.nome || !this.editavelData.email ||
        !this.editavelData.telefone || !this.editavelData.cidade || 
        !this.editavelData.uf || !this.editavelData.rua || !this.editavelData.numero) {
      this.errorMessage.set('Por favor, preencha todos os campos obrigatorios.');
      return;
    }

    // Validar email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.editavelData.email)) {
      this.errorMessage.set('Email invalido.');
      return;
    }

    const clienteAtual = this.meuDados();

    if (!clienteAtual?.cpf) {
      this.errorMessage.set('Usuario nao identificado.');
      return;
    }

    const dadosAtualizados: ClienteUpdate = {
      cpf: clienteAtual.cpf,
      nome: this.editavelData.nome,
      email: this.editavelData.email,
      telefone: this.editavelData.telefone,
      salario: Number(this.editavelData.salario),
      endereco: {
        logradouro: this.editavelData.rua,
        numero: this.editavelData.numero,
        complemento: this.editavelData.complemento || null,
        cep: this.editavelData.cep.replace(/\D/g, ''),
        cidade: this.editavelData.cidade,
        estado: this.editavelData.uf.toUpperCase()
      }
    };

    if (!dadosAtualizados.endereco.cep || dadosAtualizados.endereco.cep.length !== 8) {
      this.errorMessage.set('CEP deve conter 8 digitos.');
      return;
    }

    if (!dadosAtualizados.salario || dadosAtualizados.salario <= 0) {
      this.errorMessage.set('Salario deve ser maior que zero.');
      return;
    }

    this.isSaving.set(true);
    this.clienteService.atualizarCliente(clienteAtual.cpf, dadosAtualizados)
      .pipe(finalize(() => {
        this.isSaving.set(false);
        this.step.set(1);
      }))
      .subscribe({
        next: (cliente) => {
          this.meuDados.set(cliente);
          localStorage.setItem('usuario', JSON.stringify(cliente));
          this.preencherFormulario(cliente);
          this.successMessage.set('Dados atualizados com sucesso.');
        },
        error: (error) => {
          console.error('Erro ao atualizar perfil do cliente:', error);
          this.errorMessage.set('Nao foi possivel atualizar os dados.');
        }
      });
  }

  nextStep() {
    if (this.step() < 4) this.step.update(step => step + 1);
  }

  prevStep() {
    if (this.step() > 1) this.step.update(step => step - 1);
  }
}
