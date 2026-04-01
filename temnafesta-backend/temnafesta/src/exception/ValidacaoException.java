package exception;

/**
 * EXCEÇÃO PERSONALIZADA — ValidacaoException
 *
 * POO aplicado:
 * - Herança: estende RuntimeException (exceção não verificada)
 * - Encapsulamento: carrega o campo inválido junto com a mensagem
 * - Usada pelos Services quando dados não passam nas validações de negócio
 */
public class ValidacaoException extends RuntimeException {

    private final String campoInvalido;

    public ValidacaoException(String mensagem) {
        super(mensagem);
        this.campoInvalido = "não especificado";
    }

    public ValidacaoException(String campoInvalido, String mensagem) {
        super("Campo [" + campoInvalido + "]: " + mensagem);
        this.campoInvalido = campoInvalido;
    }

    public String getCampoInvalido() {
        return campoInvalido;
    }
}
