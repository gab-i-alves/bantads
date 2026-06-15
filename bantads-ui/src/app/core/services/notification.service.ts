import { Injectable, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

export type TipoNotificacao = 'sucesso' | 'erro' | 'info';

export interface Notificacao {
  id: number;
  mensagem: string;
  tipo: TipoNotificacao;
}

// Servico unico de feedback ao usuario para padronizar sucesso/erro/info em
// todas as telas que consomem o gateway. Mantem uma lista de notificacoes em um
// signal que o componente raiz renderiza como toasts. Implementacao propria (sem
// MatSnackBar) para nao depender de @angular/animations, que nao esta no projeto.
@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  // lista reativa de toasts visiveis; o app-root assina e renderiza.
  readonly notificacoes = signal<Notificacao[]>([]);

  private proximoId = 0;

  sucesso(mensagem: string): void {
    this.abrir(mensagem, 'sucesso', 4000);
  }

  erro(mensagem: string): void {
    // Erro fica mais tempo na tela porque exige leitura/acao do usuario.
    this.abrir(mensagem, 'erro', 6000);
  }

  info(mensagem: string): void {
    this.abrir(mensagem, 'info', 4000);
  }

  dispensar(id: number): void {
    this.notificacoes.update((lista) => lista.filter((n) => n.id !== id));
  }

  // Extrai uma mensagem legivel de um erro HTTP. O gateway costuma devolver o
  // detalhe em error.error.error ou error.error.message; caimos no fallback
  // quando a resposta nao traz nada util (ex: falha de rede status 0).
  mensagemDeErroHttp(erro: unknown, fallback: string): string {
    if (erro instanceof HttpErrorResponse) {
      if (erro.status === 0) {
        return 'Nao foi possivel conectar ao servidor. Tente novamente em instantes.';
      }

      const corpo = erro.error;
      if (typeof corpo === 'string' && corpo.trim()) {
        return corpo;
      }

      const detalhe = corpo?.error || corpo?.message;
      if (typeof detalhe === 'string' && detalhe.trim()) {
        return detalhe;
      }
    }

    return fallback;
  }

  // Atalho que une extracao da mensagem e exibicao do erro.
  erroHttp(erro: unknown, fallback: string): void {
    this.erro(this.mensagemDeErroHttp(erro, fallback));
  }

  private abrir(mensagem: string, tipo: TipoNotificacao, duracao: number): void {
    const id = this.proximoId++;
    this.notificacoes.update((lista) => [...lista, { id, mensagem, tipo }]);
    // some sozinho depois da duracao; o usuario tambem pode fechar manualmente.
    setTimeout(() => this.dispensar(id), duracao);
  }
}
