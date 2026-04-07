package stardew.engine;

import stardew.enums.Localizacao;
import stardew.exceptions.FazendaException;
import stardew.exceptions.SaldoInsuficienteException;
import stardew.interfaces.Item;
import stardew.model.*;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Processa todas as ações que o jogador pode realizar no mundo.
 * Cada local tem seus próprios comandos; este controlador decide qual método chamar.
 */
class ControladorAcoes {

    private final Scanner scanner;
    private final Renderizador tela;
    private final NPC pierre;
    private final NPC willy;

    private Fazendeiro fazendeiro;
    private GerenciadorTempo tempo;

    private static final Random RAND = new Random();

    ControladorAcoes(Scanner scanner, Renderizador tela, NPC pierre, NPC willy) {
        this.scanner = scanner;
        this.tela    = tela;
        this.pierre  = pierre;
        this.willy   = willy;
    }

    // Deve ser chamado após a sessão ser inicializada
    void setEstado(Fazendeiro fazendeiro, GerenciadorTempo tempo) {
        this.fazendeiro = fazendeiro;
        this.tempo      = tempo;
    }

    // ── Roteamento por local ──────────────────────────────────────

    void processarAcaoLocal(String input) throws FazendaException {
        switch (fazendeiro.getLocalizacao()) {
            case FAZENDA  -> processarFazenda(input);
            case CIDADE   -> processarCidade(input);
            case PRAIA    -> processarPraia(input);
            case MINAS    -> processarMinas(input);
            case FLORESTA -> processarFloresta(input);
            default       -> System.out.println("  Ação desconhecida: " + input);
        }
    }

    private void processarFazenda(String input) throws FazendaException {
        switch (input) {
            case "p" -> menuPlantar();
            case "c" -> menuColher();
            case "g" -> menuIrrigar();
            case "a" -> tela.exibirResultado(fazendeiro.cuidarAnimais());
            default  -> System.out.println("  Ação inválida na Fazenda.");
        }
    }

    private void processarCidade(String input) throws FazendaException {
        switch (input) {
            case "n" -> interagirNPC(pierre);
            case "v" -> menuVender();
            case "o" -> menuComprar(pierre);
            default  -> System.out.println("  Ação inválida na Cidade.");
        }
    }

    private void processarPraia(String input) throws FazendaException {
        switch (input) {
            case "f" -> tela.exibirResultado(fazendeiro.pescar());
            case "n" -> interagirNPC(willy);
            default  -> System.out.println("  Ação inválida na Praia.");
        }
    }

    private void processarMinas(String input) throws FazendaException {
        switch (input) {
            case "n" -> tela.exibirResultado(fazendeiro.minerar());
            case "l" -> menuLutar();
            default  -> System.out.println("  Ação inválida nas Minas.");
        }
    }

    private void processarFloresta(String input) throws FazendaException {
        switch (input) {
            case "t" -> tela.exibirResultado(fazendeiro.cortarArvore());
            default  -> System.out.println("  Ação inválida na Floresta.");
        }
    }

    // ── Menus de ação ─────────────────────────────────────────────

    void menuMover() {
        System.out.println("\n  ══ Mapa ══════════════════════════════");
        System.out.println("  [1] Fazenda    [2] Cidade");
        System.out.println("  [3] Praia      [4] Minas    [5] Floresta");
        System.out.print("  > ");
        String opt = scanner.nextLine().trim();
        Localizacao destino = switch (opt) {
            case "1" -> Localizacao.FAZENDA;
            case "2" -> Localizacao.CIDADE;
            case "3" -> Localizacao.PRAIA;
            case "4" -> Localizacao.MINAS;
            case "5" -> Localizacao.FLORESTA;
            default  -> null;
        };
        if (destino == null) {
            System.out.println("  Destino inválido.");
            return;
        }
        if (destino == fazendeiro.getLocalizacao()) {
            System.out.println("  Você já está aqui!");
            return;
        }
        fazendeiro.moverPara(destino);
        tempo.avancarHora(1);
        tela.exibirResultado("Você foi para " + destino + ".");
    }

    private void menuPlantar() throws FazendaException {
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
        int idx = tela.lerInt() - 1;
        if (idx < 0 || idx >= sementes.size()) return;

        List<Parcela> livres = fazendeiro.getFazenda().parcelasLivres().toList();
        if (livres.isEmpty()) {
            System.out.println("  Sem parcelas livres!");
            return;
        }
        System.out.println("  Parcelas livres: " + livres.stream().map(p -> "#" + p.getId()).toList());
        System.out.print("  Número da parcela: ");
        int id = tela.lerInt();

        String resultado = fazendeiro.plantar((Semente) sementes.get(idx), id, tempo.getEstacao());
        tempo.avancarHora(1);
        tela.exibirResultado(resultado);
    }

