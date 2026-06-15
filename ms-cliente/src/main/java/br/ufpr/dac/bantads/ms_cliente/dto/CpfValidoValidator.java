package br.ufpr.dac.bantads.ms_cliente.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// validacao mod-11 do cpf. Aceita com ou sem mascara (pontos/traco ignorados),
// rejeita tamanho != 11 e todos os digitos iguais (passam na conta mas nao sao reais).
public class CpfValidoValidator implements ConstraintValidator<CpfValido, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // @NotBlank cuida do nulo/vazio; aqui so valida o digito verificador
        if (value == null || value.isBlank()) {
            return true;
        }
        String digitos = value.replaceAll("\\D", "");
        if (digitos.length() != 11) return false;
        if (digitos.chars().distinct().count() == 1) return false;

        int[] n = new int[11];
        for (int i = 0; i < 11; i++) {
            n[i] = digitos.charAt(i) - '0';
        }
        if (digitoVerificador(n, 9, 10) != n[9]) return false;
        return digitoVerificador(n, 10, 11) == n[10];
    }

    // soma ponderada dos primeiros `qtd` digitos com pesos decrescentes a partir
    // de `pesoInicial`; resto < 2 vira 0 (regra do mod-11 do cpf)
    private static int digitoVerificador(int[] n, int qtd, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < qtd; i++) {
            soma += n[i] * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
