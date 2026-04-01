package model;

/**
 * MODEL — CardapioProduto
 *
 * POO aplicado:
 * - Herança: estende EntidadeBase
 * - Encapsulamento: atributos privados
 * - Relacionamento de classes: representa a tabela de junção N:N
 *   entre Cardapio e Produto (reflete 'cardapio_produto' do banco)
 * - Polimorfismo de método: implementa descricaoResumida() e toCsvLine()
 *
 * Reflete a tabela 'cardapio_produto' do banco de dados.
 */
public class CardapioProduto extends EntidadeBase {

    private int cardapioId;
    private Produto produto;       // Referência ao objeto Produto
    private int ordemExibicao;     // Controla a ordem de exibição no cardápio público

    // Construtor padrão
    public CardapioProduto() {
        super();
    }

    // Construtor principal
    public CardapioProduto(int cardapioId, Produto produto, int ordemExibicao) {
        super();
        this.cardapioId = cardapioId;
        this.produto = produto;
        this.ordemExibicao = ordemExibicao;
    }

    // Construtor de reconstrução via CSV
    public CardapioProduto(int id, int cardapioId, Produto produto, int ordemExibicao) {
        super(id);
        this.cardapioId = cardapioId;
        this.produto = produto;
        this.ordemExibicao = ordemExibicao;
    }

    // --- Getters e Setters ---

    public int getCardapioId() { return cardapioId; }
    public void setCardapioId(int cardapioId) { this.cardapioId = cardapioId; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public int getOrdemExibicao() { return ordemExibicao; }
    public void setOrdemExibicao(int ordemExibicao) { this.ordemExibicao = ordemExibicao; }

    // --- Implementação dos métodos abstratos de EntidadeBase ---

    @Override
    public String descricaoResumida() {
        return "CardapioProduto | Cardápio ID: " + cardapioId +
               " | Produto: " + (produto != null ? produto.getNome() : "N/A") +
               " | Ordem: " + ordemExibicao;
    }

    @Override
    public String toCsvLine() {
        // id;cardapioId;produtoId;ordemExibicao
        return getId() + ";" + cardapioId + ";" +
               (produto != null ? produto.getId() : "") + ";" + ordemExibicao;
    }
}
