package model;

import enums.CanalOrigem;
import enums.StatusProducao;
import interfaces.Validavel;
import interfaces.Notificavel;
import exception.ValidacaoException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MODEL — Pedido
 *
 * POO aplicado:
 * - Herança: estende EntidadeBase
 * - Interface: implementa Validavel e Notificavel
 * - Encapsulamento: valorTotal calculado a partir dos itens; status controlado
 *   por método que registra histórico
 * - Relacionamento de classes:
 *     → Agregação com Cliente (cliente existe independentemente)
 *     → Associação com Usuario (quem registrou)
 *     → Composição com PedidoProduto (itens pertencem ao pedido)
 *     → Composição com HistoricoStatusPedido (log interno)
 *     → Composição com Pagamento (1:1)
 *     → Enum: CanalOrigem e StatusProducao
 * - Polimorfismo de método: implementa descricaoResumida(), toCsvLine(),
 *                           gerarMensagemWhatsApp(), validar()
 *
 * Reflete a tabela 'pedido' do banco de dados.
 * Regra de negócio:
 * - Retirada feita pelo cliente (sem entrega)
 * - Confirmação de pagamento é manual
 */
public class Pedido extends EntidadeBase implements Validavel, Notificavel {

    private LocalDateTime dataPedido;
    private LocalDateTime dataEntrega;      // Data de retirada pelo cliente
    private BigDecimal valorTotal;          // Calculado a partir dos itens
    private String observacao;
    private CanalOrigem canalOrigem;        // Enum: de onde veio o pedido
    private StatusProducao statusProducao;  // Enum: ciclo de vida do pedido

    // Relacionamentos
    private Cliente cliente;                // Agregação
    private int usuarioId;                  // Quem registrou (FK)
    private int campanhaId;                 // Campanha vinculada (FK)
    private Pagamento pagamento;            // Composição 1:1
    private List<PedidoProduto> itens;      // Composição 1:N
    private List<HistoricoStatusPedido> historico; // Log de status

    // Construtor padrão
    public Pedido() {
        super();
        this.dataPedido = LocalDateTime.now();
        this.statusProducao = StatusProducao.AGUARDANDO_PAGAMENTO;
        this.itens = new ArrayList<>();
        this.historico = new ArrayList<>();
        this.valorTotal = BigDecimal.ZERO;
    }

    // Construtor principal — novo pedido
    public Pedido(Cliente cliente, LocalDateTime dataEntrega, CanalOrigem canalOrigem,
                  String observacao, int usuarioId, int campanhaId) {
        super();
        this.dataPedido = LocalDateTime.now();
        this.cliente = cliente;
        this.dataEntrega = dataEntrega;
        this.canalOrigem = canalOrigem;
        this.observacao = observacao;
        this.usuarioId = usuarioId;
        this.campanhaId = campanhaId;
        this.statusProducao = StatusProducao.AGUARDANDO_PAGAMENTO;
        this.itens = new ArrayList<>();
        this.historico = new ArrayList<>();
        this.valorTotal = BigDecimal.ZERO;
    }

    // Construtor de reconstrução via CSV
    public Pedido(int id, LocalDateTime dataPedido, LocalDateTime dataEntrega,
                  BigDecimal valorTotal, String observacao, CanalOrigem canalOrigem,
                  StatusProducao statusProducao, Cliente cliente, int usuarioId,
                  int campanhaId, LocalDateTime dataCriacao) {
        super(id, dataCriacao);
        this.dataPedido = dataPedido;
        this.dataEntrega = dataEntrega;
        this.valorTotal = valorTotal;
        this.observacao = observacao;
        this.canalOrigem = canalOrigem;
        this.statusProducao = statusProducao;
        this.cliente = cliente;
        this.usuarioId = usuarioId;
        this.campanhaId = campanhaId;
        this.itens = new ArrayList<>();
        this.historico = new ArrayList<>();
    }

    // --- Getters e Setters ---

    public LocalDateTime getDataPedido() { return dataPedido; }

    public LocalDateTime getDataEntrega() { return dataEntrega; }
    public void setDataEntrega(LocalDateTime dataEntrega) { this.dataEntrega = dataEntrega; }

