package model;

import enums.StatusProducao;
import java.time.LocalDateTime;

/**
 * MODEL — HistoricoStatusPedido
 *
 * POO aplicado:
 * - Herança: estende EntidadeBase
 * - Encapsulamento: dataAlteracao definida internamente na criação
 * - Relacionamento: vinculado a Pedido, StatusProducao (enum) e Usuario
 * - Polimorfismo de método: implementa descricaoResumida() e toCsvLine()
 *
 * Funciona como log de auditoria de cada mudança de status de um pedido.
 * Reflete a tabela 'historico_status_pedido' do banco de dados.
 */
public class HistoricoStatusPedido extends EntidadeBase {

    private LocalDateTime dataAlteracao;
    private String observacao;
    private StatusProducao statusProducao; // Enum — status registrado neste momento
    private int pedidoId;
    private int usuarioId; // Quem realizou a mudança de status

    // Construtor padrão
    public HistoricoStatusPedido() {
        super();
        this.dataAlteracao = LocalDateTime.now();
    }

    // Construtor principal — nova entrada no histórico
    public HistoricoStatusPedido(StatusProducao statusProducao, String observacao,
                                  int pedidoId, int usuarioId) {
        super();
        this.dataAlteracao = LocalDateTime.now();
        this.statusProducao = statusProducao;
        this.observacao = observacao;
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
    }

    // Construtor de reconstrução via CSV
    public HistoricoStatusPedido(int id, LocalDateTime dataAlteracao, String observacao,
                                  StatusProducao statusProducao, int pedidoId,
                                  int usuarioId, LocalDateTime dataCriacao) {
        super(id, dataCriacao);
        this.dataAlteracao = dataAlteracao;
        this.observacao = observacao;
        this.statusProducao = statusProducao;
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
    }

    // --- Getters ---

    public LocalDateTime getDataAlteracao() { return dataAlteracao; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public StatusProducao getStatusProducao() { return statusProducao; }

    public int getPedidoId() { return pedidoId; }

    public int getUsuarioId() { return usuarioId; }

    // --- Implementação dos métodos abstratos de EntidadeBase ---

    @Override
    public String descricaoResumida() {
        return "[" + dataAlteracao + "] Status → " + statusProducao.getDescricao() +
               " | Pedido ID: " + pedidoId +
               " | Usuário ID: " + usuarioId +
               (observacao != null && !observacao.isBlank() ? " | Obs: " + observacao : "");
    }

    @Override
    public String toCsvLine() {
        // id;dataAlteracao;observacao;statusProducao;pedidoId;usuarioId;dataCriacao
        return getId() + ";" + dataAlteracao + ";" +
               (observacao != null ? observacao.replace(";", ",") : "") + ";" +
               statusProducao.name() + ";" + pedidoId + ";" +
               usuarioId + ";" + getDataCriacao();
    }
}
