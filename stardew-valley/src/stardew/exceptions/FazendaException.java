package stardew.exceptions;

/**
 * Exceção base do sistema.
 * Todas as exceções personalizadas herdam desta.
 */
public class FazendaException extends Exception {

    public FazendaException(String mensagem) {
        super(mensagem);
    }

    public FazendaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
