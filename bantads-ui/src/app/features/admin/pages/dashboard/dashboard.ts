import { Component } from '@angular/core';
import { HeaderAdmin } from '../../../../shared/components/header-admin/header-admin';

@Component({
  selector: 'app-dashboard',
  imports: [HeaderAdmin],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class DashboardAdmin {

  gerentes: any[] = [
    {
      "id": 1,
      "nome": "Carlos Almeida",
      "saldoPositivo": 1580000.50,
      "saldoNegativo": -45000.00,
      "clientes": 1200
    },
    {
      "id": 2,
      "nome": "Fernanda Souza",
      "saldoPositivo": 1325000.75,
      "saldoNegativo": -23000.40,
      "clientes": 1200
    },
    {
      "id": 3,
      "nome": "Ricardo Mendes",
      "saldoPositivo": 980450.10,
      "saldoNegativo": -125000.00,
      "clientes": 1200
    },
    {
      "id": 4,
      "nome": "Juliana Martins",
      "saldoPositivo": 870320.90,
      "saldoNegativo": -52000.30,
      "clientes": 1200
    },
    {
      "id": 5,
      "nome": "Lucas Pereira",
      "saldoPositivo": 640000.00,
      "saldoNegativo": -87000.15,
      "clientes": 1200
    },
    {
      "id": 6,
      "nome": "mq Sofia Carvalho",
      "saldoPositivo": 420300.45,
      "saldoNegativo": -12000.00,
      "clientes": 1200
    },
    {
      "id": 7,
      "nome": "Mariana sena",
      "saldoPositivo": 420300.45,
      "saldoNegativo": -12000.00,
      "clientes": 1200
    },
    {
      "id": 8,
      "nome": "Carlos Silva",
      "saldoPositivo": 420300.45,
      "saldoNegativo": -12000.00,
      "clientes": 1200
    },
  ]

  //gerentes com maiores saldos positivos
  topGerentes = this.gerentes.sort((a, b) => b.saldoPositivo - a.saldoPositivo);
}
