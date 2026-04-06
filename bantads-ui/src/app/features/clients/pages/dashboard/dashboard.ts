import { Component, inject } from '@angular/core';
import { CashBalance } from '../../components/cash-balance/cash-balance';
import { SmallCard } from '../../components/small-card/small-card';
import { Header } from '../../../../shared/components/header/header';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { ClientService } from '../../../../core/services/client.service';

@Component({
  selector: 'app-dashboard',
  imports: [CashBalance, SmallCard, Header, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {


  authService = inject(AuthService);
  clientService = inject(ClientService);
  meusDados = this.authService.getUsuarioLogado();

  dadosAtualizados: any;

  ngOnInit() {
    this.dadosAtualizados = this.clientService.getClienteById(this.meusDados.idCliente);
  }
}
