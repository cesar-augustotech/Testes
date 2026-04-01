package repository;

import interfaces.Persistivel;
import model.Produto;
import exception.CsvException;
import exception.EntidadeNaoEncontradaException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY — ProdutoRepository
 *
 * POO aplicado:
 * - Interface: implementa Persistivel<Produto>
 * - Polimorfismo de classe: é um Persistivel — tratável polimorficamente
 * - Persistência: usa CsvUtil (FileReader + BufferedReader internamente)
 */
public class ProdutoRepository implements Persistivel<Produto> {

    private static final String CAMINHO_ARQUIVO = CsvUtil.DIRETORIO + "produtos.csv";
    private static final String CABECALHO =
        "id;nome;descricao;precoVenda;estoque;isAtivo;dataCriacao";

    private List<Produto> cache;

    public ProdutoRepository() {
        this.cache = new ArrayList<>();
        try { this.cache = carregarTodos(); }
        catch (Exception e) { System.err.println("[ProdutoRepository] Aviso: " + e.getMessage()); }
    }

    @Override
    public void salvarTodos(List<Produto> produtos) {
        List<String> linhas = new ArrayList<>();
        for (Produto p : produtos) linhas.add(p.toCsvLine());
        try {
            CsvUtil.escreverLinhas(CAMINHO_ARQUIVO, CABECALHO, linhas);
            this.cache = new ArrayList<>(produtos);
        } catch (CsvException e) {
            System.err.println("[ProdutoRepository] Erro ao salvar: " + e.getMessage());
        }
    }

    @Override
    public List<Produto> carregarTodos() {
        List<Produto> produtos = new ArrayList<>();
        try {
            for (String linha : CsvUtil.lerLinhas(CAMINHO_ARQUIVO))
                produtos.add(Produto.fromCsvLine(linha));
        } catch (CsvException e) {
            System.err.println("[ProdutoRepository] Erro ao carregar: " + e.getMessage());
        }
        return produtos;
    }

    @Override
    public String getCaminhoArquivo() { return CAMINHO_ARQUIVO; }

    public Produto salvar(Produto produto) {
        List<String> linhasRaw = new ArrayList<>();
        try { linhasRaw = CsvUtil.lerLinhas(CAMINHO_ARQUIVO); }
        catch (CsvException ignored) {}
        produto.setId(CsvUtil.proximoId(linhasRaw));
        cache.add(produto);
        salvarTodos(cache);
        return produto;
    }

    public Produto atualizar(Produto produtoAtualizado) {
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).getId() == produtoAtualizado.getId()) {
                cache.set(i, produtoAtualizado);
                salvarTodos(cache);
                return produtoAtualizado;
            }
        }
        throw new EntidadeNaoEncontradaException("Produto", produtoAtualizado.getId());
    }

    public Optional<Produto> buscarPorId(int id) {
        return cache.stream().filter(p -> p.getId() == id).findFirst();
    }

    /** Retorna apenas produtos visíveis no cardápio público. */
    public List<Produto> listarVisiveis() {
        List<Produto> visiveis = new ArrayList<>();
        for (Produto p : cache) if (p.isVisivelNoCardapio()) visiveis.add(p);
        return visiveis;
    }

    public List<Produto> listarTodos() { return new ArrayList<>(cache); }

    public void remover(int id) {
        if (!cache.removeIf(p -> p.getId() == id))
            throw new EntidadeNaoEncontradaException("Produto", id);
        salvarTodos(cache);
    }
}
