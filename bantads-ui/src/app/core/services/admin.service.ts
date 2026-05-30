import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DashboardGerente, RelatorioCliente } from '../models/admin.model';

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
}
