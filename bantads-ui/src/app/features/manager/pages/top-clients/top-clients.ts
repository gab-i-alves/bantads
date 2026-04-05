import { Component } from '@angular/core';
import { HeaderManager } from '../../../../shared/components/header-manager/header-manager';

type Cliente = {
  cpf: string;
  nome: string;
  cidade: string;
  estado: string;
  salario: number;
  saldoConta: number;
  limiteConta: number;
  aprovado: boolean | null;
};

@Component({
  selector: 'app-top-clients',
  imports: [HeaderManager],
  templateUrl: './top-clients.html',
  styleUrl: './top-clients.css',
})

export class TopClients {
    clientes: Cliente[] = [
    {
      cpf: "123.456.789-00",
      nome: "João Silva",
      cidade: "Curitiba",
      estado: "PR",
      salario: 5000,
      saldoConta: 3200,
      limiteConta: 10000,
      aprovado: true
    },
    {
      cpf: "987.654.321-11",
      nome: "Maria Oliveira",
      cidade: "São Paulo",
      estado: "SP",
      salario: 12450,
      saldoConta: 8400,
      limiteConta: 20000,
      aprovado: true
    },
    {
      cpf: "456.123.789-99",
      nome: "Ricardo Costa",
      cidade: "Florianópolis",
      estado: "SC",
      salario: 3200,
      saldoConta: 1500,
      limiteConta: 5000,
      aprovado: true
    },
    {
      cpf: "321.654.987-22",
      nome: "Ana Amaral",
      cidade: "Belo Horizonte",
      estado: "MG",
      salario: 8900,
      saldoConta: 4200,
      limiteConta: 12000,
      aprovado: true
    },
    {
      cpf: "741.852.963-00",
      nome: "Fernando Pereira",
      cidade: "Porto Alegre",
      estado: "RS",
      salario: 21000,
      saldoConta: 18000,
      limiteConta: 50000,
      aprovado: true
    }
  ];

  topClientes: Cliente[] = [];

  constructor() {
    this.topClientes = this.clientes.sort((a, b) => b.saldoConta - a.saldoConta).slice(0, 3);
    
  }
}
