import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ClientService {

  private key = 'clientes';

  constructor() {
    this.initMock();
  }

  private initMock() {
    if (!localStorage.getItem(this.key)) {
      const clientes = [
        {
          idCliente: 1,
          gerenteId: 1,
          nome: 'João Silva',
          cpf: '123.456.789-00',
          rua: 'Rua das Flores, 123',
          cidade: 'Curitiba',
          uf: 'PR',
          email: 'joao@email.com',
          senha: '123456',
          salario: 3000,
          telefone: '(41) 99999-1111',
          saldo: 1200,
          credito: 1500
        },
        {
          idCliente: 2,
          gerenteId: 1,
          nome: 'Maria Souza',
          cpf: '987.654.321-00',
          rua: 'Av. Brasil, 456',
          cidade: 'Curitiba',
          uf: 'PR',
          email: 'maria@email.com',
          senha: '123456',
          salario: 5000,
          telefone: '(41) 98888-2222',
          saldo: 3000,
          credito: 2500
        },
        {
          idCliente: 3,
          gerenteId: 2,
          nome: 'Carlos Lima',
          cpf: '111.222.333-44',
          rua: 'Rua XV de Novembro, 789',
          cidade: 'Curitiba',
          uf: 'PR',
          email: 'carlos@email.com',
          senha: '123456',
          salario: 2800,
          telefone: '(41) 97777-3333',
          saldo: 800,
          credito: 1400
        },
        {
          idCliente: 4,
          gerenteId: 2,
          nome: 'Ana Pereira',
          cpf: '555.666.777-88',
          rua: 'Rua das Palmeiras, 321',
          cidade: 'Curitiba',
          uf: 'PR',
          email: 'ana@email.com',
          senha: '123456',
          salario: 4200,
          telefone: '(41) 96666-4444',
          saldo: 2500,
          credito: 2000
        },
        {
          idCliente: 5,
          gerenteId: 3,
          nome: 'Lucas Martins',
          cpf: '999.888.777-66',
          rua: 'Av. Sete de Setembro, 654',
          cidade: 'Curitiba',
          uf: 'PR',
          email: 'lucas@email.com',
          senha: '123456',
          salario: 3500,
          telefone: '(41) 95555-5555',
          saldo: 1800,
          credito: 1700
        },
        {
          idCliente: 6,
          gerenteId: 1,
          nome: 'Lucas Martins',
          cpf: '999.888.777-66',
          rua: 'Av. Sete de Setembro, 654',
          cidade: 'Curitiba',
          uf: 'PR',
          email: 'lucas@email.com',
          senha: '123456',
          salario: 3500,
          telefone: '(41) 95555-5555',
          saldo: 1800,
          credito: 1700,
          statusAprovacao: 'pendente'
        }
      ];

      localStorage.setItem(this.key, JSON.stringify(clientes));
    }
  }

  // utils
  private getDB(): any[] {
    return JSON.parse(localStorage.getItem(this.key) || '[]');
  }

  private saveDB(clientes: any[]) {
    localStorage.setItem(this.key, JSON.stringify(clientes));
  }

  // =========================

  getClientes() {
    return this.getDB();
  }

  getClientesByGerente(gerenteId: number) {
    return this.getDB().filter(c => c.gerenteId === gerenteId);
  }

  getClienteById(id: number) {
    return this.getDB().find(c => c.idCliente === id);
  }

  PostCliente(cliente: any) {
    const clientes = this.getDB();

    const novoId =
      clientes.length > 0
        ? Math.max(...clientes.map(c => c.idCliente)) + 1
        : 1;

    const novoCliente = {
      idCliente: novoId,
      statusAprovacao: 'pendente',
      gerenteId: 1,
      ...cliente
    };

    clientes.push(novoCliente);
    this.saveDB(clientes);

    return novoCliente;
  }

  //  operações financeiras
  transferirDinheiro(idRemetente: number, idDestinatario: number, valor: number) {
    const clientes = this.getDB();

    const remetente = clientes.find(c => c.idCliente === idRemetente);
    const destinatario = clientes.find(c => c.idCliente === idDestinatario);

    if (!remetente || !destinatario) {
      throw new Error('Remetente ou destinatário não encontrado');
    }

    if (remetente.saldo < valor) {
      throw new Error('Saldo insuficiente');
    }

    remetente.saldo -= valor;
    destinatario.saldo += valor;

    this.saveDB(clientes);

    return { remetente, destinatario };
  }

  sacarDinheiro(idCliente: number, valor: number) {
    const clientes = this.getDB();
    const cliente = clientes.find(c => c.idCliente === idCliente);

    if (!cliente) throw new Error('Cliente não encontrado');
    if (cliente.saldo < valor) throw new Error('Saldo insuficiente');

    cliente.saldo -= valor;

    this.saveDB(clientes);

    return cliente;
  }

  depositarDinheiro(idCliente: number, valor: number) {

    if (valor <= 0) throw new Error('Valor de depósito deve ser positivo');
    if (!idCliente) console.error('ID do cliente é obrigatório para depósito');

    const clientes = this.getDB();
    const cliente = clientes.find(c => c.idCliente === idCliente);

    if (!cliente) throw new Error('Cliente não encontrado');

    cliente.saldo += valor;

    this.saveDB(clientes);

    return cliente;
  }


  //aprovar solicitação
  aprovarSolicitacao(idCliente: number) {
    const clientes = this.getDB();

    const cliente = clientes.find(c => c.idCliente === idCliente);

    if (!cliente) {
      throw new Error('Cliente não encontrado');
    }

    if (cliente.statusAprovacao !== 'pendente') {
      throw new Error('Cliente já foi aprovado ou não está pendente');
    }

    cliente.statusAprovacao = 'aprovado';

    this.saveDB(clientes);

    return cliente;
  }

  recusarSolicitacao(idCliente: number) {
    const clientes = this.getDB();

    const cliente = clientes.find(c => c.idCliente === idCliente);

    if (!cliente) {
      throw new Error('Cliente não encontrado');
    }

    if (cliente.statusAprovacao !== 'pendente') {
      throw new Error('Cliente já foi recusado ou não está pendente');
    }

    cliente.statusAprovacao = 'recusado';

    this.saveDB(clientes);

    return cliente;
  }

  // atualizar dados do cliente
  updateCliente(idCliente: number, dadosAtualizados: any) {
    const clientes = this.getDB();

    const cliente = clientes.find(c => c.idCliente === idCliente);

    if (!cliente) {
      throw new Error('Cliente não encontrado');
    }

    // Atualizar apenas os campos que não alteram a integridade do sistema
    const camposPermitidos = ['nome', 'email', 'salario', 'funcao', 'telefone', 'cep', 'cidade', 'uf', 'rua', 'numero', 'complemento'];

    camposPermitidos.forEach(campo => {
      if (dadosAtualizados[campo] !== undefined) {
        cliente[campo] = dadosAtualizados[campo];
      }
    });

    this.saveDB(clientes);

    return cliente;
  }
}
