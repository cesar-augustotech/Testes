package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MODEL — Cardapio
 *
 * POO aplicado:
 * - Herança: estende EntidadeBase
 * - Encapsulamento: lista de produtos gerenciada por métodos
 * - Relacionamento de classes:
 *     → Pertence a uma Campanha (associação — FK campanha_id)
 *     → Tem N Produtos via CardapioProduto (composição da tabela de junção)
 * - Polimorfismo de método: implementa descricaoResumida() e toCsvLine()
 *
 * Reflete as tabelas 'cardapio' e 'cardapio_produto' do banco de dados.
 */
public class Cardapio extends EntidadeBase {

    private String nome;
    private String observacoes;
    private boolean isAtivo;
    private int campanhaId; // FK — associação com Campanha

    // Relacionamento N:N com Produto via CardapioProduto
    private List<CardapioProduto> itens;

    // Construtor padrão
    public Cardapio() {
        super();
        this.isAtivo = true;
        this.itens = new ArrayList<>();
    }

    // Construtor principal
    public Cardapio(String nome, String observacoes, int campanhaId) {
        super();
        this.nome = nome;
        this.observacoes = observacoes;
        this.isAtivo = true;
        this.campanhaId = campanhaId;
        this.itens = new ArrayList<>();
    }

    // Construtor de reconstrução via CSV
    public Cardapio(int id, String nome, String observacoes, boolean isAtivo,
                    int campanhaId, LocalDateTime dataCriacao) {
        super(id, dataCriacao);
        this.nome = nome;
        this.observacoes = observacoes;
        this.isAtivo = isAtivo;
        this.campanhaId = campanhaId;
        this.itens = new ArrayList<>();
    }

    // --- Getters e Setters ---

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public boolean isAtivo() { return isAtivo; }
    public void setAtivo(boolean ativo) { this.isAtivo = ativo; }

    public int getCampanhaId() { return campanhaId; }
    public void setCampanhaId(int campanhaId) { this.campanhaId = campanhaId; }

    public List<CardapioProduto> getItens() { return itens; }
    public void setItens(List<CardapioProduto> itens) { this.itens = itens; }

    // --- Gerenciamento de itens (Encapsulamento de comportamento) ---

    /** Adiciona um produto ao cardápio com ordem de exibição. */
    public void adicionarProduto(Produto produto, int ordemExibicao) {
        CardapioProduto item = new CardapioProduto(getId(), produto, ordemExibicao);
        itens.add(item);
    }

    /** Remove um produto do cardápio pelo id do produto. */
    public void removerProduto(int produtoId) {
        itens.removeIf(item -> item.getProduto().getId() == produtoId);
    }

    /**
     * Retorna apenas os produtos visíveis no cardápio público.
     * Regra: isAtivo && estoque > 0
     */
    public List<CardapioProduto> getItensVisiveis() {
        return itens.stream()
                    .filter(item -> item.getProduto().isVisivelNoCardapio())
                    .sorted((a, b) -> Integer.compare(a.getOrdemExibicao(), b.getOrdemExibicao()))
                    .collect(Collectors.toList());
    }

    /** Retorna a quantidade total de produtos cadastrados (visíveis + ocultos). */
    public int getTotalProdutos() {
        return itens.size();
    }

    // --- Implementação dos métodos abstratos de EntidadeBase ---

    @Override
    public String descricaoResumida() {
        return "Cardápio: " + nome +
               " | Campanha ID: " + campanhaId +
               " | Itens: " + itens.size() +
               " | Visíveis: " + getItensVisiveis().size() +
               " | " + (isAtivo ? "ATIVO" : "INATIVO");
    }

    @Override
    public String toCsvLine() {
        // id;nome;observacoes;isAtivo;campanhaId;dataCriacao
        return getId() + ";" + nome + ";" +
               (observacoes != null ? observacoes.replace(";", ",") : "") + ";" +
               isAtivo + ";" + campanhaId + ";" + getDataCriacao();
    }

    public static Cardapio fromCsvLine(String linha) {
        String[] c = linha.split(";", -1);
        return new Cardapio(
            Integer.parseInt(c[0]),
            c[1],
            c[2],
            Boolean.parseBoolean(c[3]),
            Integer.parseInt(c[4]),
            LocalDateTime.parse(c[5])
        );
    }
}