    private void menuColher() throws FazendaException {
        List<Parcela> prontas = fazendeiro.getFazenda().parcelasProximasDeColher().toList();
        if (prontas.isEmpty()) {
            System.out.println("  Nenhuma parcela pronta para colher.");
            return;
        }
        System.out.println("  Prontas: " + prontas.stream()
                .map(p -> "#" + p.getId() + " (" + p.getSemente().getCulturaGerada() + ")")
                .toList());
        System.out.print("  Número da parcela (0 = todas): ");
        int id = tela.lerInt();
        if (id == 0) {
            for (Parcela p : prontas) {
                tela.exibirResultado(fazendeiro.colher(p.getId(), tempo.getEstacao()));
            }
        } else {
            tela.exibirResultado(fazendeiro.colher(id, tempo.getEstacao()));
        }
        tempo.avancarHora(1);
    }

    private void menuIrrigar() throws FazendaException {
        List<Parcela> plantadas = fazendeiro.getFazenda().parcelasPlantadas().toList();
        if (plantadas.isEmpty()) {
            System.out.println("  Nenhuma parcela plantada.");
            return;
        }
        System.out.println("  Plantadas: " + plantadas.stream().map(p -> "#" + p.getId()).toList());
        System.out.print("  Número da parcela (0 = todas): ");
        int id = tela.lerInt();
        if (id == 0) {
            for (Parcela p : plantadas) {
                tela.exibirResultado(fazendeiro.irrigar(p.getId()));
            }
        } else {
            tela.exibirResultado(fazendeiro.irrigar(id));
        }
        tempo.avancarHora(1);
    }

    private void menuLutar() throws FazendaException {
        Inimigo[] possiveis = {
            new Inimigo("Slime Verde",    30,  5, 20),
            new Inimigo("Morcego",        20,  8, 25),
            new Inimigo("Esqueleto",      50, 12, 40),
            new Inimigo("Golem de Pedra", 80, 18, 70)
        };
        Inimigo inimigo = possiveis[RAND.nextInt(possiveis.length)];
        System.out.println("\n  Um " + inimigo.getNome() + " apareceu!");
        System.out.println("  " + inimigo);
        System.out.println("  [1] Atacar   [2] Fugir");
        System.out.print("  > ");
        String opt = scanner.nextLine().trim();
        if (!"1".equals(opt)) {
            System.out.println("  Você fugiu!");
            return;
        }
        while (!inimigo.estaMorto() && fazendeiro.temEnergia(12)) {
            tela.exibirResultado(fazendeiro.lutar(inimigo));
            if (!inimigo.estaMorto()) {
                System.out.println("  [1] Continuar atacando   [2] Fugir");
                System.out.print("  > ");
                if (!"1".equals(scanner.nextLine().trim())) break;
            }
            tempo.avancarHora(1);
        }
    }

    private void menuVender() throws FazendaException {
        List<Item> vendaveis = fazendeiro.getInventario().getItens().stream()
                .filter(i -> !i.getTipo().equals("Ferramenta"))
                .toList();
        if (vendaveis.isEmpty()) {
            System.out.println("  Nada para vender.");
            return;
        }
        System.out.println("\n  Itens para vender:");
        for (int i = 0; i < vendaveis.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, vendaveis.get(i));
        }
        System.out.print("  Escolha (0 = cancelar): ");
        int idx = tela.lerInt() - 1;
        if (idx < 0 || idx >= vendaveis.size()) return;
        tela.exibirResultado(fazendeiro.vender(vendaveis.get(idx).getNome()));
    }

    private void menuComprar(NPC npc) throws FazendaException {
        List<Item> estoque = npc.getEstoque();
        System.out.println("\n  Loja de " + npc.getNome() + ":");
        for (int i = 0; i < estoque.size(); i++) {
            System.out.printf("  [%2d] %s%n", i + 1, estoque.get(i));
        }
        System.out.print("  Escolha (0 = sair): ");
        int idx = tela.lerInt() - 1;
        if (idx < 0 || idx >= estoque.size()) return;
        try {
            Item item = estoque.get(idx);
            fazendeiro.comprar(item);
            if (item instanceof Animal animal) {
                fazendeiro.getFazenda().adicionarAnimal(animal);
                fazendeiro.getInventario().removerItem(animal);
                tela.exibirResultado("Comprou " + animal.getNome() + " para a fazenda!");
            } else {
                tela.exibirResultado("Comprou: " + item.getNome());
            }
        } catch (SaldoInsuficienteException e) {
            tela.exibirErro(e.getMessage());
        }
    }

    private void interagirNPC(NPC npc) {
        System.out.println("\n  " + npc.agir());
        System.out.println("  Amizade com " + npc.getNome() + ": " + npc.getAmizade() + "/100");
    }
}
