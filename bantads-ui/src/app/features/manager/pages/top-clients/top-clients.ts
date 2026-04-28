import { Component, inject } from '@angular/core';
import { HeaderManager } from '../../../../shared/components/header-manager/header-manager';

@Component({
  selector: 'app-top-clients',
  imports: [HeaderManager],
  templateUrl: './top-clients.html',
  styleUrl: './top-clients.css',
})

export class TopClients {



  topClientes: any[] = [];

}
