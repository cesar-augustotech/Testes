package interfaces;

/**
 * INTERFACE — Validavel
 *
 * POO aplicado:
 * - Interface: define contrato de validação de estado de uma entidade
 * - Implementado pelos Services antes de persistir dados
 * - Garante que regras de negócio sejam verificadas antes de qualquer operação
 */
public interface Validavel {

    /**
     * Valida o estado atual do objeto.
     * Lança exceção personalizada se inválido.
     *
     * @throws exception.ValidacaoException se a entidade estiver em estado inválido
     */
    void validar() throws exception.ValidacaoException;

    /**
     * Retorna true se o objeto está em estado válido para operação,
     * sem lançar exceção — útil para verificações rápidas.
     */
    boolean isValido();
}
