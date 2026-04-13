package stardew.engine.comportamento;

import stardew.engine.Renderer;
import stardew.exceptions.FazendaException;
import stardew.model.Fazendeiro;
import stardew.model.GerenciadorTempo;
import stardew.model.NPC;

import java.util.Scanner;

/**
 * Strategy Pattern — encapsula o menu e as ações da Praia.
 */
public class ComportamentoPraia implements ComportamentoLocal {

    private final NPC willy;

    public ComportamentoPraia(Scanner scanner) {
        this.willy = new NPC("Willy", "Pescador", stardew.enums.Localizacao.PRAIA);
    }

    @Override
    public void exibirMenu() {
        System.out.println("  ── Ações disponíveis ─────────────────────────────");
        System.out.println("  [f] Pescar   [n] Conversar c/ Willy");
    }

    @Override
    public void processar(String input, Fazendeiro fazendeiro, GerenciadorTempo tempo)
            throws FazendaException {
        switch (input) {
            case "f" -> { Renderer.exibirResultado(fazendeiro.pescar()); tempo.avancarHora(2); }
            case "n" -> interagirNPC(willy);
            default  -> System.out.println("  Ação inválida na Praia.");
        }
    }

    private void interagirNPC(NPC npc) {
        System.out.println("\n  " + npc.agir());
        System.out.println("  Amizade com " + npc.getNome() + ": " + npc.getAmizade() + "/100");
    }
}
