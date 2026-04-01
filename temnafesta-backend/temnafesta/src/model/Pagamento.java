package model;

import enums.MetodoPagamento;
import interfaces.Validavel;
import exception.ValidacaoException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MODEL — Pagamento
 *
 * POO aplicado:
 * - Herança: estende EntidadeBase
 * - Interface: implementa Validavel
 * - Encapsulamento: dataPagamento definida internamente ao confirmar
 * - Relacionamento: vinculado a um Pedido (1:1) e a um Usuario (quem confirmou)
 * - Polimorfismo de método: implementa descricaoResumida() e toCsvLine()
 *
 * Regra de negócio:
 * - Confirmação de pagamento é MANUAL — a empreendedora registra
 * - Sistema NÃO integra com gateways de pagamento
 *
 * Reflete a tabela 'pagamento' do banco de dados.
 */
public class Pagamento extends EntidadeBase implements Validavel {

    private BigDecimal valor;
    private LocalDateTime dataPagamento;
    private MetodoPagamento metodo;
    private int pedidoId;   // FK — 1:1 com Pedido
    private int usuarioId;  // FK — quem registrou a confirmação

    // Construtor padrão
    public Pagamento() {
        super();
    }

    // Construtor principal — registra pagamento confirmado agora
    public Pagamento(BigDecimal valor, MetodoPagamento metodo, int pedidoId, int usuarioId) {
        super();
        this.valor = valor;
        this.metodo = metodo;
        this.dataPagamento = LocalDateTime.now(); // Momento da confirmação manual
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
    }

    // Construtor de reconstrução via CSV
    public Pagamento(int id, BigDecimal valor, LocalDateTime dataPagamento,
                     MetodoPagamento metodo, int pedidoId, int usuarioId,
                     LocalDateTime dataCriacao) {
        super(id, dataCriacao);
        this.valor = valor;
        this.dataPagamento = dataPagamento;
        this.metodo = metodo;
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
    }

    // --- Getters e Setters ---

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDateTime getDataPagamento() { return dataPagamento; }

    public MetodoPagamento getMetodo() { return metodo; }
    public void setMetodo(MetodoPagamento metodo) { this.metodo = metodo; }

    public int getPedidoId() { return pedidoId; }
    public void setPedidoId(int pedidoId) { this.pedidoId = pedidoId; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    // --- Implementação de Validavel ---

    @Override
    public void validar() throws ValidacaoException {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("valor", "O valor do pagamento deve ser positivo.");
        }
        if (metodo == null) {
            throw new ValidacaoException("metodo", "O método de pagamento é obrigatório.");
        }
        if (pedidoId <= 0) {
            throw new ValidacaoException("pedidoId", "O pagamento deve estar vinculado a um pedido.");
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
        return "Pagamento | Valor: R$ " + valor +
               " | Método: " + (metodo != null ? metodo.getDescricao() : "N/A") +
               " | Data: " + dataPagamento +
               " | Pedido ID: " + pedidoId;
    }

    @Override
    public String toCsvLine() {
        // id;valor;dataPagamento;metodo;pedidoId;usuarioId;dataCriacao
        return getId() + ";" + valor + ";" + dataPagamento + ";" +
               (metodo != null ? metodo.name() : "") + ";" +
               pedidoId + ";" + usuarioId + ";" + getDataCriacao();
    }

    public static Pagamento fromCsvLine(String linha) {
        String[] c = linha.split(";", -1);
        return new Pagamento(
            Integer.parseInt(c[0]),
            new BigDecimal(c[1]),
            LocalDateTime.parse(c[2]),
            MetodoPagamento.valueOf(c[3]),
            Integer.parseInt(c[4]),
            Integer.parseInt(c[5]),
            LocalDateTime.parse(c[6])
        );
    }
}
