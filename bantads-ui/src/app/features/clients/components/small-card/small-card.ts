import { Component, inject } from '@angular/core';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-small-card',
  imports: [],
  templateUrl: './small-card.html',
  styleUrl: './small-card.css',
})
export class SmallCard {

  authService = inject(AuthService);
}
