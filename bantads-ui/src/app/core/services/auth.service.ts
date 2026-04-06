import { Injectable, inject } from '@angular/core';
import { ClientService } from './client.service';
import { ManagerService } from './manager.service';
import { AdminService } from './admin.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  clientes = inject(ClientService);
  gerentes = inject(ManagerService);
  admins = inject(AdminService);

  private key = 'usuarioLogado';
  private keyTipo = 'tipoUsuario';

  constructor() { }

  login(email: string, senha: string) {
    // Primeiro tenta login como gerente
    const gerentes = this.gerentes.getGerentes();
    const gerente = gerentes.find(
      g => g.email === email && g.senha === senha
    );

    if (gerente) {
      localStorage.setItem(this.key, JSON.stringify(gerente));
      localStorage.setItem(this.keyTipo, 'gerente');
      return { usuario: gerente, tipo: 'gerente' };
    }

    // Se não encontrar gerente, tenta como cliente
    const clientes = this.clientes.getClientes();
    const cliente = clientes.find(
      c => c.email === email && c.senha === senha
    );

    if (cliente) {
      localStorage.setItem(this.key, JSON.stringify(cliente));
      localStorage.setItem(this.keyTipo, 'cliente');
      return { usuario: cliente, tipo: 'cliente' };
    }

    // Se não encontrar cliente, tenta como admin
    const admins = this.admins.getAdmins();
    const admin = admins.find(
      c => c.email === email && c.senha === senha
    );

    if (admin) {
      localStorage.setItem(this.key, JSON.stringify(admin));
      localStorage.setItem(this.keyTipo, 'admin');
      return { usuario: admin, tipo: 'admin' };
    }

    return null;
  }

  informacaoUserLogado() {
    const user = localStorage.getItem(this.key);
    const tipo = localStorage.getItem(this.keyTipo);
    return user ? { usuario: JSON.parse(user), tipo: tipo || 'cliente' } : null;
  }

  getTipoUsuario(): string | null {
    return localStorage.getItem(this.keyTipo);
  }

  getUsuarioLogado() {
    const info = this.informacaoUserLogado();
    return info ? info.usuario : null;
  }

  logout() {
    localStorage.removeItem(this.key);
    localStorage.removeItem(this.keyTipo);
  }

  autoCadastro(cliente: any) {
    const novoCliente = this.clientes.PostCliente(cliente);

    localStorage.setItem(this.key, JSON.stringify(novoCliente));

    return novoCliente;
  }

  atualizarPerfil(idCliente: number, dadosAtualizados: any) {
    const clienteAtualizado = this.clientes.updateCliente(idCliente, dadosAtualizados);

    // Atualizar localStorage com os novos dados
    localStorage.setItem(this.key, JSON.stringify(clienteAtualizado));
    localStorage.setItem(this.keyTipo, 'cliente');

    return clienteAtualizado;
  }
}
