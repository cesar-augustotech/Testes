package stardew.engine.comportamento;

import stardew.engine.Renderer;
import stardew.exceptions.FazendaException;
import stardew.model.Fazendeiro;
import stardew.model.GerenciadorTempo;

import java.util.Scanner;

/**
 * Strategy Pattern — encapsula o menu e as ações da Floresta.
 */
public class ComportamentoFloresta implements ComportamentoLocal {

    public ComportamentoFloresta(Scanner scanner) {}

    @Override
    public void exibirMenu() {
        System.out.println("  ── Ações disponíveis ─────────────────────────────");
        System.out.println("  [t] Cortar árvore");
    }

    @Override
    public void processar(String input, Fazendeiro fazendeiro, GerenciadorTempo tempo)
            throws FazendaException {
        switch (input) {
            case "t" -> { Renderer.exibirResultado(fazendeiro.cortarArvore()); tempo.avancarHora(2); }
            default  -> System.out.println("  Ação inválida na Floresta.");
        }
    }
}
