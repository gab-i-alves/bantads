import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Cliente, ClienteCreate, ClienteUpdate, MelhorCliente } from '../models/cliente.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ClienteService {

  private http = inject(HttpClient);
  private ApiBaseUrl = environment.apiBaseUrl;

  listarClientes(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.ApiBaseUrl}/cliente`);
  }

  listarClientesPorGerente(): Observable<Cliente[]> {
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    return this.http.post<Cliente[]>(`${this.ApiBaseUrl}/clientes-por-gerente`, {
      gerenteCpf: usuario.cpf,
    });
  }

  listarClientesPendentes(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.ApiBaseUrl}/cliente?filtro=para_aprovar`);
  }

  buscarCliente(cpf: string): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.ApiBaseUrl}/cliente/${cpf}`);
  }

  criarCliente(cliente: ClienteCreate): Observable<Cliente> {
    return this.http.post<Cliente>(`${this.ApiBaseUrl}/cliente`, cliente);
  }

  atualizarCliente(cpf: string, dados: ClienteUpdate): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.ApiBaseUrl}/cliente/${cpf}`, dados);
  }

  aprovarCliente(cpf: string): Observable<Cliente> {
    return this.http.post<Cliente>(`${this.ApiBaseUrl}/cliente/${cpf}/aprovar`, {});
  }

  rejeitarCliente(cpf: string, motivo: string): Observable<Cliente> {
    return this.http.post<Cliente>(`${this.ApiBaseUrl}/cliente/${cpf}/rejeitar`, { motivo });
  }

  listarMelhoresClientes(): Observable<MelhorCliente[]> {
    return this.http.get<MelhorCliente[]>(`${this.ApiBaseUrl}/melhores-clientes`);
  }

  buscarGerentePorCliente(cpf: string): Observable<{ cpf: string; nome: string | null }> {
    return this.http.get<{ cpf: string; nome: string | null }>(`${this.ApiBaseUrl}/cliente/${cpf}/gerente`);
  }
}
