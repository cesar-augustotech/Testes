package enums;

/**
 * ENUM — MetodoPagamento
 * Formas de pagamento aceitas pela confeitaria.
 * O sistema NÃO integra com gateways — confirmação é manual.
 */
public enum MetodoPagamento {

    PIX("PIX"),
    DINHEIRO("Dinheiro"),
    CARTAO_DEBITO("Cartão de Débito"),
    CARTAO_CREDITO("Cartão de Crédito");

    private final String descricao;

    MetodoPagamento(String descricao) { this.descricao = descricao; }

    public String getDescricao() { return descricao; }

    @Override
    public String toString() { return descricao; }
}
