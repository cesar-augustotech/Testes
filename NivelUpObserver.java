package stardew.interfaces;

import stardew.model.Habilidades;

/**
 * Observer Pattern — contrato para quem quer ser notificado
 * quando uma habilidade do Fazendeiro sobe de nível.
 *
 * Implementado por GameEngine para exibir a mensagem na UI.
 * Poderia também ser implementado por um sistema de achievements
 * ou de desbloqueio de receitas — sem alterar Habilidades.
 */
@FunctionalInterface
public interface NivelUpObserver {

    /**
     * Chamado por Habilidades.ganharXp() quando o nível sobe.
     *
     * @param tipo      qual habilidade subiu
     * @param novoNivel o nível alcançado (2–10)
     */
    void onNivelUp(Habilidades.Tipo tipo, int novoNivel);
}
