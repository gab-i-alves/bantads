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
  selector: 'app-consult-client',
  imports: [HeaderManager, FormsModule],
  templateUrl: './consult-client.html',
  styleUrl: './consult-client.css',
})
export class ConsultClient {

  clienteService = inject(ClientService);
  authService = inject(AuthService);

  clientes = this.clienteService.getClientesByGerente(this.authService.getUsuarioLogado().gerenteId);


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
