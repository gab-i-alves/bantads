import { Component, OnInit, inject, signal } from '@angular/core';
import { HeaderAdmin } from '../../../../shared/components/header-admin/header-admin';
import { AdminService } from '../../../../core/services/admin.service';
import { DashboardGerente } from '../../../../core/models/admin.model';

@Component({
  selector: 'app-dashboard',
  imports: [HeaderAdmin],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class DashboardAdmin implements OnInit {
  private adminService = inject(AdminService);

  topGerentes = signal<DashboardGerente[]>([]);
  isLoadingGerentes = signal(false);
  errorMessage = signal('');

  ngOnInit() {
    this.carregarDashboardGerentes();
  }

  carregarDashboardGerentes() {
    this.isLoadingGerentes.set(true);
    this.errorMessage.set('');

    this.adminService.listarDashboardGerentes().subscribe({
      next: (gerentes) => {
        this.topGerentes.set(gerentes);
        this.isLoadingGerentes.set(false);
      },
      error: (error) => {
        console.error('Erro ao buscar dashboard do admin:', error);
        this.errorMessage.set('Nao foi possivel carregar os dados dos gerentes.');
        this.isLoadingGerentes.set(false);
      }
    });
  }

  formatarMoeda(valor: number | null | undefined): string {
    // padrao brasileiro com separador de milhar; o template prefixa "R$ "
    return (valor || 0).toLocaleString('pt-BR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
  }
}
