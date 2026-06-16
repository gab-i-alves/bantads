import { Component, OnInit, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { HeaderAdmin } from '../../../../shared/components/header-admin/header-admin';
import { NewManager } from '../../components/new-manager/new-manager';
import { EditManager } from '../../components/edit-manager/edit-manager';
import { AdminService } from '../../../../core/services/admin.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Gerente, GerenteCreate, GerenteUpdate } from '../../../../core/models/admin.model';

@Component({
  selector: 'app-manage-managers',
  imports: [HeaderAdmin, NewManager, EditManager],
  templateUrl: './manage-managers.html',
  styleUrl: './manage-managers.css',
})
export class ManageManagers implements OnInit {
  private adminService = inject(AdminService);
  private notify = inject(NotificationService);

  gerentes = signal<Gerente[]>([]);
  isLoadingGerentes = signal(false);
  errorMessage = signal('');

  adicionarGerenteIsActive = signal(false);
  editarGerenteIsActive = signal(false);
  gerenteSelecionado = signal<Gerente | null>(null);
  // Loading das operacoes de criar/editar/remover gerente para travar os botoes correspondentes.
  isSalvandoGerente = signal(false);
  // CPF do gerente em remocao, para mostrar o estado de loading apenas na linha certa.
  removendoCpf = signal<string | null>(null);

  ngOnInit() {
    this.carregarGerentes();
  }

  carregarGerentes() {
    this.isLoadingGerentes.set(true);
    this.errorMessage.set('');

    this.adminService.listarGerentes().subscribe({
      next: (gerentes) => {
        // o gateway (GET /gerentes) ja devolve so gerentes (filtra role no servidor)
        // e expoe o papel como `tipo`, nao `role` - por isso nao filtramos aqui.
        this.gerentes.set(gerentes);
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
    if (this.isSalvandoGerente()) {
      return;
    }

    this.isSalvandoGerente.set(true);
    this.adminService.criarGerente(gerente)
      .pipe(finalize(() => this.isSalvandoGerente.set(false)))
      .subscribe({
        next: () => {
          this.fecharModalAdicionar();
          this.notify.sucesso('Gerente criado com sucesso.');
          this.carregarGerentes();
        },
        error: (error) => {
          console.error('Erro ao criar gerente:', error);
          this.notify.erroHttp(error, 'Nao foi possivel criar o gerente.');
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
    if (this.isSalvandoGerente()) {
      return;
    }

    this.isSalvandoGerente.set(true);
    this.adminService.atualizarGerente(evento.cpf, evento.gerente)
      .pipe(finalize(() => this.isSalvandoGerente.set(false)))
      .subscribe({
        next: () => {
          this.fecharModalEditar();
          this.notify.sucesso('Gerente atualizado com sucesso.');
          this.carregarGerentes();
        },
        error: (error) => {
          console.error('Erro ao atualizar gerente:', error);
          this.notify.erroHttp(error, 'Nao foi possivel atualizar o gerente.');
        }
      });
  }

  deletar(gerente: Gerente) {
    if (this.removendoCpf()) {
      return;
    }

    if (this.gerentes().length <= 1) {
      this.notify.erro('Nao e possivel remover o ultimo gerente do banco.');
      return;
    }

    const confirmar = confirm(`Deseja remover o gerente ${gerente.nome}?`);

    if (!confirmar) {
      return;
    }

    this.removendoCpf.set(gerente.cpf);
    this.adminService.removerGerente(gerente.cpf)
      .pipe(finalize(() => this.removendoCpf.set(null)))
      .subscribe({
        next: () => {
          this.gerentes.update(gerentes =>
            gerentes.filter(item => item.cpf !== gerente.cpf)
          );
          this.notify.sucesso('Remocao de gerente iniciada. As contas dele serao reatribuidas automaticamente.');
        },
        error: (error) => {
          console.error('Erro ao remover gerente:', error);
          this.notify.erroHttp(error, 'Nao foi possivel remover o gerente.');
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

  formatarTelefone(telefone?: string): string {
    const digitos = (telefone || '').replace(/\D/g, '');

    if (!digitos) {
      return '--';
    }

    return digitos
      .slice(0, 11)
      .replace(/(\d{2})(\d)/, '($1) $2')
      .replace(/(\d{4,5})(\d{4})$/, '$1-$2');
  }

  // CPF mascarado (000.000.000-00); padStart preserva zero a esquerda.
  formatarCpf(cpf?: string): string {
    const digitos = String(cpf ?? '').replace(/\D/g, '');
    if (!digitos) {
      return '--';
    }
    return digitos.padStart(11, '0').replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }

}
