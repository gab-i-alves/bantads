import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

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
      },
      error: (error) => {
        console.error('Erro no login:', error);
        alert('Falha no login. Verifique suas credenciais e tente novamente.');
      }
    });
  }

  registrar() {
    // Validação básica
    if (!this.registroData.nome || !this.registroData.email || !this.registroData.cpf ||
        !this.registroData.telefone || !this.registroData.cidade || !this.registroData.uf ||
        !this.registroData.rua || !this.registroData.numero) {
      alert('Por favor, preencha todos os campos obrigatórios!');
      return;
    }

    // Validar email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.registroData.email)) {
      alert('Email inválido!');
      return;
    }

    // Validar CPF (formato básico)
    const cpfRegex = /^\d{3}\.\d{3}\.\d{3}-\d{2}$/;
    if (!cpfRegex.test(this.registroData.cpf)) {
      alert('CPF deve estar no formato: 000.000.000-00');
      return;
    }
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
