import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-header-manager',
  imports: [RouterLink],
  templateUrl: './header-manager.html',
  styleUrl: './header-manager.css',
})
export class HeaderManager {
  authService = inject(AuthService);


  //meusDados = this.authService.getUsuarioLogado()

}
