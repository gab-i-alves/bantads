package br.ufpr.dac.bantads.ms_conta.exceptions;

import org.springframework.http.HttpStatus;

public class ContaExceptions {

    public static class NotFoundException extends ServiceException {
        public NotFoundException(String numeroConta) {
            super("Conta não encontrada: " + numeroConta, HttpStatus.NOT_FOUND);
        }
    }
}
