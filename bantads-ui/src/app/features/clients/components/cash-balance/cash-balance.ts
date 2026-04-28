import { Component, inject } from '@angular/core';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-cash-balance',
  imports: [],
  templateUrl: './cash-balance.html',
  styleUrl: './cash-balance.css',
})
export class CashBalance {


  // authService = inject(AuthService);
  // clientService = inject(ClientService);
  // meusDados = this.authService.getUsuarioLogado();

  // dadosAtualizados: any;

  // ngOnInit() {
  //   this.dadosAtualizados = this.clientService.getClienteById(this.meusDados.idCliente);
  // }
}
