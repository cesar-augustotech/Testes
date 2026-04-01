package model;

import interfaces.Validavel;
import exception.ValidacaoException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MODEL — Produto
 *
 * POO aplicado:
 * - Herança: estende EntidadeBase
 * - Interface: implementa Validavel
 * - Encapsulamento: estoque controlado via métodos (não exposto diretamente para escrita)
 * - Relacionamento: vinculado a um Cardapio via CardapioProduto (N:N)
 * - Polimorfismo de método: implementa descricaoResumida() e toCsvLine()
 *
 * Regra de negócio:
 * - Produto com estoque zerado é ocultado automaticamente do cardápio público
 *
 * Reflete a tabela 'produto' do banco de dados.
 */
public class Produto extends EntidadeBase implements Validavel {

    private String nome;
    private String descricao;
    private BigDecimal precoVenda;
    private int estoque;
    private boolean isAtivo;

    // Construtor padrão
    public Produto() {
        super();
        this.isAtivo = true;
        this.estoque = 0;
    }

    // Construtor principal — novo produto
    public Produto(String nome, String descricao, BigDecimal precoVenda, int estoque) {
        super();
        this.nome = nome;
        this.descricao = descricao;
        this.precoVenda = precoVenda;
        this.estoque = estoque;
        this.isAtivo = true;
    }

    // Construtor de reconstrução via CSV
    public Produto(int id, String nome, String descricao, BigDecimal precoVenda,
                   int estoque, boolean isAtivo, LocalDateTime dataCriacao) {
        super(id, dataCriacao);
        this.nome = nome;
        this.descricao = descricao;
        this.precoVenda = precoVenda;
        this.estoque = estoque;
        this.isAtivo = isAtivo;
    }

    // --- Getters ---

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(BigDecimal precoVenda) { this.precoVenda = precoVenda; }

    public int getEstoque() { return estoque; }

    public boolean isAtivo() { return isAtivo; }
    public void setAtivo(boolean ativo) { this.isAtivo = ativo; }

    // --- Regras de negócio (Encapsulamento de comportamento) ---

    /**
     * Verifica se o produto está visível no cardápio público.
     * Regra: visível somente se ativo E com estoque > 0.
     */
    public boolean isVisivelNoCardapio() {
        return isAtivo && estoque > 0;
    }

    /**
     * Reduz o estoque do produto.
     * Lança EstoqueInsuficienteException se não houver quantidade suficiente.
     */
    public void reduzirEstoque(int quantidade) {
        if (quantidade > estoque) {
            throw new exception.EstoqueInsuficienteException(nome, estoque, quantidade);
        }
        this.estoque -= quantidade;
    }

    /**
     * Adiciona unidades ao estoque.
     * POLIMORFISMO DE MÉTODO — sobrecarga (overloading)
     */
    public void adicionarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new ValidacaoException("quantidade", "Quantidade a adicionar deve ser positiva.");
        }
        this.estoque += quantidade;
    }

    /** Define o estoque diretamente (usado em atualizações manuais). */
    public void adicionarEstoque(int quantidade, String motivo) {
        adicionarEstoque(quantidade);
        System.out.println("[ESTOQUE] +" + quantidade + " em '" + nome + "'. Motivo: " + motivo);
    }

    // --- Implementação de Validavel ---

    @Override
    public void validar() throws ValidacaoException {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("nome", "O nome do produto é obrigatório.");
        }
        if (precoVenda == null || precoVenda.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("precoVenda", "O preço de venda deve ser positivo.");
        }
        if (estoque < 0) {
            throw new ValidacaoException("estoque", "O estoque não pode ser negativo.");
        }
    }

    @Override
    public boolean isValido() {
        try { validar(); return true; }
        catch (ValidacaoException e) { return false; }
    }

    // --- Implementação dos métodos abstratos de EntidadeBase ---

    @Override
    public String descricaoResumida() {
        return "Produto: " + nome +
               " | Preço: R$ " + precoVenda +
               " | Estoque: " + estoque +
               " | Visível: " + (isVisivelNoCardapio() ? "Sim" : "Não");
    }

    @Override
    public String toCsvLine() {
        // id;nome;descricao;precoVenda;estoque;isAtivo;dataCriacao
        return getId() + ";" + nome + ";" +
               (descricao != null ? descricao.replace(";", ",") : "") + ";" +
               precoVenda + ";" + estoque + ";" + isAtivo + ";" + getDataCriacao();
    }

    public static Produto fromCsvLine(String linha) {
        String[] c = linha.split(";", -1);
        return new Produto(
            Integer.parseInt(c[0]),
            c[1],
            c[2],
            new BigDecimal(c[3]),
            Integer.parseInt(c[4]),
            Boolean.parseBoolean(c[5]),
            LocalDateTime.parse(c[6])
        );
    }
}
