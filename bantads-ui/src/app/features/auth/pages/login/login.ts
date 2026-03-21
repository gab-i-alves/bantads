import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {


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
