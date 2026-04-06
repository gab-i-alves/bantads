import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Header } from '../../../../shared/components/header/header';
import { ClientService } from '../../../../core/services/client.service';
import { AuthService } from '../../../../core/services/auth.service';
import { signal } from '@angular/core';

interface Cliente {
  idCliente: number;
  nome: string;
  email: string;
  [key: string]: any;
}

@Component({
  selector: 'app-transfer',
  imports: [Header, FormsModule],
  templateUrl: './transfer.html',
  styleUrl: './transfer.css',
})
export class Transfer implements OnInit {
  private clientService = inject(ClientService);
  private authService = inject(AuthService);

  amount: string = '0,00';
  isActive: boolean = false;

  recipients = signal<Cliente[]>([]);
  selectedRecipient = signal<Cliente | null>(null);

  ngOnInit() {
    this.loadRecipients();
  }

  loadRecipients() {
    const currentUser = this.authService.getUsuarioLogado();
    const allClients = this.clientService.getClientes();

    if (currentUser) {
      const filtered = allClients.filter(
        client => client.idCliente !== currentUser.idCliente
      );
      this.recipients.set(filtered);
    } else {
      this.recipients.set(allClients);
    }
  }

  formatCurrency(value: string) {
    let numeric = value.replace(/\D/g, '');

    if (numeric === '') numeric = '0';

    let number = (parseInt(numeric) / 100).toFixed(2);

    this.amount = number.replace('.', ',');
  }


  transferir() {

    if (this.selectedRecipient() && this.amount) {
      const currentUser = this.authService.getUsuarioLogado();
      const recipientId = this.selectedRecipient()!.idCliente;
      const amountNumber = parseFloat(this.amount.replace(',', '.'));
      console.log('Transferindo', amountNumber, 'para cliente ID:', recipientId);
      this.clientService.transferirDinheiro(currentUser.idCliente, recipientId, amountNumber);
    }

    alert('dinheiro transferido!')
  }
}
