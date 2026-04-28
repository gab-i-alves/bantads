import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { HeaderManager } from '../../../../shared/components/header-manager/header-manager';
import { AuthService } from '../../../../core/services/auth.service';

type Cliente = {
  cpf: string;
  nome: string;
  cidade: string;
  uf: string;
  salario: number;
  saldo: number;
  credito: number;
  aprovado: boolean | null;
};

@Component({
  selector: 'app-consult-client',
  imports: [HeaderManager, FormsModule],
  templateUrl: './consult-client.html',
  styleUrl: './consult-client.css',
})
export class ConsultClient implements OnInit {

  authService = inject(AuthService);
  private route = inject(ActivatedRoute);


  searchTerm: string = '';

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['cpf']) {
        this.searchTerm = params['cpf'];
      }
    });
  }

  // get clienteSelecionado(): Cliente | null {
  //   if (!this.searchTerm.trim()) {
  //     return null;
  //   }

  //   const termo = this.searchTerm.trim().toLowerCase();
  //   const cpfSemPontuacao = this.searchTerm.replace(/\D/g, '');

  //   return this.clientes.find(cliente => {
  //     const cpfClienteSemPontuacao = cliente.cpf.replace(/\D/g, '');
  //     return (
  //       cliente.nome.toLowerCase() === termo ||
  //       cpfClienteSemPontuacao === cpfSemPontuacao
  //     );
  //   }) || null;
  // }
}
