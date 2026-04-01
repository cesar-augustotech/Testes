package exception;

/**
 * EXCEÇÃO PERSONALIZADA — EntidadeNaoEncontradaException
 *
 * POO aplicado:
 * - Herança: estende RuntimeException
 * - Lançada pelos Services/Repositories quando uma entidade não é
 *   encontrada pelo id informado (equivalente ao 404 em REST)
 */
public class EntidadeNaoEncontradaException extends RuntimeException {

    private final String tipoEntidade;
    private final int idBuscado;

    public EntidadeNaoEncontradaException(String tipoEntidade, int idBuscado) {
        super(tipoEntidade + " com id=" + idBuscado + " não encontrado(a).");
        this.tipoEntidade = tipoEntidade;
        this.idBuscado = idBuscado;
    }

    public String getTipoEntidade() { return tipoEntidade; }
    public int getIdBuscado() { return idBuscado; }
}
