package model;

import java.math.BigDecimal;

/**
 * MODEL — PedidoProduto
 *
 * POO aplicado:
 * - Herança: estende EntidadeBase
 * - Encapsulamento: subtotal calculado internamente
 * - Relacionamento de classes: tabela de junção N:N entre Pedido e Produto
 *   com atributos próprios (quantidade e precoUnitario)
 * - Polimorfismo de método: implementa descricaoResumida() e toCsvLine()
 *
 * Reflete a tabela 'pedido_produto' do banco de dados.
 */
public class PedidoProduto extends EntidadeBase {

    private int pedidoId;
    private Produto produto;
    private int quantidade;
    private BigDecimal precoUnitario; // Preço no momento do pedido (histórico)

    // Construtor padrão
    public PedidoProduto() {
        super();
    }

    // Construtor principal
    public PedidoProduto(int pedidoId, Produto produto, int quantidade) {
        super();
        this.pedidoId = pedidoId;
        this.produto = produto;
        this.quantidade = quantidade;
        // Captura o preço atual do produto no momento do pedido (histórico de preço)
        this.precoUnitario = produto.getPrecoVenda();
    }

    // Construtor de reconstrução via CSV (com preço histórico)
    public PedidoProduto(int id, int pedidoId, Produto produto,
                         int quantidade, BigDecimal precoUnitario) {
        super(id);
        this.pedidoId = pedidoId;
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    // --- Getters e Setters ---

    public int getPedidoId() { return pedidoId; }
    public void setPedidoId(int pedidoId) { this.pedidoId = pedidoId; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }

    /**
     * Calcula o subtotal deste item.
     * Encapsulamento: cálculo interno, exposto apenas como resultado.
     */
    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    // --- Implementação dos métodos abstratos de EntidadeBase ---

    @Override
    public String descricaoResumida() {
        return (produto != null ? produto.getNome() : "Produto N/A") +
               " x" + quantidade +
               " | Unit: R$ " + precoUnitario +
               " | Subtotal: R$ " + getSubtotal();
    }

    @Override
    public String toCsvLine() {
        // id;pedidoId;produtoId;quantidade;precoUnitario
        return getId() + ";" + pedidoId + ";" +
               (produto != null ? produto.getId() : "") + ";" +
               quantidade + ";" + precoUnitario;
    }
}
