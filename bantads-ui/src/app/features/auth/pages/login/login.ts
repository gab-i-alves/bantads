import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { ClienteCreate } from '../../../../core/models/cliente.model';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {

  authService = inject(AuthService);
  routerlink = inject(Router);

  // Login
  email: string = '';
  senha: string = '';
  isRegistering: boolean = false;

  // Registro - dados coletados
  registroData = {
    nome: '',
    email: '',
    cpf: '',
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

  login() {
    console.log('Email:', this.email);
    console.log('Senha:', this.senha);

    this.authService.login(this.email, this.senha).subscribe({
      next: (response) => {
        console.log('Login bem-sucedido:', response);
        this.authService.handleLoginSuccess(response);
      },
      error: (error) => {
        console.error('Erro no login:', error);
        alert('Falha no login. Verifique suas credenciais e tente novamente.');
      }
    });
  }

  registrar() {
    if (!this.registroData.nome || !this.registroData.email || !this.registroData.cpf ||
        !this.registroData.telefone || !this.registroData.cep || !this.registroData.cidade ||
        !this.registroData.uf || !this.registroData.rua || !this.registroData.numero ||
        !this.registroData.salario) {
      alert('Por favor, preencha todos os campos obrigatorios!');
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.registroData.email)) {
      alert('Email invalido!');
      return;
    }

    const cpfRegex = /^\d{3}\.\d{3}\.\d{3}-\d{2}$/;
    if (!cpfRegex.test(this.registroData.cpf)) {
      alert('CPF deve estar no formato: 000.000.000-00');
      return;
    }

    const cep = this.onlyDigits(this.registroData.cep);
    if (cep.length !== 8) {
      alert('CEP deve conter 8 digitos.');
      return;
    }

    const salario = Number(this.registroData.salario);
    if (!Number.isFinite(salario) || salario <= 0) {
      alert('Salario deve ser maior que zero.');
      return;
    }

    const estado = this.registroData.uf.trim().toUpperCase();
    if (!/^[A-Z]{2}$/.test(estado)) {
      alert('Estado deve ser informado como UF com 2 letras. Exemplo: PR.');
      return;
    }

    const cliente: ClienteCreate = {
      nome: this.registroData.nome.trim(),
      email: this.registroData.email.trim(),
      cpf: this.onlyDigits(this.registroData.cpf),
      telefone: this.registroData.telefone.trim(),
      salario,
      endereco: {
        logradouro: this.registroData.rua.trim(),
        numero: this.registroData.numero.trim(),
        complemento: this.registroData.complemento?.trim() || null,
        cep,
        cidade: this.registroData.cidade.trim(),
        estado,
      }
    };

    this.isRegistering = true;

    this.authService.autocadastro(cliente).subscribe({
      next: () => {
        alert('Cadastro enviado com sucesso! Aguarde a aprovacao de um gerente.');
        this.resetRegistro();
        this.isLogin = true;
        this.step = 1;
      },
      error: (error) => {
        console.error('Erro no autocadastro:', error);
        alert(this.getCadastroErrorMessage(error));
        this.isRegistering = false;
      },
      complete: () => {
        this.isRegistering = false;
      }
    });
  }

  private onlyDigits(value: string): string {
    return value.replace(/\D/g, '');
  }

  private resetRegistro() {
    this.registroData = {
      nome: '',
      email: '',
      cpf: '',
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
  }

  private getCadastroErrorMessage(error: any): string {
    if (error?.status === 409) {
      return 'CPF ja cadastrado ou aguardando aprovacao.';
    }

    if (error?.status === 400) {
      const details = error?.error?.fields;
      if (details && typeof details === 'object') {
        return Object.entries(details)
          .map(([field, message]) => `${field}: ${message}`)
          .join('\n');
      }

      return error?.error?.error || 'Verifique os dados informados e tente novamente.';
    }

    if (error?.status === 0) {
      return 'Nao foi possivel conectar ao servidor. Verifique se o gateway esta rodando.';
    }

    return error?.error?.error || 'Erro ao realizar cadastro. Tente novamente.';
  }

  isLogin: boolean = true;

  trocarEstado() {
    this.isLogin = !this.isLogin;
    this.step = 1;
  }

  step: number = 1;

  nextStep() {
    if (this.step < 4) this.step++;
  }

  prevStep() {
    if (this.step > 1) this.step--;
  }
}
