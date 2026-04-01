package enums;

/**
 * ENUM — CanalOrigem
 * Canal pelo qual o pedido foi recebido pela empreendedora.
 * Regra de negócio: pedidos chegam por múltiplos canais e são
 * registrados MANUALMENTE no sistema.
 */
public enum CanalOrigem {

    WHATSAPP("WhatsApp"),
    INSTAGRAM("Instagram"),
    PRESENCIAL("Presencialmente");

    private final String descricao;

    CanalOrigem(String descricao) { this.descricao = descricao; }

    public String getDescricao() { return descricao; }

    @Override
    public String toString() { return descricao; }
}
