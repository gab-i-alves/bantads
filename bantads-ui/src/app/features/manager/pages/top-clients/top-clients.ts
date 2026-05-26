import { Component, OnInit, inject, signal } from '@angular/core';
import { HeaderManager } from '../../../../shared/components/header-manager/header-manager';
import { ClienteService } from '../../../../core/services/cliente.service';
import { MelhorCliente } from '../../../../core/models/cliente.model';

@Component({
  selector: 'app-top-clients',
  imports: [HeaderManager],
  templateUrl: './top-clients.html',
  styleUrl: './top-clients.css',
})

export class TopClients implements OnInit {

  private clienteService = inject(ClienteService);

  topClientes = signal<MelhorCliente[]>([]);
  isLoadingClientes = signal(false);
  errorMessage = signal('');

  ngOnInit() {
    this.carregarMelhoresClientes();
  }

  carregarMelhoresClientes() {
    this.isLoadingClientes.set(true);
    this.errorMessage.set('');

    this.clienteService.listarMelhoresClientes().subscribe({
      next: (clientes) => {
        this.topClientes.set(clientes);
        this.isLoadingClientes.set(false);
      },
      error: (error) => {
        console.error('Erro ao buscar melhores clientes:', error);
        this.errorMessage.set('Nao foi possivel carregar os melhores clientes.');
        this.isLoadingClientes.set(false);
      }
    });
  }

  iniciais(nome: string | null): string {
    if (!nome) {
      return '--';
    }

    return nome
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map(parte => parte.charAt(0))
      .join('')
      .toUpperCase();
  }

  formatarMoeda(valor: number | null | undefined): string {
    return (valor || 0).toFixed(2).replace('.', ',');
  }

}
