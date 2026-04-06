import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { ClientService } from '../../../../core/services/client.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {

  authService = inject(AuthService);
  clienteService = inject(ClientService)
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

    const resultado = this.authService.login(this.email, this.senha);
    if (resultado) {
      alert('Login successful!');

      // Redireciona baseado no tipo de usuário
      if (resultado.tipo === 'gerente') {
        this.routerlink.navigate(['/manager/dashboard']);
      } else if (resultado.tipo === 'cliente') {
        this.routerlink.navigate(['/client/dashboard']);
      } else if (resultado.tipo === 'admin') {
        this.routerlink.navigate(['/admin/dashboard']);
      }
    } else {
      alert('dados incorretos, tente novamente!');
    }
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

    try {
      // Chamar autoCadastro com os dados coletados
      const novoCliente = {
        nome: this.registroData.nome,
        email: this.registroData.email,
        cpf: this.registroData.cpf,
        salario: this.registroData.salario,
        funcao: this.registroData.funcao,
        telefone: this.registroData.telefone,
        cep: this.registroData.cep,
        cidade: this.registroData.cidade,
        uf: this.registroData.uf,
        rua: this.registroData.rua,
        numero: this.registroData.numero,
        complemento: this.registroData.complemento,
        saldo: 0,
        credito: 0,
        senha: '123456'
      }

      this.clienteService.PostCliente(novoCliente);

      alert('Cadastro realizado com sucesso! aguarde aprovação!');
      window.location.reload();
      
      //this.routerlink.navigate(['/client/dashboard']);

    } catch (error: any) {
      alert('Erro ao realizar cadastro: ' + error.message);
    }
  }

  isLogin: boolean = true;

  trocarEstado() {
    this.isLogin = !this.isLogin;
    // Reset do formulário ao trocar de estado
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