    public BigDecimal getValorTotal() { return valorTotal; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public CanalOrigem getCanalOrigem() { return canalOrigem; }
    public void setCanalOrigem(CanalOrigem canalOrigem) { this.canalOrigem = canalOrigem; }

    public StatusProducao getStatusProducao() { return statusProducao; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getCampanhaId() { return campanhaId; }
    public void setCampanhaId(int campanhaId) { this.campanhaId = campanhaId; }

    public Pagamento getPagamento() { return pagamento; }

    public List<PedidoProduto> getItens() { return itens; }

    public List<HistoricoStatusPedido> getHistorico() { return historico; }

    // --- Regras de negócio (Encapsulamento de comportamento) ---

    /**
     * Adiciona um item ao pedido e recalcula o valor total.
     * Reduz o estoque do produto automaticamente.
     */
    public void adicionarItem(Produto produto, int quantidade) {
        produto.reduzirEstoque(quantidade); // Lança EstoqueInsuficienteException se falhar
        PedidoProduto item = new PedidoProduto(getId(), produto, quantidade);
        itens.add(item);
        recalcularTotal();
    }

    /** Remove um item do pedido e devolve o estoque. */
    public void removerItem(int produtoId) {
        itens.stream()
             .filter(i -> i.getProduto().getId() == produtoId)
             .findFirst()
             .ifPresent(item -> {
                 item.getProduto().adicionarEstoque(item.getQuantidade(), "Cancelamento de item no pedido #" + getId());
                 itens.remove(item);
                 recalcularTotal();
             });
    }

    /** Recalcula o valorTotal somando os subtotais dos itens. */
    private void recalcularTotal() {
        this.valorTotal = itens.stream()
                               .map(PedidoProduto::getSubtotal)
                               .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Avança o status do pedido e registra no histórico.
     * Lança ValidacaoException se a transição for inválida.
     */
    public void avancarStatus(StatusProducao novoStatus, String observacao, int usuarioId) {
        if (!novoStatus.isPosteriorA(this.statusProducao) &&
            novoStatus != StatusProducao.CANCELADO) {
            throw new ValidacaoException("status",
                "Transição inválida: " + this.statusProducao + " → " + novoStatus);
        }
        this.statusProducao = novoStatus;
        HistoricoStatusPedido registro = new HistoricoStatusPedido(
            novoStatus, observacao, getId(), usuarioId
        );
        this.historico.add(registro);
    }

    /**
     * Confirma o pagamento do pedido.
     * Avança o status para EM_PRODUCAO automaticamente.
     */
    public void confirmarPagamento(Pagamento pagamento, int usuarioId) {
        this.pagamento = pagamento;
        avancarStatus(StatusProducao.EM_PRODUCAO, "Pagamento confirmado via " +
                      pagamento.getMetodo().getDescricao(), usuarioId);
    }

    /** Retorna true se a retirada é amanhã (usado nos alertas de notificação). */
    public boolean isRetiradaAmanha() {
        if (dataEntrega == null) return false;
        LocalDateTime amanha = LocalDateTime.now().plusDays(1);
        return dataEntrega.toLocalDate().equals(amanha.toLocalDate());
    }

    /** Retorna true se o pedido está aguardando pagamento há mais de 24h. */
    public boolean isPagamentoPendenteCritico() {
        return statusProducao == StatusProducao.AGUARDANDO_PAGAMENTO &&
               dataPedido.isBefore(LocalDateTime.now().minusHours(24));
    }

    // --- Implementação de Validavel ---

    @Override
    public void validar() throws ValidacaoException {
        if (cliente == null) {
            throw new ValidacaoException("cliente", "O pedido deve ter um cliente vinculado.");
        }
        if (dataEntrega == null) {
            throw new ValidacaoException("dataEntrega", "A data de retirada é obrigatória.");
        }
        if (dataEntrega.isBefore(LocalDateTime.now())) {
            throw new ValidacaoException("dataEntrega", "A data de retirada não pode ser no passado.");
        }
        if (itens == null || itens.isEmpty()) {
            throw new ValidacaoException("itens", "O pedido deve ter ao menos um item.");
        }
        if (canalOrigem == null) {
            throw new ValidacaoException("canalOrigem", "O canal de origem é obrigatório.");
        }
    }

    @Override
    public boolean isValido() {
        try { validar(); return true; }
        catch (ValidacaoException e) { return false; }
    }

    // --- Implementação de Notificavel ---

    @Override
    public String gerarMensagemWhatsApp() {
        StringBuilder sb = new StringBuilder();
        sb.append("🍫 *Recibo — Tem na Festa Chocolate*\n\n");
        sb.append("Olá, *").append(cliente != null ? cliente.getNome() : "Cliente").append("*!\n");
        sb.append("Seu pedido *#").append(getId()).append("* foi confirmado.\n\n");
        sb.append("📦 *Itens:*\n");
        for (PedidoProduto item : itens) {
            sb.append("  • ").append(item.descricaoResumida()).append("\n");
        }
        sb.append("\n💰 *Total: R$ ").append(valorTotal).append("*\n");
        sb.append("📅 *Retirada:* ").append(dataEntrega).append("\n");
        sb.append("\nObrigada pela preferência! 🤍");
        return sb.toString();
    }

    @Override
    public boolean requerNotificacao() {
        return isRetiradaAmanha() || isPagamentoPendenteCritico();
    }

    // --- Implementação dos métodos abstratos de EntidadeBase ---

    @Override
    public String descricaoResumida() {
        return "Pedido #" + getId() +
               " | Cliente: " + (cliente != null ? cliente.getNome() : "N/A") +
               " | Canal: " + (canalOrigem != null ? canalOrigem.getDescricao() : "N/A") +
               " | Total: R$ " + valorTotal +
               " | Retirada: " + dataEntrega +
               " | Status: " + statusProducao.getDescricao();
    }

    @Override
    public String toCsvLine() {
        // id;dataPedido;dataEntrega;valorTotal;observacao;canalOrigem;
        // statusProducao;clienteId;usuarioId;campanhaId;dataCriacao
        return getId() + ";" + dataPedido + ";" + dataEntrega + ";" +
               valorTotal + ";" +
               (observacao != null ? observacao.replace(";", ",") : "") + ";" +
               (canalOrigem != null ? canalOrigem.name() : "") + ";" +
               (statusProducao != null ? statusProducao.name() : "") + ";" +
               (cliente != null ? cliente.getId() : "") + ";" +
               usuarioId + ";" + campanhaId + ";" + getDataCriacao();
    }

    public static Pedido fromCsvLine(String linha) {
        String[] c = linha.split(";", -1);
        return new Pedido(
            Integer.parseInt(c[0]),
            LocalDateTime.parse(c[1]),
            LocalDateTime.parse(c[2]),
            new BigDecimal(c[3]),
            c[4],
            CanalOrigem.valueOf(c[5]),
            StatusProducao.valueOf(c[6]),
            null, // cliente resolvido pelo Repository
            Integer.parseInt(c[8]),
            Integer.parseInt(c[9]),
            LocalDateTime.parse(c[10])
        );
    }
}
