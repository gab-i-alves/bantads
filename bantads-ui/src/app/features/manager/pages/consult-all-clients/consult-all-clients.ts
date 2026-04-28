import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HeaderManager } from '../../../../shared/components/header-manager/header-manager';
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

  authService = inject(AuthService);
  private router = inject(Router);


  searchTerm: string = '';

  get clientesFiltrados(): any[] {
    const resultado = 0 as any;

    return resultado
  }

  verCliente(cpf: string) {
    this.router.navigate(['/manager/consultar-cliente'], { queryParams: { cpf } });
  }
}