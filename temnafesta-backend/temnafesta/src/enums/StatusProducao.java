package enums;

/**
 * ENUM — StatusProducao
 * Representa o ciclo de vida de produção de um pedido.
 * Reflete a tabela status_producao do banco de dados.
 *
 * POO: Enum com atributos, construtor e método — evita strings mágicas no sistema.
 */
public enum StatusProducao {

    AGUARDANDO_PAGAMENTO("Aguardando Pagamento", 1),
    EM_PRODUCAO("Em Produção", 2),
    PRONTO("Pronto para Retirada", 3),
    RETIRADO("Retirado", 4),
    CANCELADO("Cancelado", 5);

    private final String descricao;
    private final int ordem;

    StatusProducao(String descricao, int ordem) {
        this.descricao = descricao;
        this.ordem = ordem;
    }

    public String getDescricao() { return descricao; }
    public int getOrdem() { return ordem; }

    /**
     * Polimorfismo de método — sobrescrita de toString()
     */
    @Override
    public String toString() { return descricao; }

    /**
     * Verifica se este status é posterior ao informado.
     * Usado nas regras de negócio de transição de status.
     */
    public boolean isPosteriorA(StatusProducao outro) {
        return this.ordem > outro.ordem;
    }
}
