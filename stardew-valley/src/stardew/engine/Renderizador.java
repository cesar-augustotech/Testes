package stardew.engine;

import stardew.enums.Localizacao;
import stardew.model.Animal;
import stardew.model.Fazendeiro;
import stardew.model.GerenciadorTempo;

import java.util.List;
import java.util.Scanner;

/**
 * Cuida de toda a saída visual do jogo no terminal.
 * Nada de lógica de negócio aqui — só impressão e leitura simples.
 */
class Renderizador {

    private final Scanner scanner;

    Renderizador(Scanner scanner) {
        this.scanner = scanner;
    }

    // ── Tela inicial ──────────────────────────────────────────────

    void exibirSplash() {
        System.out.println("""
                ███████╗████████╗ █████╗ ██████╗ ██████╗ ███████╗██╗    ██╗
                ██╔════╝╚══██╔══╝██╔══██╗██╔══██╗██╔══██╗██╔════╝██║    ██║
                ███████╗   ██║   ███████║██████╔╝██║  ██║█████╗  ██║ █╗ ██║
                ╚════██║   ██║   ██╔══██║██╔══██╗██║  ██║██╔══╝  ██║███╗██║
                ███████║   ██║   ██║  ██║██║  ██║██████╔╝███████╗╚███╔███╔╝
                ╚══════╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝ ╚══════╝ ╚══╝╚══╝\s
                
                ██╗   ██╗ █████╗ ██╗     ██╗     ███████╗██╗   ██╗        \s
                ██║   ██║██╔══██╗██║     ██║     ██╔════╝╚██╗ ██╔╝        \s
                ██║   ██║███████║██║     ██║     █████╗   ╚████╔╝         \s
                ╚██╗ ██╔╝██╔══██║██║     ██║     ██╔══╝    ╚██╔╝          \s
                 ╚████╔╝ ██║  ██║███████╗███████╗███████╗   ██║           \s
                  ╚═══╝  ╚═╝  ╚═╝╚══════╝╚══════╝╚══════╝   ╚═╝           \s
                """);
        System.out.println();
    }

    // ── HUD e menus do loop principal ─────────────────────────────

    void exibirHUD(Fazendeiro fazendeiro, GerenciadorTempo tempo) {
        System.out.println("  ┌──────────────────────────────────────────────────────┐");
        System.out.printf ("  │  %s%n", tempo.toString());
        System.out.printf ("  │  Fazendeiro: %-12s  Energia: %3d/270%n",
                fazendeiro.getNome(), fazendeiro.getEnergia());
        System.out.printf ("  │  Ouro: G%-8.0f  Local: %-12s%n",
                fazendeiro.getDinheiro(), fazendeiro.getLocalizacao());
        System.out.println("  └──────────────────────────────────────────────────────┘");
        System.out.println();
    }

    void exibirMenuLocal(Fazendeiro fazendeiro) {
        Localizacao local = fazendeiro.getLocalizacao();
        System.out.println(mapaASCII(local));
        System.out.println("  ── Ações disponíveis ─────────────────────────────");
        switch (local) {
            case FAZENDA  -> {
                System.out.println("  [p] Plantar      [c] Colher      [g] Irrigar");
                System.out.println("  [a] Cuidar animais");
            }
            case CIDADE   -> System.out.println("  [n] Conversar c/ Pierre   [o] Comprar   [v] Vender");
            case PRAIA    -> System.out.println("  [f] Pescar   [n] Conversar c/ Willy");
            case MINAS    -> System.out.println("  [n] Minerar   [l] Lutar");
            case FLORESTA -> System.out.println("  [t] Cortar árvore");
        }
        System.out.println("  ─────────────────────────────────────────────────");
        System.out.println("  [m] Mapa/Mover   [i] Inventário   [h] Habilidades");
        System.out.println("  [r] Relatório    [d] Dormir       [s] Salvar   [q] Sair");
        System.out.print("  > ");
    }

    private String mapaASCII(Localizacao local) {
        String f = local == Localizacao.FAZENDA  ? "[◆FAZENDA◆]" : "[ Fazenda  ]";
        String c = local == Localizacao.CIDADE   ? "[◆CIDADE◆ ]" : "[ Cidade   ]";
        String p = local == Localizacao.PRAIA    ? "[◆ PRAIA ◆]" : "[  Praia   ]";
        String m = local == Localizacao.MINAS    ? "[◆ MINAS ◆]" : "[  Minas   ]";
        String o = local == Localizacao.FLORESTA ? "[◆FLORESTA◆]" : "[ Floresta ]";
        return String.format(
                "  ┌────────────────────────────────────────┐%n" +
                "  │  %s ── %s           │%n" +
                "  │       |              |                 │%n" +
                "  │  %s      %s        │%n" +
                "  │              |                         │%n" +
                "  │         %s                   │%n" +
                "  └────────────────────────────────────────┘",
                f, c, p, m, o);
    }

    // ── Telas de informação do jogador ────────────────────────────

    void exibirInventario(Fazendeiro fazendeiro) {
        System.out.println("\n  ══ Inventário ("
                + fazendeiro.getInventario().getOcupado()
                + "/" + fazendeiro.getInventario().getCapacidade() + ") ═══════════════");
        System.out.println(fazendeiro.getInventario());
        System.out.printf("  Valor total dos itens: G%.0f%n", fazendeiro.getInventario().valorTotal());
        fazendeiro.getInventario().contagemPorTipo()
                .forEach((tipo, qtd) -> System.out.printf("  • %-12s: %d item(ns)%n", tipo, qtd));
    }

    void exibirHabilidades(Fazendeiro fazendeiro) {
        System.out.println("\n  ══ Status de " + fazendeiro.getNome() + " ════════════════");
        System.out.println("  " + fazendeiro.agir());
        System.out.println("\n  ── Habilidades ──");
        System.out.println(fazendeiro.getHabilidades());
    }

    void exibirRelatorioFazenda(Fazendeiro fazendeiro, GerenciadorTempo tempo) {
        System.out.println("\n  ══ Relatório da Fazenda ═══════════════════");
        System.out.println(fazendeiro.getFazenda().relatorio(tempo.getEstacao()));
        System.out.println("\n  ── Parcelas ──");
        fazendeiro.getFazenda().getParcelas().forEach(p -> System.out.println("  " + p));
        System.out.println("\n  ── Animais ──");
        List<Animal> animais = fazendeiro.getFazenda().getAnimais();
        if (animais.isEmpty()) {
            System.out.println("  Nenhum animal.");
        } else {
            animais.forEach(a -> System.out.println("  " + a));
        }
    }

    // Tela de manhã mostrada após dormir
    void exibirManha(GerenciadorTempo tempo, Fazendeiro fazendeiro) {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════╗");
        System.out.printf ("  ║  Bom dia! %s%n", tempo.getDataFormatada());
        System.out.println("  ║  Energia restaurada.             ║");
        System.out.println("  ╚══════════════════════════════════╝");
        long prontas = fazendeiro.getFazenda().parcelasProximasDeColher().count();
        if (prontas > 0) {
            System.out.printf("  >> %d parcela(s) pronta(s) para colher!%n", prontas);
        }
    }

    // ── Utilitários de saída / entrada ────────────────────────────

    void exibirResultado(String mensagem) {
        System.out.println("\n  >> " + mensagem);
    }

    void exibirErro(String mensagem) {
        System.out.println("\n  !! ERRO: " + mensagem);
    }

    void pausar() {
        System.out.print("  [Enter para continuar]");
        scanner.nextLine();
    }

    void limparTela() {
        for (int i = 0; i < 50; i++) System.out.println();
    }

    int lerInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
