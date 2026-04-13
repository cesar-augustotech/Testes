package stardew.engine.comportamento;

import stardew.engine.Renderer;
import stardew.exceptions.FazendaException;
import stardew.exceptions.SaldoInsuficienteException;
import stardew.interfaces.Item;
import stardew.model.*;

import java.util.List;
import java.util.Scanner;

/**
 * Strategy Pattern — encapsula o menu e as ações da Cidade.
 */
public class ComportamentoCidade implements ComportamentoLocal {

    private final Scanner scanner;
    private final NPC pierre;

    public ComportamentoCidade(Scanner scanner) {
        this.scanner = scanner;
        this.pierre  = new NPC("Pierre", "Lojista", stardew.enums.Localizacao.CIDADE);
    }

    @Override
    public void exibirMenu() {
        System.out.println("  ── Ações disponíveis ─────────────────────────────");
        System.out.println("  [n] Conversar c/ Pierre   [o] Comprar   [v] Vender");
    }

    @Override
    public void processar(String input, Fazendeiro fazendeiro, GerenciadorTempo tempo)
            throws FazendaException {
        switch (input) {
            case "n" -> interagirNPC(pierre);
            case "o" -> menuComprar(pierre, fazendeiro);
            case "v" -> menuVender(fazendeiro);
            default  -> System.out.println("  Ação inválida na Cidade.");
        }
    }

    // ── Menus privados ────────────────────────────────────────────

    private void interagirNPC(NPC npc) {
        System.out.println("\n  " + npc.agir());
        System.out.println("  Amizade com " + npc.getNome() + ": " + npc.getAmizade() + "/100");
    }

    private void menuComprar(NPC npc, Fazendeiro fazendeiro) {
        List<Item> estoque = npc.getEstoque();
        System.out.printf("%n  Loja de %s:%n", npc.getNome());
        for (int i = 0; i < estoque.size(); i++) {
            System.out.printf("  [%2d] %s%n", i + 1, estoque.get(i));
        }
        System.out.print("  Escolha (0 = sair): ");
        int idx = lerInt() - 1;
        if (idx < 0 || idx >= estoque.size()) return;
        try {
            Item item = estoque.get(idx);
            fazendeiro.comprar(item);
            if (item instanceof Animal animal) {
                fazendeiro.getFazenda().adicionarAnimal(animal);
                fazendeiro.getInventario().removerItem(animal);
                Renderer.exibirResultado("Comprou " + animal.getNomeAnimal() + " para a fazenda!");
            } else {
                Renderer.exibirResultado("Comprou: " + item.getNome());
            }
        } catch (SaldoInsuficienteException e) {
            Renderer.exibirErro(e.getMessage());
        }
    }

    private void menuVender(Fazendeiro fazendeiro) throws FazendaException {
        List<Item> vendaveis = fazendeiro.getInventario().getItens().stream()
                .filter(i -> !i.getTipo().equals("Ferramenta"))
                .toList();
        if (vendaveis.isEmpty()) { System.out.println("  Nada para vender."); return; }
        System.out.println("\n  Itens para vender:");
        for (int i = 0; i < vendaveis.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, vendaveis.get(i));
        }
        System.out.print("  Escolha (0 = cancelar): ");
        int idx = lerInt() - 1;
        if (idx < 0 || idx >= vendaveis.size()) return;
        Renderer.exibirResultado(fazendeiro.vender(vendaveis.get(idx).getNome()));
    }

    private int lerInt() {
        try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }
}
