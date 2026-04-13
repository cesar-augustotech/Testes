package stardew.engine;

import stardew.enums.Localizacao;
import stardew.model.*;

import java.util.List;

/**
 * Single Responsibility Principle — responsável exclusivamente pela
 * renderização ASCII no terminal. Nenhuma lógica de negócio aqui.
 *
 * Todos os métodos são estáticos: Renderer é um utilitário de exibição,
 * não um objeto com estado próprio.
 */
public final class Renderer {

    private Renderer() {}

    // ── Telas globais ─────────────────────────────────────────────

    public static void exibirSplash() {
        System.out.println();
        System.out.println("  ┌─────────────────────────────────────────────┐");
        System.out.println("  │                                             │");
        System.out.println("  │   ░██████╗████████╗░█████╗░██████╗██╗    ██╗│");
        System.out.println("  │   ██╔════╝╚══██╔══╝██╔══██╗██╔══██╗██║    ██║│");
        System.out.println("  │   ╚█████╗    ██║   ███████║██████╔╝██║ █╗ ██║│");
        System.out.println("  │    ╚═══██╗   ██║   ██╔══██║██╔══██╗██║███╗██║│");
        System.out.println("  │   ██████╔╝   ██║   ██║  ██║██║  ██║╚███╔███╔╝│");
        System.out.println("  │   ╚═════╝    ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝ ╚══╝╚══╝│");
        System.out.println("  │         F A R M   S I M U L A T O R          │");
        System.out.println("  │                                             │");
        System.out.println("  └─────────────────────────────────────────────┘");
        System.out.println();
    }

    public static void exibirHUD(Fazendeiro fazendeiro, GerenciadorTempo tempo) {
        System.out.println("  ┌──────────────────────────────────────────────────────┐");
        System.out.printf ("  │  %s%n", tempo);
        System.out.printf ("  │  Fazendeiro: %-12s  Energia: %3d/270%n",
            fazendeiro.getNome(), fazendeiro.getEnergia());
        System.out.printf ("  │  Ouro: G%-8.0f  Local: %-12s%n",
            fazendeiro.getDinheiro(), fazendeiro.getLocalizacao());
        System.out.println("  └──────────────────────────────────────────────────────┘");
        System.out.println();
    }

    public static void exibirMapaASCII(Localizacao localAtual) {
        String f = localAtual == Localizacao.FAZENDA  ? "[◆FAZENDA◆]" : "[ Fazenda  ]";
        String c = localAtual == Localizacao.CIDADE   ? "[◆CIDADE◆ ]" : "[ Cidade   ]";
        String p = localAtual == Localizacao.PRAIA    ? "[◆ PRAIA ◆]" : "[  Praia   ]";
        String m = localAtual == Localizacao.MINAS    ? "[◆ MINAS ◆]" : "[  Minas   ]";
        String o = localAtual == Localizacao.FLORESTA ? "[◆FLORESTA◆]": "[ Floresta ]";
        System.out.printf(
            "  ┌────────────────────────────────────────┐%n" +
            "  │  %s ── %s  │%n" +
            "  │       |              |            │%n" +
            "  │  %s      %s         │%n" +
            "  │              |                    │%n" +
            "  │         %s                   │%n" +
            "  └────────────────────────────────────────┘%n",
            f, c, p, m, o);
    }

    public static void exibirMenuGlobal() {
        System.out.println("  ─────────────────────────────────────────────────");
        System.out.println("  [m] Mapa/Mover   [i] Inventário   [h] Habilidades");
        System.out.println("  [r] Relatório    [d] Dormir       [s] Salvar   [q] Sair");
        System.out.print("  > ");
    }

    // ── Telas de informação ───────────────────────────────────────

    public static void exibirInventario(Fazendeiro fazendeiro) {
        Inventario inv = fazendeiro.getInventario();
        System.out.printf("%n  ══ Inventário (%d/%d) ═══════════════%n",
            inv.getOcupado(), inv.getCapacidade());
        System.out.println(inv);
        System.out.printf("  Valor total dos itens: G%.0f%n", inv.valorTotal());
        inv.contagemPorTipo()
           .forEach((tipo, qtd) -> System.out.printf("  • %-12s: %d item(ns)%n", tipo, qtd));
    }

    public static void exibirHabilidades(Fazendeiro fazendeiro) {
        System.out.printf("%n  ══ Status de %s ════════════════%n", fazendeiro.getNome());
        System.out.println("  " + fazendeiro.agir());
        System.out.println("\n  ── Habilidades ──");
        System.out.println(fazendeiro.getHabilidades());
    }

    public static void exibirRelatorioFazenda(Fazendeiro fazendeiro, GerenciadorTempo tempo) {
        System.out.println("\n  ══ Relatório da Fazenda ═══════════════════");
        System.out.println(fazendeiro.getFazenda().relatorio(tempo.getEstacao()));
        System.out.println("\n  ── Parcelas ──");
        fazendeiro.getFazenda().getParcelas()
            .forEach(p -> System.out.println("  " + p));
        System.out.println("\n  ── Animais ──");
        List<Animal> animais = fazendeiro.getFazenda().getAnimais();
        if (animais.isEmpty()) System.out.println("  Nenhum animal.");
        else animais.forEach(a -> System.out.println("  " + a));
    }

    public static void exibirBoasManha(GerenciadorTempo tempo, long parcelasProximas) {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════╗");
        System.out.printf ("  ║  Bom dia! %s%n", tempo.getDataFormatada());
        System.out.println("  ║  Energia restaurada.             ║");
        System.out.println("  ╚══════════════════════════════════╝");
        if (parcelasProximas > 0)
            System.out.printf("  >> %d parcela(s) pronta(s) para colher!%n", parcelasProximas);
    }

    // ── Mensagens pontuais ────────────────────────────────────────

    public static void exibirResultado(String mensagem) {
        System.out.println("\n  >> " + mensagem);
    }

    public static void exibirErro(String mensagem) {
        System.out.println("\n  !! ERRO: " + mensagem);
    }

    public static void exibirNivelUp(String nomeHabilidade, int novoNivel) {
        System.out.printf("%n  ╔══ NÍVEL UP! ══════════════════════╗%n");
        System.out.printf("  ║  %-20s → Nv.%-2d      ║%n", nomeHabilidade, novoNivel);
        System.out.printf("  ╚═══════════════════════════════════╝%n");
    }

    public static void pausar(java.util.Scanner scanner) {
        System.out.print("  [Enter para continuar]");
        scanner.nextLine();
    }

    public static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
