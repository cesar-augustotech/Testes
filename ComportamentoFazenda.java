package stardew.engine.comportamento;

import stardew.engine.Renderer;
import stardew.exceptions.FazendaException;
import stardew.interfaces.Item;
import stardew.model.*;

import java.util.List;
import java.util.Scanner;

/**
 * Strategy Pattern — encapsula o menu e as ações da Fazenda.
 * GameEngine não conhece os detalhes: apenas chama processar().
 */
public class ComportamentoFazenda implements ComportamentoLocal {

    private final Scanner scanner;

    public ComportamentoFazenda(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void exibirMenu() {
        System.out.println("  ── Ações disponíveis ─────────────────────────────");
        System.out.println("  [p] Plantar      [c] Colher      [g] Irrigar");
        System.out.println("  [a] Cuidar animais");
    }

    @Override
    public void processar(String input, Fazendeiro fazendeiro, GerenciadorTempo tempo)
            throws FazendaException {
        switch (input) {
            case "p" -> menuPlantar(fazendeiro, tempo);
            case "c" -> menuColher(fazendeiro, tempo);
            case "g" -> menuIrrigar(fazendeiro);
            case "a" -> Renderer.exibirResultado(fazendeiro.cuidarAnimais());
            default  -> System.out.println("  Ação inválida na Fazenda.");
        }
    }

    // ── Menus privados de cada ação ───────────────────────────────

    private void menuPlantar(Fazendeiro fazendeiro, GerenciadorTempo tempo)
            throws FazendaException {
        List<Item> sementes = fazendeiro.getInventario().listarPorTipo("Semente");
        if (sementes.isEmpty()) {
            System.out.println("  Você não tem sementes!");
            return;
        }
        System.out.println("\n  Sementes disponíveis:");
        for (int i = 0; i < sementes.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, sementes.get(i));
        }
        System.out.print("  Escolha a semente (0 = cancelar): ");
        int idx = lerInt() - 1;
        if (idx < 0 || idx >= sementes.size()) return;

        List<Parcela> livres = fazendeiro.getFazenda().parcelasLivres().toList();
        if (livres.isEmpty()) { System.out.println("  Sem parcelas livres!"); return; }
        System.out.println("  Parcelas livres: " + livres.stream().map(p -> "#" + p.getId()).toList());
        System.out.print("  Número da parcela: ");
        int id = lerInt();

        Renderer.exibirResultado(fazendeiro.plantar((Semente) sementes.get(idx), id, tempo.getEstacao()));
        tempo.avancarHora(1);
    }

    private void menuColher(Fazendeiro fazendeiro, GerenciadorTempo tempo)
            throws FazendaException {
        List<Parcela> prontas = fazendeiro.getFazenda().parcelasProximasDeColher().toList();
        if (prontas.isEmpty()) { System.out.println("  Nenhuma parcela pronta para colher."); return; }
        System.out.println("  Prontas: " + prontas.stream()
            .map(p -> "#" + p.getId() + " (" + p.getSemente().getCulturaGerada() + ")").toList());
        System.out.print("  Parcela (0 = todas): ");
        int id = lerInt();
        if (id == 0) {
            for (Parcela p : prontas) Renderer.exibirResultado(fazendeiro.colher(p.getId(), tempo.getEstacao()));
        } else {
            Renderer.exibirResultado(fazendeiro.colher(id, tempo.getEstacao()));
        }
        tempo.avancarHora(1);
    }

    private void menuIrrigar(Fazendeiro fazendeiro) throws FazendaException {
        List<Parcela> plantadas = fazendeiro.getFazenda().parcelasPlantadas().toList();
        if (plantadas.isEmpty()) { System.out.println("  Nenhuma parcela plantada."); return; }
        System.out.println("  Plantadas: " + plantadas.stream().map(p -> "#" + p.getId()).toList());
        System.out.print("  Parcela (0 = todas): ");
        int id = lerInt();
        if (id == 0) {
            for (Parcela p : plantadas) Renderer.exibirResultado(fazendeiro.irrigar(p.getId()));
        } else {
            Renderer.exibirResultado(fazendeiro.irrigar(id));
        }
    }

    private int lerInt() {
        try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }
}
