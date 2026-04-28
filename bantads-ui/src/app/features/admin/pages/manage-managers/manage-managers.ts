import { Component, inject, signal } from '@angular/core';
import { HeaderAdmin } from '../../../../shared/components/header-admin/header-admin';
import { NewManager } from '../../components/new-manager/new-manager';
import { EditManager } from '../../components/edit-manager/edit-manager';

type Gerente = {
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  senha: string;
}

@Component({
  selector: 'app-manage-managers',
  imports: [HeaderAdmin, NewManager, EditManager],
  templateUrl: './manage-managers.html',
  styleUrl: './manage-managers.css',
})
export class ManageManagers {

 



  deletar(idGerente: number) {
    alert('deletado com sucesso');
  }

  adicionarGerenteIsActive = signal(false);

  editarGerenteIsActive = signal(false);
  selectedGerente = signal<Gerente | null>(null);

  toggleAdicionarGerente() {
    this.adicionarGerenteIsActive.set(true);
  }

  fecharModalAdicionar() {
    this.adicionarGerenteIsActive.set(false);
  }

  adicionarNovoGerente(gerente: Gerente) {

  }


  gerenteSelecionado: any = null;

  toggleeditarGerente(gerente: Gerente) {
    this.gerenteSelecionado = { ...gerente };
    this.editarGerenteIsActive.set(true);
  }

  fecharModalEditar() {
    this.editarGerenteIsActive.set(false);
  }

}
