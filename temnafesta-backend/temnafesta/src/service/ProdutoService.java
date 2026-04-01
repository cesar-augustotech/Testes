package service;

import model.Produto;
import repository.ProdutoRepository;
import exception.ValidacaoException;
import exception.EntidadeNaoEncontradaException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * SERVICE — ProdutoService
 *
 * POO aplicado:
 * - Encapsulamento: regras de atualização de estoque e visibilidade isoladas
 * - Polimorfismo de método: atualizarEstoque sobrecarregado (add/remover)
 * - Exceções personalizadas: EstoqueInsuficienteException, ValidacaoException
 */
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    /** Cadastra um novo produto e persiste no CSV. */
    public Produto cadastrar(String nome, String descricao,
                              BigDecimal preco, int estoque) {
        Produto produto = new Produto(nome, descricao, preco, estoque);
        produto.validar(); // Lança ValidacaoException se inválido
        return produtoRepository.salvar(produto);
    }

    /** Atualiza o preço de um produto. */
    public Produto atualizarPreco(int produtoId, BigDecimal novoPreco) {
        Produto produto = buscarOuFalhar(produtoId);
        if (novoPreco == null || novoPreco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("preco", "O novo preço deve ser positivo.");
        }
        produto.setPrecoVenda(novoPreco);
        return produtoRepository.atualizar(produto);
    }

    /**
     * Adiciona unidades ao estoque.
     * POLIMORFISMO DE MÉTODO — sobrecarga (overloading)
     */
    public Produto atualizarEstoque(int produtoId, int quantidade) {
        Produto produto = buscarOuFalhar(produtoId);
        produto.adicionarEstoque(quantidade);
        return produtoRepository.atualizar(produto);
    }

    /** Adiciona estoque com motivo registrado no log. */
    public Produto atualizarEstoque(int produtoId, int quantidade, String motivo) {
        Produto produto = buscarOuFalhar(produtoId);
        produto.adicionarEstoque(quantidade, motivo);
        return produtoRepository.atualizar(produto);
    }

    /** Ativa ou inativa um produto manualmente. */
    public Produto alterarVisibilidade(int produtoId, boolean ativo) {
        Produto produto = buscarOuFalhar(produtoId);
        produto.setAtivo(ativo);
        return produtoRepository.atualizar(produto);
    }

    public List<Produto> listarTodos() {
        return produtoRepository.listarTodos();
    }

    public List<Produto> listarVisiveis() {
        return produtoRepository.listarVisiveis();
    }

    public Optional<Produto> buscarPorId(int id) {
        return produtoRepository.buscarPorId(id);
    }

    private Produto buscarOuFalhar(int id) {
        return produtoRepository.buscarPorId(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Produto", id));
    }
}
