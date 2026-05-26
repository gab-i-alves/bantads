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

  searchTerm = signal('');

  clientesFiltrados = computed(() => {
    const busca = this.searchTerm();
    const termo = this.normalizarTexto(busca);
    const cpfBusca = this.normalizarCpf(busca);
    const clientes = this.clientes();

    if (!termo) {
      return clientes;
    }

    return clientes.filter(cliente => {
      const nomeCliente = this.normalizarTexto(cliente.nome);
      const cpfCliente = this.normalizarCpf(cliente.cpf);

      return nomeCliente.includes(termo) || (cpfBusca !== '' && cpfCliente.includes(cpfBusca));
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

  private normalizarTexto(valor: string): string {
    return valor
      .trim()
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }

  private normalizarCpf(valor: string): string {
    return valor.replace(/\D/g, '');
  }

  verCliente(cpf: string) {
    this.router.navigate(['/manager/consultar-cliente'], { queryParams: { cpf } });
  }
}
