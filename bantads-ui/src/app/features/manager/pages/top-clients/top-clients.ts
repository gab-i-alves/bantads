import { Component, inject } from '@angular/core';
import { HeaderManager } from '../../../../shared/components/header-manager/header-manager';
import { ClientService } from '../../../../core/services/client.service';


@Component({
  selector: 'app-top-clients',
  imports: [HeaderManager],
  templateUrl: './top-clients.html',
  styleUrl: './top-clients.css',
})

export class TopClients {


  clienteService = inject(ClientService)
  clientes = this.clienteService.getClientes();

  topClientes: any[] = [];

  constructor() {
    this.topClientes = this.clientes.sort((a, b) => b.saldo - a.saldo).slice(0, 3);
  }
}
