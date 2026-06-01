import { Component, inject } from '@angular/core';
import { CashBalance } from '../../components/cash-balance/cash-balance';
import { Header } from '../../../../shared/components/header/header';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  imports: [CashBalance, Header, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  authService = inject(AuthService);
}
