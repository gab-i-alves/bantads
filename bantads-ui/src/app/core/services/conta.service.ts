import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ContaResumo, SaldoConta } from '../models/conta.model';

@Injectable({
  providedIn: 'root',
})
export class ContaService {
  private http = inject(HttpClient);
  private ApiBaseUrl = 'http://localhost:3000';

  listarContas(): Observable<ContaResumo[]> {
    return this.http.get<ContaResumo[]>(`${this.ApiBaseUrl}/conta`);
  }

  buscarContaPorClienteCpf(cpf: string): Observable<ContaResumo | null> {
    return this.listarContas().pipe(
      map(contas => contas.find(conta => conta.clienteCpf === cpf) || null)
    );
  }

  consultarSaldo(numero: string): Observable<SaldoConta> {
    return this.http.get<SaldoConta>(`${this.ApiBaseUrl}/conta/${numero}/saldo`);
  }
}
