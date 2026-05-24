import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-header',
  imports: [RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {


  authService = inject(AuthService);
  meusDados = this.authService.getUsuarioLogado();

  get iniciais(): string {
    return this.meusDados?.nome
      ?.trim()
      .split(/\s+/)
      .slice(0, 2)
      .map(parte => parte.charAt(0))
      .join('')
      .toUpperCase() || 'C';
  }

  logout(){
    this.authService.logout();
  }

}
