import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
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
  selector: 'app-consult-client',
  imports: [HeaderManager, FormsModule],
  templateUrl: './consult-client.html',
  styleUrl: './consult-client.css',
})
export class ConsultClient {

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

  searchTerm: string = '';

  get clienteSelecionado(): Cliente | null {
    if (!this.searchTerm.trim()) {
      return null;
    }

    const termo = this.searchTerm.trim().toLowerCase();
    const cpfSemPontuacao = this.searchTerm.replace(/\D/g, '');

    return this.clientes.find(cliente => {
      const cpfClienteSemPontuacao = cliente.cpf.replace(/\D/g, '');
      return (
        cliente.nome.toLowerCase() === termo ||
        cpfClienteSemPontuacao === cpfSemPontuacao
      );
    }) || null;
  }
}
