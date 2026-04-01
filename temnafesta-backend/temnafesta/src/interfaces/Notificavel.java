package interfaces;

/**
 * INTERFACE — Notificavel
 *
 * POO aplicado:
 * - Interface: define contrato de geração de mensagem de notificação
 * - Implementado por entidades que geram alertas/recibos (Pedido, Campanha)
 * - Polimorfismo de classe: cada implementador gera sua mensagem de forma diferente
 */
public interface Notificavel {

    /**
     * Gera a mensagem pré-formatada para envio via WhatsApp.
     * Cada entidade formata sua própria mensagem.
     */
    String gerarMensagemWhatsApp();

    /**
     * Retorna true se esta entidade requer notificação imediata.
     * Ex: pedido com retirada amanhã, campanha próxima do limite.
     */
    boolean requerNotificacao();
}
