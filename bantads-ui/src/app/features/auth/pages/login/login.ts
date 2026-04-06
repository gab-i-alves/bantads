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

  email: string = '';
  senha: string = '';

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




  isLogin: boolean = true;

  trocarEstado() {
    this.isLogin = !this.isLogin
  }

  step: number = 1;

  nextStep() {
    if (this.step < 4) this.step++;
  }

  prevStep() {
    if (this.step > 1) this.step--;
  }
}
