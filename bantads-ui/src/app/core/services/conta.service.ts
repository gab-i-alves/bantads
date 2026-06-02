import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ContaResumo, Extrato, OperacaoContaResponse, SaldoConta, TransferenciaResponse } from '../models/conta.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ContaService {
  private http = inject(HttpClient);
  private ApiBaseUrl = environment.apiBaseUrl;

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

  depositar(numero: string, valor: number): Observable<OperacaoContaResponse> {
    return this.http.post<OperacaoContaResponse>(
      `${this.ApiBaseUrl}/conta/${numero}/depositar`,
      { valor }
    );
  }

  sacar(numero: string, valor: number): Observable<OperacaoContaResponse> {
    return this.http.post<OperacaoContaResponse>(
      `${this.ApiBaseUrl}/conta/${numero}/sacar`,
      { valor }
    );
  }

  transferir(numeroOrigem: string, numeroDestino: string, valor: number): Observable<TransferenciaResponse> {
    return this.http.post<TransferenciaResponse>(
      `${this.ApiBaseUrl}/conta/${numeroOrigem}/transferir`,
      { destino: numeroDestino, valor }
    );
  }

  consultarExtrato(numero: string, inicio?: string, fim?: string): Observable<Extrato> {
    let params = new HttpParams();
    if (inicio) {
      params = params.set('inicio', inicio);
    }
    if (fim) {
      params = params.set('fim', fim);
    }
    return this.http.get<Extrato>(`${this.ApiBaseUrl}/conta/${numero}/extrato`, { params });
  }
}
