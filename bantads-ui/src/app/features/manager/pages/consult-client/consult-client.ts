import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { HeaderManager } from '../../../../shared/components/header-manager/header-manager';
import { AuthService } from '../../../../core/services/auth.service';
import { ClienteService } from '../../../../core/services/cliente.service';
import { Cliente } from '../../../../core/models/cliente.model';

@Component({
  selector: 'app-consult-client',
  imports: [HeaderManager, FormsModule],
  templateUrl: './consult-client.html',
  styleUrl: './consult-client.css',
})
export class ConsultClient implements OnInit {

  authService = inject(AuthService);
  private route = inject(ActivatedRoute);
  private clienteService = inject(ClienteService);

  searchTerm = signal('');
  clientes = signal<Cliente[]>([]);
  isLoadingClientes = signal(false);
  errorMessage = signal('');

  clienteSelecionado = computed(() => {
    const busca = this.searchTerm();
    const termo = this.normalizarTexto(busca);
    const cpfBusca = this.normalizarCpf(busca);

    if (!termo) {
      return null;
    }

    return this.clientes().find(cliente => {
      const nomeCliente = this.normalizarTexto(cliente.nome);
      const cpfCliente = this.normalizarCpf(cliente.cpf);

      return nomeCliente.includes(termo) || (cpfBusca !== '' && cpfCliente.includes(cpfBusca));
    }) || null;
  });

  ngOnInit() {
    this.carregarClientes();

    this.route.queryParams.subscribe(params => {
      if (params['cpf']) {
        this.searchTerm.set(params['cpf']);
      }
    });
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

  cidadeCliente(cliente: Cliente): string {
    return cliente.endereco?.cidade || cliente.cidade || '--';
  }

  estadoCliente(cliente: Cliente): string {
    return cliente.endereco?.estado || cliente.uf || '--';
  }

  formatarMoeda(valor?: number): string {
    return `R$ ${(valor || 0).toLocaleString('pt-BR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;
  }

  limparBusca() {
    this.searchTerm.set('');
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
}
