import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HeaderManager } from '../../../../shared/components/header-manager/header-manager';
import { ClientService } from '../../../../core/services/client.service';
import { AuthService } from '../../../../core/services/auth.service';

type Cliente = {
  cpf: string;
  nome: string;
  cidade: string;
  uf: string;
  salario: number;
  saldo: number;
  credito: number;
  aprovado: boolean | null;
};
@Component({
  selector: 'app-consult-all-clients',
  imports: [HeaderManager, FormsModule],
  templateUrl: './consult-all-clients.html',
  styleUrl: './consult-all-clients.css',
})
export class ConsultAllClients {

  clienteService = inject(ClientService);
  authService = inject(AuthService);

  clientes = this.clienteService.getClientesByGerente(this.authService.getUsuarioLogado().gerenteId);

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