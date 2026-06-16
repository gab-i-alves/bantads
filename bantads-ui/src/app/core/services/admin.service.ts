import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { DashboardGerente, Gerente, GerenteCreate, GerenteUpdate, RelatorioCliente } from '../models/admin.model';
import { environment } from '../../../environments/environment';

// R15 no contrato do testador: GET /gerentes?filtro=dashboard, com itens no
// formato { gerente:{cpf,nome}, clientes:[contas], saldo_positivo, saldo_negativo }.
interface DashboardItemApi {
  gerente: { cpf: string; nome: string };
  clientes: unknown[];
  saldo_positivo: number;
  saldo_negativo: number;
}

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private http = inject(HttpClient);
  private ApiBaseUrl = environment.apiBaseUrl;

  listarDashboardGerentes(): Observable<DashboardGerente[]> {
    // R15: consome o contrato do testador e adapta pro shape da tela.
    return this.http.get<DashboardItemApi[]>(`${this.ApiBaseUrl}/gerentes?filtro=dashboard`).pipe(
      map((lista) => lista.map((item) => ({
        cpf: item.gerente?.cpf ?? '',
        nome: item.gerente?.nome ?? '',
        email: '',
        clientes: Array.isArray(item.clientes) ? item.clientes.length : 0,
        saldosPositivos: item.saldo_positivo ?? 0,
        saldosNegativos: item.saldo_negativo ?? 0,
      })))
    );
  }

  listarRelatorioClientes(): Observable<RelatorioCliente[]> {
    return this.http.get<RelatorioCliente[]>(`${this.ApiBaseUrl}/clientes?filtro=adm_relatorio_clientes`);
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
