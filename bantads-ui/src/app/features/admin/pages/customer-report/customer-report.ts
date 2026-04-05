import { Component } from '@angular/core';
import { HeaderAdmin } from '../../../../shared/components/header-admin/header-admin';
import { FormsModule } from '@angular/forms';

type Cliente = {
  cpf: string;
  nome: string;
  email: string;
  cidade: string;
  estado: string;
  salario: number;
  numeroConta: string;
  saldoConta: number;
  limiteConta: number;
  cpfGerente: string;
  nomeGerente: string;
  aprovado: boolean;
}
@Component({
  selector: 'app-customer-report',
  imports: [HeaderAdmin, FormsModule],
  templateUrl: './customer-report.html',
  styleUrl: './customer-report.css',
})
export class CustomerReport {
  clientes: Cliente[] = [
    {
      cpf: "123.456.789-00",
      nome: "João Silva",
      email: "joao.silva@email.com",
      cidade: "Curitiba",
      estado: "PR",
      salario: 5000,
      numeroConta: "1001-0",
      saldoConta: 3200,
      limiteConta: 10000,
      cpfGerente: "111.222.333-44",
      nomeGerente: "Carlos Mendes",
      aprovado: true
    },
    {
      cpf: "987.654.321-11",
      nome: "Maria Oliveira",
      email: "maria.oliveira@email.com",
      cidade: "São Paulo",
      estado: "SP",
      salario: 12450,
      numeroConta: "1002-5",
      saldoConta: 8400,
      limiteConta: 20000,
      cpfGerente: "111.222.333-44",
      nomeGerente: "Carlos Mendes",
      aprovado: true
    },
    {
      cpf: "456.123.789-99",
      nome: "Ricardo Costa",
      email: "ricardo.costa@email.com",
      cidade: "Florianópolis",
      estado: "SC",
      salario: 3200,
      numeroConta: "1003-8",
      saldoConta: 1500,
      limiteConta: 5000,
      cpfGerente: "555.666.777-88",
      nomeGerente: "Fernanda Rocha",
      aprovado: true
    },
    {
      cpf: "321.654.987-22",
      nome: "Ana Amaral",
      email: "ana.amaral@email.com",
      cidade: "Belo Horizonte",
      estado: "MG",
      salario: 8900,
      numeroConta: "1004-2",
      saldoConta: 4200,
      limiteConta: 12000,
      cpfGerente: "555.666.777-88",
      nomeGerente: "Fernanda Rocha",
      aprovado: true
    },
    {
      cpf: "741.852.963-00",
      nome: "Fernando Pereira",
      email: "fernando.pereira@email.com",
      cidade: "Porto Alegre",
      estado: "RS",
      salario: 21000,
      numeroConta: "1005-9",
      saldoConta: 18000,
      limiteConta: 50000,
      cpfGerente: "999.888.777-66",
      nomeGerente: "Roberto Almeida",
      aprovado: true
    }
  ];

  searchTerm: string = '';

  get clientesFiltrados(): Cliente[] {
    const resultado = this.searchTerm.trim()
      ? this.clientes.filter(cliente =>
        cliente.nome.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        cliente.cpf.includes(this.searchTerm)
      )
      : this.clientes;

    return resultado.sort((a, b) => a.nome.localeCompare(b.nome));
  }
}
