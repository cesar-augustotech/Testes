package stardew.interfaces;

import stardew.enums.Estacao;
import stardew.model.GerenciadorTempo;

/**
 * Observer Pattern — contrato para quem quer ser notificado
 * quando o dia avança em GerenciadorTempo.
 *
 * Implementado por:
 *   - Fazendeiro  → restaura energia (descansar)
 *   - Fazenda     → avança crescimento de parcelas e animais
 *   - RepositorioJogo → auto-save ao dormir
 *   - GameEngine  → exibe a tela de bom dia
 *
 * O método onEstacaoMudou() tem implementação default vazia,
 * então implementações que não precisam reagir a estações não
 * precisam sobrescrever.
 */
public interface EventoDiaObserver {

    /** Disparado toda vez que o dia avança (inclusive mudança de estação). */
    void onDiaAvancou(GerenciadorTempo tempo);

    /** Disparado apenas quando a estação muda (a cada 28 dias). */
    default void onEstacaoMudou(Estacao novaEstacao) {}
}
