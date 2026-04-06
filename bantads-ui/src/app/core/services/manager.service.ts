import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ManagerService {

  private key = 'gerentes';

  constructor() {
    this.initMock();
  }

  private initMock() {
    if (!localStorage.getItem(this.key)) {
      const gerentes = [
        {
          gerenteId: 1,
          nome: 'Carlos Pereira',
          cpf: '111.222.333-44',
          rua: 'Rua das Palmeiras, 789',
          cidade: 'Curitiba',
          uf: 'PR',
          email: 'carlos@gerente.com',
          senha: '123456',
          telefone: '(41) 97777-3333'
        },
        {
          gerenteId: 2,
          nome: 'yhan w',
          cpf: '111.222.333-44',
          rua: 'Rua das Palmeiras, 789',
          cidade: 'Curitiba',
          uf: 'PR',
          email: 'yhan@gerente.com',
          senha: '123456',
          telefone: '(41) 97777-3333'
        }
      ];

      localStorage.setItem(this.key, JSON.stringify(gerentes));
    }
  }

  // utils
  private getDB(): any[] {
    return JSON.parse(localStorage.getItem(this.key) || '[]');
  }

  private saveDB(gerentes: any[]) {
    localStorage.setItem(this.key, JSON.stringify(gerentes));
  }

  //funcionalidades
  getGerentes() {
    return this.getDB();
  }

  getGerenteById(id: number) {
    const gerentes = this.getDB();
    return gerentes.find(g => g.gerenteId === id);
  }

  createGerente(gerente: any) {
    const gerentes = this.getDB();

    const novoId =
      gerentes.length > 0
        ? Math.max(...gerentes.map(g => g.gerenteId)) + 1
        : 1;

    const novoGerente = {
      gerenteId: novoId,
      ...gerente
    };

    gerentes.push(novoGerente);
    this.saveDB(gerentes);

    return novoGerente;
  }

  updateGerente(id: number, dadosAtualizados: any) {
    const gerentes = this.getDB();

    const index = gerentes.findIndex(g => g.gerenteId === id);

    if (index === -1) {
      throw new Error('Gerente não encontrado');
    }

    gerentes[index] = {
      ...gerentes[index],
      ...dadosAtualizados
    };

    this.saveDB(gerentes);

    return gerentes[index];
  }


  deleteGerente(id: number) {
    const gerentes = this.getDB();

    const novosGerentes = gerentes.filter(g => g.gerenteId !== id);

    if (gerentes.length === novosGerentes.length) {
      throw new Error('Gerente não encontrado');
    }

    this.saveDB(novosGerentes);

    return true;
  }

}
