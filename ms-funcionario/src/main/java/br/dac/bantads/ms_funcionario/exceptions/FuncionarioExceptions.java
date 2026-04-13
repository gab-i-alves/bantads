package br.dac.bantads.ms_funcionario.exceptions;

import org.springframework.http.HttpStatus;

public class FuncionarioExceptions {

    public static class NotFoundException extends ServiceException {
        public NotFoundException() {
            super("Funcionário não encontrado", HttpStatus.NOT_FOUND);
        }
    }

    // vi que pode ser 409 conflict também, mas parece que 400 é mais correto
    public static class CpfInUseException extends ServiceException {
        public CpfInUseException(String cpf) {
            super("CPF já cadastrado: " + cpf, HttpStatus.BAD_REQUEST);
        }
    }
}
