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
    return this.http.get<Cliente[]>(`${this.ApiBaseUrl}/clientes`);
  }

  listarClientesPorGerente(): Observable<Cliente[]> {
    // GET /clientes sem filtro: o gateway escopa pelos clientes do gerente do token (R12)
    return this.http.get<Cliente[]>(`${this.ApiBaseUrl}/clientes`);
  }

  listarClientesPendentes(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.ApiBaseUrl}/clientes?filtro=para_aprovar`);
  }

  buscarCliente(cpf: string): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.ApiBaseUrl}/clientes/${cpf}`);
  }

  criarCliente(cliente: ClienteCreate): Observable<Cliente> {
    return this.http.post<Cliente>(`${this.ApiBaseUrl}/clientes`, cliente);
  }

  atualizarCliente(cpf: string, dados: ClienteUpdate): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.ApiBaseUrl}/clientes/${cpf}`, dados);
  }

  aprovarCliente(cpf: string): Observable<Cliente> {
    return this.http.post<Cliente>(`${this.ApiBaseUrl}/clientes/${cpf}/aprovar`, {});
  }

  rejeitarCliente(cpf: string, motivo: string): Observable<Cliente> {
    return this.http.post<Cliente>(`${this.ApiBaseUrl}/clientes/${cpf}/rejeitar`, { motivo });
  }

  listarMelhoresClientes(): Observable<MelhorCliente[]> {
    return this.http.get<MelhorCliente[]>(`${this.ApiBaseUrl}/clientes?filtro=melhores_clientes`);
  }

  buscarGerentePorCliente(cpf: string): Observable<{ cpf: string; nome: string | null }> {
    return this.http.get<{ cpf: string; nome: string | null }>(`${this.ApiBaseUrl}/clientes/${cpf}/gerente`);
  }
}
