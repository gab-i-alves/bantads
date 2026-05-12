import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Cliente, ClienteCreate, ClienteUpdate } from '../models/cliente.model';

@Injectable({
  providedIn: 'root',
})
export class ClienteService {

  private http = inject(HttpClient);
  private ApiBaseUrl = 'http://localhost:8081';

  listarClientes(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.ApiBaseUrl}/clientes`);
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

  aprovarCliente(cpf: string): Observable<void> {
    return this.http.post<void>(`${this.ApiBaseUrl}/clientes/${cpf}/aprovar`, {});
  }

  rejeitarCliente(cpf: string): Observable<void> {
    return this.http.post<void>(`${this.ApiBaseUrl}/clientes/${cpf}/rejeitar`, {});
  }
}
