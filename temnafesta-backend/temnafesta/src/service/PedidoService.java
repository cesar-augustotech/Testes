package service;

import model.*;
import enums.*;
import repository.*;
import exception.*;
import java.util.List;

/**
 * SERVICE — PedidoService
 *
 * POO aplicado:
 * - Encapsulamento: toda regra de negócio isolada aqui (fora dos models)
 * - Relacionamento de classes: orquestra Cliente, Produto, Campanha e Pedido
 * - Polimorfismo de método: métodos sobrecarregados para registrar pedido
 *   com e sem campanha
 * - Exceções personalizadas: lançadas ao violar regras de negócio
 *
 * Responsável pelo fluxo completo do pedido:
 * registro → confirmação de pagamento → geração de recibo → atualização de status
 */
public class PedidoService {

    // Injeção manual de dependências (sem Spring)
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final CampanhaRepository campanhaRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         ClienteRepository clienteRepository,
                         ProdutoRepository produtoRepository,
                         CampanhaRepository campanhaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.campanhaRepository = campanhaRepository;
    }

    /**
     * Registra um novo pedido vinculado a uma campanha.
     * POLIMORFISMO DE MÉTODO — sobrecarga (overloading)
     *
     * Fluxo:
     * 1. Valida cliente e campanha
     * 2. Adiciona itens (reduz estoque de cada produto)
     * 3. Registra pedido na campanha (verifica limite)
     * 4. Persiste no CSV
     */
    public Pedido registrarPedido(int clienteId, int campanhaId,
                                   java.util.Map<Integer, Integer> itensProdutoQuantidade,
                                   java.time.LocalDateTime dataEntrega,
                                   CanalOrigem canal, String observacao, int usuarioId) {

        // 1. Valida cliente
        Cliente cliente = clienteRepository.buscarPorId(clienteId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente", clienteId));

        // 2. Valida campanha
        Campanha campanha = campanhaRepository.buscarPorId(campanhaId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Campanha", campanhaId));

        // Cria o pedido
        Pedido pedido = new Pedido(cliente, dataEntrega, canal, observacao, usuarioId, campanhaId);

        // 3. Adiciona itens (cada adição reduz estoque e recalcula total)
        for (java.util.Map.Entry<Integer, Integer> entry : itensProdutoQuantidade.entrySet()) {
            Produto produto = produtoRepository.buscarPorId(entry.getKey())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Produto", entry.getKey()));

            if (!produto.isVisivelNoCardapio()) {
                throw new ValidacaoException("produto",
                    "O produto '" + produto.getNome() + "' não está disponível no cardápio.");
            }
            pedido.adicionarItem(produto, entry.getValue()); // Lança EstoqueInsuficienteException se falhar
            produtoRepository.atualizar(produto); // Persiste estoque atualizado
        }

        // 4. Valida o pedido completo
        pedido.validar();

        // 5. Registra na campanha (verifica limite — lança CampanhaEncerradaException se cheio)
        campanha.registrarNovoPedido();
        campanhaRepository.atualizar(campanha);

        // 6. Persiste o pedido
        return pedidoRepository.salvar(pedido);
    }

    /**
     * Registra pedido SEM campanha vinculada.
     * POLIMORFISMO DE MÉTODO — sobrecarga (overloading)
     */
    public Pedido registrarPedido(int clienteId,
                                   java.util.Map<Integer, Integer> itensProdutoQuantidade,
                                   java.time.LocalDateTime dataEntrega,
                                   CanalOrigem canal, String observacao, int usuarioId) {
        return registrarPedido(clienteId, 0, itensProdutoQuantidade,
                               dataEntrega, canal, observacao, usuarioId);
    }

    /**
     * Confirma o pagamento de um pedido.
     * Avança o status para EM_PRODUCAO e gera o recibo.
     */
    public String confirmarPagamento(int pedidoId, MetodoPagamento metodo, int usuarioId) {
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Pedido", pedidoId));

        if (pedido.getStatusProducao() != StatusProducao.AGUARDANDO_PAGAMENTO) {
            throw new ValidacaoException("status",
                "Pedido #" + pedidoId + " não está aguardando pagamento. " +
                "Status atual: " + pedido.getStatusProducao().getDescricao());
        }

        // Cria e vincula o pagamento
        Pagamento pagamento = new Pagamento(pedido.getValorTotal(), metodo, pedidoId, usuarioId);
        pagamento.validar();

        // Confirma no pedido (avança status automaticamente)
        pedido.confirmarPagamento(pagamento, usuarioId);
        pedidoRepository.atualizar(pedido);

        // Retorna a mensagem de recibo formatada para WhatsApp
        return pedido.gerarMensagemWhatsApp();
    }

    /**
     * Avança o status de produção de um pedido.
     */
    public Pedido avancarStatus(int pedidoId, StatusProducao novoStatus,
                                 String observacao, int usuarioId) {
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Pedido", pedidoId));

        pedido.avancarStatus(novoStatus, observacao, usuarioId);
        return pedidoRepository.atualizar(pedido);
    }

    /** Lista pedidos com retirada amanhã (para alertas do dashboard). */
    public List<Pedido> listarRetiradaAmanha() {
        return pedidoRepository.listarRetiradaAmanha();
    }

    /** Lista pedidos com pagamento pendente crítico (> 24h sem confirmar). */
    public List<Pedido> listarPagamentosCriticos() {
        return pedidoRepository.listarPagamentosPendentesCriticos();
    }

    /** Lista todos os pedidos de uma campanha. */
    public List<Pedido> listarPorCampanha(int campanhaId) {
        return pedidoRepository.listarPorCampanha(campanhaId);
    }

    /** Lista pedidos por status. */
    public List<Pedido> listarPorStatus(StatusProducao status) {
        return pedidoRepository.listarPorStatus(status);
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.listarTodos();
    }
}
