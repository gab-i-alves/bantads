import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';
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
  private notify = inject(NotificationService);

  // Login
  email: string = '';
  senha: string = '';
  isRegistering: boolean = false;
  // Estado de loading do login para desabilitar o botao enquanto a chamada esta em voo.
  isLogando = signal(false);

  // Registro - dados coletados (salario como texto mascarado, NF15)
  registroData = {
    nome: '',
    email: '',
    cpf: '',
    salario: '',
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
    if (this.isLogando()) {
      return;
    }

    if (!this.email.trim() || !this.senha) {
      this.notify.erro('Informe email e senha para entrar.');
      return;
    }

    this.isLogando.set(true);
    this.authService.login(this.email, this.senha)
      .pipe(finalize(() => this.isLogando.set(false)))
      .subscribe({
        next: (response) => {
          this.authService.handleLoginSuccess(response);
          this.notify.sucesso('Login realizado com sucesso.');
        },
        error: (error) => {
          console.error('Erro no login:', error);
          this.notify.erro(this.notify.mensagemDeErroHttp(error, 'Falha no login. Verifique suas credenciais e tente novamente.'));
        }
      });
  }

  registrar() {
    if (this.isRegistering) {
      return;
    }

    if (!this.registroData.nome || !this.registroData.email || !this.registroData.cpf ||
        !this.registroData.telefone || !this.registroData.cep || !this.registroData.cidade ||
        !this.registroData.uf || !this.registroData.rua || !this.registroData.numero ||
        !this.registroData.salario) {
      this.notify.erro('Por favor, preencha todos os campos obrigatorios!');
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.registroData.email)) {
      this.notify.erro('Email invalido!');
      return;
    }

    const cpfRegex = /^\d{3}\.\d{3}\.\d{3}-\d{2}$/;
    if (!cpfRegex.test(this.registroData.cpf)) {
      this.notify.erro('CPF deve estar no formato: 000.000.000-00');
      return;
    }

    const cep = this.onlyDigits(this.registroData.cep);
    if (cep.length !== 8) {
      this.notify.erro('CEP deve conter 8 digitos.');
      return;
    }

    const salario = this.parseMoeda(this.registroData.salario);
    if (!Number.isFinite(salario) || salario <= 0) {
      this.notify.erro('Salario deve ser maior que zero.');
      return;
    }

    const estado = this.registroData.uf.trim().toUpperCase();
    if (!/^[A-Z]{2}$/.test(estado)) {
      this.notify.erro('Estado deve ser informado como UF com 2 letras. Exemplo: PR.');
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

    this.authService.autocadastro(cliente)
      .pipe(finalize(() => this.isRegistering = false))
      .subscribe({
        next: () => {
          this.notify.sucesso('Cadastro enviado com sucesso! Aguarde a aprovacao de um gerente.');
          this.resetRegistro();
          this.isLogin = true;
          this.step = 1;
        },
        error: (error) => {
          console.error('Erro no autocadastro:', error);
          this.notify.erro(this.getCadastroErrorMessage(error));
        }
      });
  }

  ufs = ['AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS',
    'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'];

  formatCpf(value: string) {
    this.registroData.cpf = this.onlyDigits(value)
      .slice(0, 11)
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
  }

  formatTelefone(value: string) {
    this.registroData.telefone = this.onlyDigits(value)
      .slice(0, 11)
      .replace(/(\d{2})(\d)/, '($1) $2')
      .replace(/(\d{4,5})(\d{4})$/, '$1-$2');
  }

  // NF16: mascara de CEP (00000-000).
  formatCep(value: string) {
    this.registroData.cep = this.onlyDigits(value)
      .slice(0, 8)
      .replace(/(\d{5})(\d{1,3})$/, '$1-$2');
  }

  // NF15: salario como texto mascarado em Real, guardado como string formatada.
  formatSalario(value: string) {
    this.registroData.salario = this.maskMoeda(value);
  }

  private maskMoeda(value: string): string {
    const digitos = this.onlyDigits(value);
    if (!digitos) {
      return '';
    }

    const numero = Number(digitos) / 100;
    return numero.toLocaleString('pt-BR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
  }

  private parseMoeda(value: string | number): number {
    if (typeof value === 'number') {
      return value;
    }
    const normalizado = value.replace(/\./g, '').replace(',', '.').replace(/[^\d.-]/g, '');
    return Number(normalizado);
  }

  private onlyDigits(value: string): string {
    return value.replace(/\D/g, '');
  }

  private resetRegistro() {
    this.registroData = {
      nome: '',
      email: '',
      cpf: '',
      salario: '',
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
      return 'Não foi possível conectar ao servidor. Tente novamente em instantes.';
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
