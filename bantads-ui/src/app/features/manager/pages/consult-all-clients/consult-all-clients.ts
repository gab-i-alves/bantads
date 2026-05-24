import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HeaderManager } from '../../../../shared/components/header-manager/header-manager';
import { AuthService } from '../../../../core/services/auth.service';
import { ClienteService } from '../../../../core/services/cliente.service';
import { Cliente } from '../../../../core/models/cliente.model';

@Component({
  selector: 'app-consult-all-clients',
  imports: [HeaderManager, FormsModule],
  templateUrl: './consult-all-clients.html',
  styleUrl: './consult-all-clients.css',
})
export class ConsultAllClients implements OnInit {

  authService = inject(AuthService);
  private router = inject(Router);
  private clienteService = inject(ClienteService);

  clientes = signal<Cliente[]>([]);
  isLoadingClientes = signal(false);
  errorMessage = signal('');

  searchTerm: string = '';

  clientesFiltrados = computed(() => {
    const termo = this.searchTerm.trim().toLowerCase();
    const cpfBusca = this.searchTerm.replace(/\D/g, '');
    const clientes = this.clientes();

    if (!termo) {
      return clientes;
    }

    return clientes.filter(cliente => {
      const cpfCliente = cliente.cpf.replace(/\D/g, '');
      return cliente.nome.toLowerCase().includes(termo) || cpfCliente.includes(cpfBusca);
    });
  });

  ngOnInit() {
    this.carregarClientes();
  }

  carregarClientes() {
    this.isLoadingClientes.set(true);
    this.errorMessage.set('');

    this.clienteService.listarClientes().subscribe({
      next: (clientes) => {
        this.clientes.set(clientes);
        this.isLoadingClientes.set(false);
      },
      error: (error) => {
        console.error('Erro ao buscar clientes:', error);
        this.errorMessage.set('Nao foi possivel carregar os clientes.');
        this.isLoadingClientes.set(false);
      }
    });
  }

  iniciais(nome: string): string {
    return nome
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map(parte => parte.charAt(0))
      .join('')
      .toUpperCase();
  }

  verCliente(cpf: string) {
    this.router.navigate(['/manager/consultar-cliente'], { queryParams: { cpf } });
  }
}
