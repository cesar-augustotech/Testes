package stardew.engine.comportamento;

import stardew.engine.Renderer;
import stardew.exceptions.FazendaException;
import stardew.model.Fazendeiro;
import stardew.model.GerenciadorTempo;
import stardew.model.Inimigo;

import java.util.Random;
import java.util.Scanner;

/**
 * Strategy Pattern — encapsula o menu e as ações das Minas.
 */
public class ComportamentoMinas implements ComportamentoLocal {

    private final Scanner scanner;
    private static final Random RAND = new Random();

    private static final Inimigo[] INIMIGOS_DISPONIVEIS = {
        new Inimigo("Slime Verde",    30,  5, 20),
        new Inimigo("Morcego",        20,  8, 25),
        new Inimigo("Esqueleto",      50, 12, 40),
        new Inimigo("Golem de Pedra", 80, 18, 70)
    };

    public ComportamentoMinas(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void exibirMenu() {
        System.out.println("  ── Ações disponíveis ─────────────────────────────");
        System.out.println("  [n] Minerar   [l] Lutar");
    }

    @Override
    public void processar(String input, Fazendeiro fazendeiro, GerenciadorTempo tempo)
            throws FazendaException {
        switch (input) {
            case "n" -> { Renderer.exibirResultado(fazendeiro.minerar()); tempo.avancarHora(2); }
            case "l" -> menuLutar(fazendeiro, tempo);
            default  -> System.out.println("  Ação inválida nas Minas.");
        }
    }

    private void menuLutar(Fazendeiro fazendeiro, GerenciadorTempo tempo)
            throws FazendaException {
        // Cria novo inimigo a cada encontro (não reutiliza os do array — seria com vida anterior)
        Inimigo original = INIMIGOS_DISPONIVEIS[RAND.nextInt(INIMIGOS_DISPONIVEIS.length)];
        Inimigo inimigo  = new Inimigo(original.getNome(), original.getVidaMax(),
                                       original.getDano(), original.getXpRecompensa());

        System.out.println("\n  Um " + inimigo.getNome() + " apareceu!");
        System.out.println("  " + inimigo);
        System.out.println("  [1] Atacar   [2] Fugir");
        System.out.print("  > ");
        if (!"1".equals(scanner.nextLine().trim())) {
            System.out.println("  Você fugiu!");
            return;
        }

        while (!inimigo.estaMorto() && fazendeiro.temEnergia(12)) {
            Renderer.exibirResultado(fazendeiro.lutar(inimigo));
            tempo.avancarHora(1);
            if (!inimigo.estaMorto()) {
                System.out.println("  [1] Continuar atacando   [2] Fugir");
                System.out.print("  > ");
                if (!"1".equals(scanner.nextLine().trim())) {
                    System.out.println("  Você recuou das Minas.");
                    break;
                }
            }
        }
        if (!fazendeiro.temEnergia(12) && !inimigo.estaMorto()) {
            System.out.println("  Sem energia para continuar lutando!");
        }
    }
}
