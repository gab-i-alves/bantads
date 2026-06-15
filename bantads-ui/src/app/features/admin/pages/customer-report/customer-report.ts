import { Component, ElementRef, OnInit, ViewChild, inject, signal } from '@angular/core';
import { HeaderAdmin } from '../../../../shared/components/header-admin/header-admin';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../../core/services/admin.service';
import { RelatorioCliente } from '../../../../core/models/admin.model';

@Component({
  selector: 'app-customer-report',
  imports: [HeaderAdmin, FormsModule],
  templateUrl: './customer-report.html',
  styleUrl: './customer-report.css',
})
export class CustomerReport implements OnInit {
  @ViewChild('tableContainer') tableContainer?: ElementRef<HTMLDivElement>;

  private adminService = inject(AdminService);

  clientes = signal<RelatorioCliente[]>([]);
  isLoadingClientes = signal(false);
  errorMessage = signal('');

  searchTerm: string = '';

  ngOnInit() {
    this.carregarRelatorioClientes();
  }

  carregarRelatorioClientes() {
    this.isLoadingClientes.set(true);
    this.errorMessage.set('');

    this.adminService.listarRelatorioClientes().subscribe({
      next: (clientes) => {
        this.clientes.set(clientes);
        this.isLoadingClientes.set(false);
      },
      error: (error) => {
        console.error('Erro ao buscar relatorio de clientes:', error);
        this.errorMessage.set('Nao foi possivel carregar o relatorio de clientes.');
        this.isLoadingClientes.set(false);
      }
    });
  }

  get clientesFiltrados(): RelatorioCliente[] {
    const resultado = this.searchTerm.trim()
      ? this.clientes().filter(cliente =>
        cliente.nome.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        cliente.cpf.includes(this.searchTerm)
      )
      : this.clientes();

    return [...resultado].sort((a, b) => a.nome.localeCompare(b.nome));
  }

  iniciais(nome: string): string {
    const partes = nome.trim().split(/\s+/).slice(0, 2);

    if (!partes.length || !partes[0]) {
      return '--';
    }

    return partes.map(parte => parte.charAt(0)).join('').toUpperCase();
  }

  formatarMoeda(valor: number | string | null | undefined): string {
    const numero = Number(valor || 0);

    // Padrao brasileiro com separador de milhar (NF15).
    return new Intl.NumberFormat('pt-BR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(Number.isFinite(numero) ? numero : 0);
  }

  rolarTabela(direcao: 'esquerda' | 'direita') {
    const container = this.tableContainer?.nativeElement;

    if (!container) {
      return;
    }

    const deslocamento = direcao === 'direita' ? 420 : -420;

    container.scrollBy({
      left: deslocamento,
      behavior: 'smooth',
    });
  }
}
