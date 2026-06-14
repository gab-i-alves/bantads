import { Component, OnInit, inject, signal } from '@angular/core';
import { HeaderAdmin } from '../../../../shared/components/header-admin/header-admin';
import { NewManager } from '../../components/new-manager/new-manager';
import { EditManager } from '../../components/edit-manager/edit-manager';
import { AdminService } from '../../../../core/services/admin.service';
import { Gerente, GerenteCreate, GerenteUpdate } from '../../../../core/models/admin.model';

@Component({
  selector: 'app-manage-managers',
  imports: [HeaderAdmin, NewManager, EditManager],
  templateUrl: './manage-managers.html',
  styleUrl: './manage-managers.css',
})
export class ManageManagers implements OnInit {
  private adminService = inject(AdminService);

  gerentes = signal<Gerente[]>([]);
  isLoadingGerentes = signal(false);
  errorMessage = signal('');

  adicionarGerenteIsActive = signal(false);
  editarGerenteIsActive = signal(false);
  gerenteSelecionado = signal<Gerente | null>(null);

  ngOnInit() {
    this.carregarGerentes();
  }

  carregarGerentes() {
    this.isLoadingGerentes.set(true);
    this.errorMessage.set('');

    this.adminService.listarGerentes().subscribe({
      next: (gerentes) => {
        this.gerentes.set(gerentes.filter(gerente => gerente.role === 'GERENTE'));
        this.isLoadingGerentes.set(false);
      },
      error: (error) => {
        console.error('Erro ao buscar gerentes:', error);
        this.errorMessage.set('Nao foi possivel carregar os gerentes.');
        this.isLoadingGerentes.set(false);
      }
    });
  }

  toggleAdicionarGerente() {
    this.adicionarGerenteIsActive.set(true);
  }

  fecharModalAdicionar() {
    this.adicionarGerenteIsActive.set(false);
  }

  adicionarNovoGerente(gerente: GerenteCreate) {
    this.adminService.criarGerente(gerente).subscribe({
      next: () => {
        this.fecharModalAdicionar();
        this.carregarGerentes();
      },
      error: (error) => {
        console.error('Erro ao criar gerente:', error);
        alert('Nao foi possivel criar o gerente.');
      }
    });
  }

  toggleeditarGerente(gerente: Gerente) {
    this.gerenteSelecionado.set({ ...gerente });
    this.editarGerenteIsActive.set(true);
  }

  fecharModalEditar() {
    this.editarGerenteIsActive.set(false);
    this.gerenteSelecionado.set(null);
  }

  editarGerente(evento: { cpf: string; gerente: GerenteUpdate }) {
    this.adminService.atualizarGerente(evento.cpf, evento.gerente).subscribe({
      next: () => {
        this.fecharModalEditar();
        this.carregarGerentes();
      },
      error: (error) => {
        console.error('Erro ao atualizar gerente:', error);
        alert('Nao foi possivel atualizar o gerente.');
      }
    });
  }

  deletar(gerente: Gerente) {
    if (this.gerentes().length <= 1) {
      alert('Não é possível remover o último gerente do banco.');
      return;
    }

    const confirmar = confirm(`Deseja remover o gerente ${gerente.nome}?`);

    if (!confirmar) {
      return;
    }

    this.adminService.removerGerente(gerente.cpf).subscribe({
      next: () => {
        this.gerentes.update(gerentes =>
          gerentes.filter(item => item.cpf !== gerente.cpf)
        );
        alert('Remoção de gerente iniciada. As contas dele serão reatribuídas automaticamente.');
      },
      error: (error) => {
        console.error('Erro ao remover gerente:', error);
        alert('Nao foi possivel remover o gerente.');
      }
    });
  }

  iniciais(nome: string): string {
    const partes = nome.trim().split(/\s+/).slice(0, 2);

    if (!partes.length || !partes[0]) {
      return '--';
    }

    return partes.map(parte => parte.charAt(0)).join('').toUpperCase();
  }

}
