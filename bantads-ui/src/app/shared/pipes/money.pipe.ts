import { Pipe, PipeTransform } from '@angular/core';

// Formata um valor em Real (BRL) no padrao brasileiro, com separador de milhar.
// Aceita string para suportar o salario digitado como texto mascarado.
@Pipe({ name: 'money' })
export class MoneyPipe implements PipeTransform {
  transform(value: number | string | null | undefined): string {
    const numero = typeof value === 'string'
      ? Number(value.replace(/\./g, '').replace(',', '.').replace(/[^\d.-]/g, ''))
      : Number(value ?? 0);

    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(Number.isFinite(numero) ? numero : 0);
  }
}
