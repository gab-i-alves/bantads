import { Component, inject } from '@angular/core';
import { Header } from '../../../../shared/components/header/header';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-profile',
  imports: [Header],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile {

  authService = inject(AuthService);

  meuDados = this.authService.getUsuarioLogado();

  ngOnInit() {
    console.log(this.meuDados);
  }

  step: number = 1;

  nextStep() {
    if (this.step < 4) this.step++;
  }

  prevStep() {
    if (this.step > 1) this.step--;
  }
}
