package exception;

/**
 * EXCEÇÃO PERSONALIZADA — CampanhaEncerradaException
 *
 * POO aplicado:
 * - Herança: estende RuntimeException
 * - Lançada quando se tenta adicionar um pedido a uma campanha encerrada
 *   ou que já atingiu o limite máximo de pedidos
 */
public class CampanhaEncerradaException extends RuntimeException {

    private final String nomeCampanha;

    public CampanhaEncerradaException(String nomeCampanha) {
        super("A campanha '" + nomeCampanha + "' está encerrada ou atingiu o limite de pedidos.");
        this.nomeCampanha = nomeCampanha;
    }

    public CampanhaEncerradaException(String nomeCampanha, String motivo) {
        super("Campanha '" + nomeCampanha + "' indisponível: " + motivo);
        this.nomeCampanha = nomeCampanha;
    }

    public String getNomeCampanha() {
        return nomeCampanha;
    }
}
