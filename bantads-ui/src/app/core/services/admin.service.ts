import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DashboardGerente, Gerente, GerenteCreate, GerenteUpdate, RelatorioCliente } from '../models/admin.model';

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private http = inject(HttpClient);
  private ApiBaseUrl = 'http://localhost:3000';

  listarDashboardGerentes(): Observable<DashboardGerente[]> {
    return this.http.get<DashboardGerente[]>(`${this.ApiBaseUrl}/admin/dashboard`);
  }

  listarRelatorioClientes(): Observable<RelatorioCliente[]> {
    return this.http.get<RelatorioCliente[]>(`${this.ApiBaseUrl}/admin/relatorio`);
  }

  listarGerentes(): Observable<Gerente[]> {
    return this.http.get<Gerente[]>(`${this.ApiBaseUrl}/gerentes`);
  }

  criarGerente(gerente: GerenteCreate): Observable<Gerente> {
    return this.http.post<Gerente>(`${this.ApiBaseUrl}/gerentes`, gerente);
  }

  atualizarGerente(cpf: string, gerente: GerenteUpdate): Observable<Gerente> {
    return this.http.put<Gerente>(`${this.ApiBaseUrl}/gerentes/${cpf}`, gerente);
  }

  removerGerente(cpf: string): Observable<Gerente> {
    return this.http.delete<Gerente>(`${this.ApiBaseUrl}/gerentes/${cpf}`);
  }
}
