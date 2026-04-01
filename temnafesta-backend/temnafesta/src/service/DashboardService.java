package service;

import model.*;
import enums.StatusProducao;
import interfaces.Notificavel;
import repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SERVICE — DashboardService
 *
 * POO aplicado:
 * - Interface: usa Notificavel polimorficamente — qualquer entidade que
 *   implemente Notificavel pode ser coletada nos alertas
 * - Polimorfismo de classe: trata Pedido e Campanha como Notificavel
 * - Encapsulamento: cálculo de KPIs encapsulado em métodos específicos
 *
 * Responsável por calcular todos os KPIs e métricas do Dashboard (Tela 02).
 */
public class DashboardService {

    private final PedidoRepository pedidoRepository;
    private final CampanhaRepository campanhaRepository;

    public DashboardService(PedidoRepository pedidoRepository,
                             CampanhaRepository campanhaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.campanhaRepository = campanhaRepository;
    }

    // =====================================================================
    // KPIs — Levam a uma decisão/ação
    // =====================================================================

    /** KPI: Total de pedidos confirmados (status != AGUARDANDO_PAGAMENTO). */
    public int getPedidosAceitos() {
        return (int) pedidoRepository.listarTodos().stream()
            .filter(p -> p.getStatusProducao() != StatusProducao.AGUARDANDO_PAGAMENTO &&
                         p.getStatusProducao() != StatusProducao.CANCELADO)
            .count();
    }

    /** KPI: Pedidos aguardando confirmação de pagamento. */
    public int getPedidosAguardandoPagamento() {
        return pedidoRepository.listarPorStatus(StatusProducao.AGUARDANDO_PAGAMENTO).size();
    }

    /** KPI: Retiradas agendadas para hoje. */
    public int getRetiradosHoje() {
        return pedidoRepository.listarPorDataRetirada(LocalDate.now()).size();
    }

    /** KPI: Taxa de ocupação da campanha ativa (pedidosAceitos / limite). */
    public double getTaxaOcupacaoCampanha() {
        return campanhaRepository.buscarAtiva()
            .map(Campanha::getTaxaOcupacao)
            .orElse(0.0);
    }

    /** KPI: Pedidos com pagamento pendente há mais de 24h (crítico). */
    public List<Pedido> getPagamentosPendentesCriticos() {
        return pedidoRepository.listarPagamentosPendentesCriticos();
    }

    /** KPI: Próximas retiradas (hoje + amanhã). */
    public List<Pedido> getProximasRetiradas() {
        List<Pedido> resultado = new ArrayList<>();
        resultado.addAll(pedidoRepository.listarPorDataRetirada(LocalDate.now()));
        resultado.addAll(pedidoRepository.listarRetiradaAmanha());
        return resultado;
    }

    // =====================================================================
    // Métricas — Informam e contextualizam
    // =====================================================================

    /** Métrica: Faturamento total da campanha ativa (somente pedidos confirmados). */
    public BigDecimal getFaturamentoCampanhaAtiva() {
        Optional<Campanha> ativa = campanhaRepository.buscarAtiva();
        if (ativa.isEmpty()) return BigDecimal.ZERO;

        return pedidoRepository.listarPorCampanha(ativa.get().getId()).stream()
            .filter(p -> p.getPagamento() != null)
            .map(Pedido::getValorTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Métrica: Ticket médio da campanha ativa. */
    public BigDecimal getTicketMedioCampanhaAtiva() {
        Optional<Campanha> ativa = campanhaRepository.buscarAtiva();
        if (ativa.isEmpty()) return BigDecimal.ZERO;

        List<Pedido> pedidosCampanha = pedidoRepository.listarPorCampanha(ativa.get().getId())
            .stream()
            .filter(p -> p.getPagamento() != null)
            .toList();

        if (pedidosCampanha.isEmpty()) return BigDecimal.ZERO;

        BigDecimal total = pedidosCampanha.stream()
            .map(Pedido::getValorTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(BigDecimal.valueOf(pedidosCampanha.size()),
                            2, java.math.RoundingMode.HALF_UP);
    }

    // =====================================================================
    // Alertas — Usa polimorfismo de interface (Notificavel)
    // =====================================================================

    /**
     * Coleta TODOS os alertas do sistema.
     *
     * POLIMORFISMO DE CLASSE — trata Pedido e Campanha como Notificavel.
     * O método requerNotificacao() é chamado polimorficamente sem saber
     * o tipo concreto de cada objeto.
     */
    public List<String> getAlertasSistema() {
        List<String> alertas = new ArrayList<>();

        // Coleta todos os Notificavel do sistema
        List<Notificavel> notificaveis = new ArrayList<>();
        notificaveis.addAll(pedidoRepository.listarTodos()); // Pedido implements Notificavel
        campanhaRepository.buscarAtiva().ifPresent(notificaveis::add); // Campanha implements Notificavel

        // Polimorfismo: chama requerNotificacao() em cada objeto sem saber o tipo concreto
        for (Notificavel n : notificaveis) {
            if (n.requerNotificacao()) {
                alertas.add(n.gerarMensagemWhatsApp());
            }
        }

        return alertas;
    }

    /** Exibe um resumo completo do dashboard no console. */
    public void exibirResumo() {
        System.out.println("========================================");
        System.out.println("       DASHBOARD — TEM NA FESTA        ");
        System.out.println("========================================");
        System.out.println("Pedidos Aceitos:           " + getPedidosAceitos());
        System.out.println("Aguardando Pagamento:      " + getPedidosAguardandoPagamento());
        System.out.println("Retiradas Hoje:            " + getRetiradosHoje());
        System.out.printf ("Taxa de Ocupação:          %.1f%%%n", getTaxaOcupacaoCampanha());
        System.out.println("Faturamento (confirmados): R$ " + getFaturamentoCampanhaAtiva());
        System.out.println("Ticket Médio:              R$ " + getTicketMedioCampanhaAtiva());
        System.out.println("Alertas ativos:            " + getAlertasSistema().size());
        System.out.println("========================================");
    }
}
