package br.ufpr.dac.bantads.ms_cliente.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// R1: valida cpf por digito verificador (mod-11) no caminho REST do autocadastro.
// Cpf valido (como os gerados pelo teste de integracao) passa direto.
@Documented
@Constraint(validatedBy = CpfValidoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfValido {
    String message() default "cpf invalido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
