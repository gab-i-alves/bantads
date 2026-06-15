import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Header } from '../../../../shared/components/header/header';
import { AuthService } from '../../../../core/services/auth.service';
import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../../../core/services/cliente.service';
import { ContaService } from '../../../../core/services/conta.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { Cliente, ClienteUpdate } from '../../../../core/models/cliente.model';
import { CpfPipe } from '../../../../shared/pipes/cpf.pipe';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-profile',
  imports: [Header, FormsModule, CpfPipe],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {

  authService = inject(AuthService);
  private clienteService = inject(ClienteService);
  private contaService = inject(ContaService);
  private notify = inject(NotificationService);

  meuDados = signal<Cliente | null>(null);
  saldo = signal<number | null>(null);
  gerenteNome = signal<string | null>(null);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  isSaving = signal(false);
  step = signal(1);

  saldoNegativo = computed(() => (this.saldo() ?? 0) < 0);
  saldoFormatado = computed(() => this.formatarMoeda(this.saldo() ?? 0));

  // Dados editáveis (salario como texto mascarado, NF15)
  editavelData = {
    nome: '',
    email: '',
    salario: '',
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
    const usuarioLogado = this.authService.getUsuarioLogado();
    this.meuDados.set(usuarioLogado);

    if (!usuarioLogado?.cpf) {
      this.errorMessage.set('Usuario nao identificado.');
      return;
    }

    this.preencherFormulario(usuarioLogado);
    this.carregarSaldoEGerente(usuarioLogado.cpf);

    this.clienteService.buscarCliente(usuarioLogado.cpf).subscribe({
      next: (cliente) => {
        this.meuDados.set(cliente);
        localStorage.setItem('usuario', JSON.stringify(cliente));
        this.preencherFormulario(cliente);
      },
      error: (error) => {
        console.error('Erro ao carregar perfil do cliente:', error);
        this.errorMessage.set('Nao foi possivel carregar os dados atualizados.');
      }
    });
  }

  carregarSaldoEGerente(cpf: string) {
    this.contaService.buscarContaPorClienteCpf(cpf).subscribe({
      next: (conta) => this.saldo.set(conta?.saldo ?? null),
      error: (error) => console.error('Erro ao buscar saldo do cliente:', error)
    });

    this.clienteService.buscarGerentePorCliente(cpf).subscribe({
      next: (gerente) => this.gerenteNome.set(gerente?.nome ?? null),
      error: (error) => console.error('Erro ao buscar gerente do cliente:', error)
    });
  }

  private formatarMoeda(valor: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(valor);
  }

  preencherFormulario(cliente: Cliente) {
    this.editavelData = {
      nome: cliente.nome || '',
      email: cliente.email || '',
      salario: this.formatarSalarioInput(cliente.salario || 0),
      telefone: this.formatarTelefone(cliente.telefone || ''),
      cep: this.formatarCep(cliente.endereco?.cep || ''),
      cidade: cliente.endereco?.cidade || '',
      uf: cliente.endereco?.estado || '',
      rua: cliente.endereco?.logradouro || '',
      numero: cliente.endereco?.numero || '',
      complemento: cliente.endereco?.complemento || ''
    };
  }

  // NF16: mascara de telefone aplicada na digitacao.
  onTelefoneInput(value: string) {
    this.editavelData.telefone = this.formatarTelefone(value);
  }

  // NF16: mascara de CEP aplicada na digitacao.
  onCepInput(value: string) {
    this.editavelData.cep = this.formatarCep(value);
  }

  // NF15: salario como texto mascarado em Real.
  onSalarioInput(value: string) {
    this.editavelData.salario = this.formatarSalarioInput(value);
  }

  private formatarTelefone(value: string): string {
    return value.replace(/\D/g, '')
      .slice(0, 11)
      .replace(/(\d{2})(\d)/, '($1) $2')
      .replace(/(\d{4,5})(\d{4})$/, '$1-$2');
  }

  private formatarCep(value: string): string {
    return value.replace(/\D/g, '')
      .slice(0, 8)
      .replace(/(\d{5})(\d{1,3})$/, '$1-$2');
  }

  private formatarSalarioInput(value: string | number): string {
    const digitos = typeof value === 'number'
      ? String(Math.round(value * 100))
      : value.replace(/\D/g, '');

    if (!digitos) {
      return '';
    }

    return (Number(digitos) / 100).toLocaleString('pt-BR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
  }

  private parseSalario(value: string): number {
    const normalizado = value.replace(/\./g, '').replace(',', '.').replace(/[^\d.-]/g, '');
    return Number(normalizado);
  }

  salvarDados() {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    // Validação básica
    if (!this.editavelData.nome || !this.editavelData.email ||
        !this.editavelData.telefone || !this.editavelData.cidade || 
        !this.editavelData.uf || !this.editavelData.rua || !this.editavelData.numero) {
      this.errorMessage.set('Por favor, preencha todos os campos obrigatorios.');
      return;
    }

    // Validar email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.editavelData.email)) {
      this.errorMessage.set('Email invalido.');
      return;
    }

    const clienteAtual = this.meuDados();

    if (!clienteAtual?.cpf) {
      this.errorMessage.set('Usuario nao identificado.');
      return;
    }

    const dadosAtualizados: ClienteUpdate = {
      cpf: clienteAtual.cpf,
      nome: this.editavelData.nome,
      email: this.editavelData.email,
      telefone: this.editavelData.telefone.replace(/\D/g, ''),
      salario: this.parseSalario(this.editavelData.salario),
      endereco: {
        logradouro: this.editavelData.rua,
        numero: this.editavelData.numero,
        complemento: this.editavelData.complemento || null,
        cep: this.editavelData.cep.replace(/\D/g, ''),
        cidade: this.editavelData.cidade,
        estado: this.editavelData.uf.toUpperCase()
      }
    };

    if (!dadosAtualizados.endereco.cep || dadosAtualizados.endereco.cep.length !== 8) {
      this.errorMessage.set('CEP deve conter 8 digitos.');
      return;
    }

    if (!dadosAtualizados.salario || dadosAtualizados.salario <= 0) {
      this.errorMessage.set('Salario deve ser maior que zero.');
      return;
    }

    this.isSaving.set(true);
    this.clienteService.atualizarCliente(clienteAtual.cpf, dadosAtualizados)
      .pipe(finalize(() => {
        this.isSaving.set(false);
        this.step.set(1);
      }))
      .subscribe({
        next: (cliente) => {
          this.meuDados.set(cliente);
          localStorage.setItem('usuario', JSON.stringify(cliente));
          this.preencherFormulario(cliente);
          this.successMessage.set('Dados atualizados com sucesso.');
          this.notify.sucesso('Dados atualizados com sucesso.');
        },
        error: (error) => {
          console.error('Erro ao atualizar perfil do cliente:', error);
          const mensagem = this.notify.mensagemDeErroHttp(error, 'Nao foi possivel atualizar os dados.');
          this.errorMessage.set(mensagem);
          this.notify.erro(mensagem);
        }
      });
  }

  nextStep() {
    if (this.step() < 2) this.step.update(step => step + 1);
  }

  prevStep() {
    if (this.step() > 1) this.step.update(step => step - 1);
  }
}
