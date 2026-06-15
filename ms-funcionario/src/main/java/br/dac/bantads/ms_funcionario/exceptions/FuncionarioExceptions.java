package br.dac.bantads.ms_funcionario.exceptions;

import org.springframework.http.HttpStatus;

public class FuncionarioExceptions {

    public static class NotFoundException extends ServiceException {
        public NotFoundException() {
            super("Funcionário não encontrado", HttpStatus.NOT_FOUND);
        }
    }

    // CPF duplicado é conflito de recurso já existente, não erro de sintaxe da requisição
    public static class CpfInUseException extends ServiceException {
        public CpfInUseException(String cpf) {
            super("CPF já cadastrado: " + cpf, HttpStatus.CONFLICT);
        }
    }
}
