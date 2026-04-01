package interfaces;

/**
 * INTERFACE — Persistivel
 *
 * POO aplicado:
 * - Interface: define contrato de persistência em CSV
 * - Qualquer classe que implemente esta interface se compromete
 *   a fornecer serialização e deserialização em CSV
 *
 * Implementado por: ClienteRepository, ProdutoRepository,
 *                   CampanhaRepository, PedidoRepository
 */
public interface Persistivel<T> {

    /**
     * Persiste (salva/atualiza) uma lista de entidades no CSV.
     * Sobrescreve o arquivo com o estado atual da lista em memória.
     */
    void salvarTodos(java.util.List<T> entidades);

    /**
     * Carrega todas as entidades do CSV para uma lista em memória.
     */
    java.util.List<T> carregarTodos();

    /**
     * Retorna o caminho do arquivo CSV gerenciado por este repositório.
     */
    String getCaminhoArquivo();
}
