import { Component, signal } from '@angular/core';
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
  gerentes: Gerente[] = [
    {
      nome: "Carlos Mendes",
      cpf: "111.222.333-44",
      email: "carlos.mendes@banco.com",
      telefone: "(41) 98888-1111",
      senha: "Gerente@123"
    },
    {
      nome: "Fernanda Rocha",
      cpf: "555.666.777-88",
      email: "fernanda.rocha@banco.com",
      telefone: "(11) 97777-2222",
      senha: "Banco@456"
    },
    {
      nome: "Roberto Almeida",
      cpf: "999.888.777-66",
      email: "roberto.almeida@banco.com",
      telefone: "(51) 96666-3333",
      senha: "Admin@789"
    },
    {
      nome: "Juliana Martins",
      cpf: "222.333.444-55",
      email: "juliana.martins@banco.com",
      telefone: "(31) 95555-4444",
      senha: "Gerente@321"
    },
    {
      nome: "Eduardo Costa",
      cpf: "333.444.555-66",
      email: "eduardo.costa@banco.com",
      telefone: "(48) 94444-5555",
      senha: "Banco@654"
    }
  ];

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
    this.gerentes.push(gerente);
  }

  toggleeditarGerente(gerente: Gerente) {
    this.selectedGerente.set(gerente);
    this.editarGerenteIsActive.set(true);
  }

  fecharModalEditar() {
    this.editarGerenteIsActive.set(false);
  }

}
