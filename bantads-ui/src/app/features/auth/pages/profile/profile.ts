import { Component, inject, OnInit } from '@angular/core';
import { Header } from '../../../../shared/components/header/header';
import { AuthService } from '../../../../core/services/auth.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-profile',
  imports: [Header, FormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {

  authService = inject(AuthService);

  meuDados: any;

  // Dados editáveis
  editavelData = {
    nome: '',
    email: '',
    salario: 0,
    funcao: '',
    telefone: '',
    cep: '',
    cidade: '',
    uf: '',
    rua: '',
    numero: '',
    complemento: ''
  };

  ngOnInit() {
    this.carregarDados();
  }

  carregarDados() {
    this.meuDados = this.authService.getUsuarioLogado();

    if (this.meuDados) {
      // Preencher objeto editável com dados atuais
      this.editavelData = {
        nome: this.meuDados.nome,
        email: this.meuDados.email,
        salario: this.meuDados.salario || 0,
        funcao: this.meuDados.funcao || '',
        telefone: this.meuDados.telefone || '',
        cep: this.meuDados.endereco?.cep || '',
        cidade: this.meuDados.endereco?.cidade || '',
        uf: this.meuDados.endereco?.estado || '',
        rua: this.meuDados.endereco?.logradouro || '',
        numero: this.meuDados.endereco?.numero || '',
        complemento: this.meuDados.endereco?.complemento || ''
      };
    }
  }

  salvarDados() {
    // Validação básica
    if (!this.editavelData.nome || !this.editavelData.email ||
        !this.editavelData.telefone || !this.editavelData.cidade || 
        !this.editavelData.uf || !this.editavelData.rua || !this.editavelData.numero) {
      alert('Por favor, preencha todos os campos obrigatórios!');
      return;
    }

    // Validar email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.editavelData.email)) {
      alert('Email inválido!');
      return;
    }

    try {
      // Apenas o cliente pode editar seus próprios dados
      if (this.meuDados && this.meuDados.idCliente) {
 

        // Atualizar dados exibidos


        alert('Dados atualizados com sucesso!');
        this.step = 1;
      } else {
        alert('Erro: Usuário não identificado');
      }
    } catch (error: any) {
      alert('Erro ao atualizar dados: ' + error.message);
    }
  }

  step: number = 1;

  nextStep() {
    if (this.step < 4) this.step++;
  }

  prevStep() {
    if (this.step > 1) this.step--;
  }
}
