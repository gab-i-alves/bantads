import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NotificationService } from './core/services/notification.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('bantads-ui');

  // expoe as notificacoes pro template renderizar os toasts globais
  private readonly notificacaoService = inject(NotificationService);
  protected readonly notificacoes = this.notificacaoService.notificacoes;

  protected dispensar(id: number): void {
    this.notificacaoService.dispensar(id);
  }
}
