package stardew.engine;

import stardew.enums.Localizacao;
import stardew.exceptions.AcaoInvalidaException;
import stardew.exceptions.ColheitaForaDeEstacaoException;
import stardew.exceptions.FazendaException;
import stardew.exceptions.SaldoInsuficienteException;
import stardew.interfaces.Item;
import stardew.model.*;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Motor principal do jogo.
 * Gerencia o loop de game, renderização ASCII e tratamento de input.
 *
 * Conceitos aplicados:
 *   - Polimorfismo (chama agir() em Fazendeiro e NPC)
 *   - Exceções personalizadas (capturadas aqui, exibidas ao jogador)
 *   - Streams (relatórios, listagens)
 *   - Persistência (salva ao dormir, carrega ao iniciar)
 */
public class GameEngine {

    private Fazendeiro      fazendeiro;
    private GerenciadorTempo tempo;
    private final PersistenciaCSV persistencia;
    private final Scanner   scanner;
    private boolean         rodando;

    // NPCs do mundo
    private final NPC pierre;  // lojista
    private final NPC willy;   // pescador

    private static final Random RAND = new Random();

    public GameEngine() {
        this.persistencia = new PersistenciaCSV();
        this.scanner      = new Scanner(System.in);
        this.pierre = new NPC("Pierre",  "Lojista",   Localizacao.CIDADE);
        this.willy  = new NPC("Willy",   "Pescador",  Localizacao.PRAIA);
    }

    // ══════════════════════════════════════════════════════════════
    //  ENTRADA DO JOGO
    // ══════════════════════════════════════════════════════════════

    public void iniciar() {
        limparTela();
        exibirSplash();
        if (persistencia.saveExiste()) {
            System.out.println("  [1] Continuar jogo salvo");
            System.out.println("  [2] Novo jogo");
            System.out.print("  > ");
            String opt = scanner.nextLine().trim();
            if ("1".equals(opt)) {
                carregarJogo();
            } else {
                novoJogo();
            }
        } else {
            novoJogo();
        }
        rodando = true;
        loopPrincipal();
    }

    private void novoJogo() {
        System.out.print("\n  Qual é o nome do seu fazendeiro? > ");
        String nome = scanner.nextLine().trim();
        if (nome.isBlank()) nome = "Jogador";
        System.out.print("  Nome da sua fazenda? > ");
        String nomeFazenda = scanner.nextLine().trim();
        if (nomeFazenda.isBlank()) nomeFazenda = "Fazenda do Vale";
        fazendeiro = new Fazendeiro(nome, nomeFazenda);
        tempo      = new GerenciadorTempo();
        System.out.println("\n  Bem-vindo a " + nomeFazenda + ", " + nome + "! Boa sorte!");
        pausar();
    }

    private void carregarJogo() {
        try {
            tempo      = persistencia.carregarTempo();
            var inv    = persistencia.carregarInventario();
            var fazenda= persistencia.carregarFazenda();
            fazendeiro = persistencia.carregarFazendeiro(inv, fazenda);
            System.out.println("\n  Jogo carregado! " + tempo.getDataFormatada());
            pausar();
        } catch (IOException e) {
            System.out.println("  Erro ao carregar save: " + e.getMessage());
            System.out.println("  Iniciando novo jogo...");
            novoJogo();
        }
    }

    private void salvarJogo() {
        try {
            persistencia.salvarTudo(fazendeiro, tempo);
            System.out.println("  >> Jogo salvo com sucesso!");
        } catch (IOException e) {
            System.out.println("  ERRO ao salvar: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  LOOP PRINCIPAL
    // ══════════════════════════════════════════════════════════════

    private void loopPrincipal() {
        while (rodando) {
            limparTela();
            exibirHUD();
            exibirMenuLocal();
            String input = scanner.nextLine().trim().toLowerCase();
            processarInput(input);
        }
    }

    private void processarInput(String input) {
        try {
            switch (input) {
                case "m"              -> menuMover();
                case "i"              -> exibirInventario();
                case "h"              -> exibirHabilidades();
                case "r"              -> exibirRelatorioFazenda();
                case "d"              -> dormir();
                case "s"              -> salvarJogo();
                case "q"              -> confirmarSaida();
                default               -> processarAcaoLocal(input);
            }
        } catch (AcaoInvalidaException e) {
            exibirErro(e.getMessage());
        } catch (ColheitaForaDeEstacaoException e) {
            exibirErro(e.getMessage());
        } catch (SaldoInsuficienteException e) {
            exibirErro(e.getMessage());
        } catch (FazendaException e) {
            exibirErro(e.getMessage());
        }
        if (!input.equals("d") && !input.equals("q")) pausar();
    }

    private void processarAcaoLocal(String input) throws FazendaException {
        Localizacao local = fazendeiro.getLocalizacao();
        switch (local) {
            case FAZENDA -> processarFazenda(input);
            case CIDADE  -> processarCidade(input);
            case PRAIA   -> processarPraia(input);
            case MINAS   -> processarMinas(input);
            case FLORESTA-> processarFloresta(input);
            default      -> System.out.println("  Ação desconhecida: " + input);
        }
    }

    // ── Ações por local ──────────────────────────────────────────

    private void processarFazenda(String input) throws FazendaException {
        switch (input) {
            case "p" -> menuPlantar();
            case "c" -> menuColher();
            case "g" -> menuIrrigar();
            case "a" -> exibirResultado(fazendeiro.cuidarAnimais());
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
            case "f" -> exibirResultado(fazendeiro.pescar());
            case "n" -> interagirNPC(willy);
            default  -> System.out.println("  Ação inválida na Praia.");
        }
    }

    private void processarMinas(String input) throws FazendaException {
        switch (input) {
            case "n" -> exibirResultado(fazendeiro.minerar());
            case "l" -> menuLutar();
            default  -> System.out.println("  Ação inválida nas Minas.");
        }
    }

    private void processarFloresta(String input) throws FazendaException {
        switch (input) {
            case "t" -> exibirResultado(fazendeiro.cortarArvore());
            default  -> System.out.println("  Ação inválida na Floresta.");
        }
    }

    // ── Menus de ação ────────────────────────────────────────────

    private void menuMover() {
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
        if (destino == null) { System.out.println("  Destino inválido."); return; }
        if (destino == fazendeiro.getLocalizacao()) {
            System.out.println("  Você já está aqui!"); return;
        }
        fazendeiro.moverPara(destino);
        tempo.avancarHora(1);
        exibirResultado("Você foi para " + destino + ".");
    }

    private void menuPlantar() throws FazendaException {
        List<Item> sementes = fazendeiro.getInventario().listarPorTipo("Semente");
        if (sementes.isEmpty()) { System.out.println("  Você não tem sementes!"); return; }
        System.out.println("\n  Sementes disponíveis:");
        for (int i = 0; i < sementes.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, sementes.get(i));
        }
        System.out.print("  Escolha a semente (0 = cancelar): ");
        int idx = lerInt() - 1;
        if (idx < 0 || idx >= sementes.size()) return;

        // Mostrar parcelas livres
        List<Parcela> livres = fazendeiro.getFazenda().parcelasLivres().toList();
        if (livres.isEmpty()) { System.out.println("  Sem parcelas livres!"); return; }
        System.out.println("  Parcelas livres: " + livres.stream().map(p -> "#" + p.getId()).toList());
        System.out.print("  Número da parcela: ");
        int id = lerInt();

        String resultado = fazendeiro.plantar(
            (Semente) sementes.get(idx), id, tempo.getEstacao()
        );
        tempo.avancarHora(1);
        exibirResultado(resultado);
    }

    private void menuColher() throws FazendaException {
        List<Parcela> prontas = fazendeiro.getFazenda().parcelasProximasDeColher().toList();
        if (prontas.isEmpty()) { System.out.println("  Nenhuma parcela pronta para colher."); return; }
        System.out.println("  Prontas: " + prontas.stream().map(p -> "#" + p.getId() + " (" + p.getSemente().getCulturaGerada() + ")").toList());
        System.out.print("  Número da parcela (0 = todas): ");
        int id = lerInt();
        if (id == 0) {
            for (Parcela p : prontas) {
                exibirResultado(fazendeiro.colher(p.getId(), tempo.getEstacao()));
            }
        } else {
            exibirResultado(fazendeiro.colher(id, tempo.getEstacao()));
        }
        tempo.avancarHora(1);
    }

    private void menuIrrigar() throws FazendaException {
        List<Parcela> plantadas = fazendeiro.getFazenda().parcelasPlantadas().toList();
        if (plantadas.isEmpty()) { System.out.println("  Nenhuma parcela plantada."); return; }
        System.out.println("  Plantadas: " + plantadas.stream().map(p -> "#" + p.getId()).toList());
        System.out.print("  Número da parcela (0 = todas): ");
        int id = lerInt();
        if (id == 0) {
            for (Parcela p : plantadas) {
                exibirResultado(fazendeiro.irrigar(p.getId()));
            }
        } else {
            exibirResultado(fazendeiro.irrigar(id));
        }
        tempo.avancarHora(1);
    }

    private void menuLutar() throws FazendaException {
        Inimigo[] inimigos = {
            new Inimigo("Slime Verde",  30, 5, 20),
            new Inimigo("Morcego",      20, 8, 25),
            new Inimigo("Esqueleto",    50, 12, 40),
            new Inimigo("Golem de Pedra", 80, 18, 70)
        };
        Inimigo inimigo = inimigos[RAND.nextInt(inimigos.length)];
        System.out.println("\n  Um " + inimigo.getNome() + " apareceu!");
        System.out.println("  " + inimigo);
        System.out.println("  [1] Atacar   [2] Fugir");
        System.out.print("  > ");
        String opt = scanner.nextLine().trim();
        if ("1".equals(opt)) {
            // Loop de combate
            while (!inimigo.estaMorto() && fazendeiro.temEnergia(12)) {
                exibirResultado(fazendeiro.lutar(inimigo));
                if (!inimigo.estaMorto()) {
                    System.out.println("  [1] Continuar atacando   [2] Fugir");
                    System.out.print("  > ");
                    if (!"1".equals(scanner.nextLine().trim())) break;
                }
                tempo.avancarHora(1);
            }
        } else {
            System.out.println("  Você fugiu!");
        }
    }

    private void menuVender() throws FazendaException {
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
        exibirResultado(fazendeiro.vender(vendaveis.get(idx).getNome()));
    }

    private void menuComprar(NPC npc) throws FazendaException {
        List<Item> estoque = npc.getEstoque();
        System.out.println("\n  Loja de " + npc.getNome() + ":");
        for (int i = 0; i < estoque.size(); i++) {
            System.out.printf("  [%2d] %s%n", i + 1, estoque.get(i));
        }
        System.out.print("  Escolha (0 = sair): ");
        int idx = lerInt() - 1;
        if (idx < 0 || idx >= estoque.size()) return;
        try {
            Item item = estoque.get(idx);
            fazendeiro.comprar(item);
            // Se for animal, adiciona à fazenda
            if (item instanceof Animal animal) {
                fazendeiro.getFazenda().adicionarAnimal(animal);
                fazendeiro.getInventario().removerItem(animal);
                exibirResultado("Comprou " + animal.getNome() + " para a fazenda!");
            } else {
                exibirResultado("Comprou: " + item.getNome());
            }
        } catch (SaldoInsuficienteException e) {
            exibirErro(e.getMessage());
        }
    }

    private void interagirNPC(NPC npc) {
        // Polimorfismo: chama agir() do NPC (que retorna diálogo)
        System.out.println("\n  " + npc.agir());
        System.out.println("  Amizade com " + npc.getNome() + ": " + npc.getAmizade() + "/100");
    }

    // ── Dormir / fim de dia ───────────────────────────────────────

    private void dormir() {
        System.out.println("\n  Você foi dormir...");
        fazendeiro.descansar();
        fazendeiro.getFazenda().avancarDia(tempo.getEstacao());
        tempo.avancarDia();
        salvarJogo();
        limparTela();
        System.out.println();
        System.out.println("  ╔══════════════════════════════════╗");
        System.out.printf ("  ║  Bom dia! %s%n", tempo.getDataFormatada());
        System.out.println("  ║  Energia restaurada.             ║");
        System.out.println("  ╚══════════════════════════════════╝");
        // Mostrar parcelas prontas
        long prontas = fazendeiro.getFazenda().parcelasProximasDeColher().count();
        if (prontas > 0)
            System.out.printf("  >> %d parcela(s) pronta(s) para colher!%n", prontas);
        pausar();
    }

    private void confirmarSaida() {
        System.out.print("  Deseja salvar antes de sair? [s/n]: ");
        if ("s".equalsIgnoreCase(scanner.nextLine().trim())) salvarJogo();
        System.out.println("  Até a próxima, " + fazendeiro.getNome() + "!");
        rodando = false;
    }

    // ══════════════════════════════════════════════════════════════
    //  RENDERIZAÇÃO ASCII
    // ══════════════════════════════════════════════════════════════

    private void exibirSplash() {
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

    private void exibirHUD() {
        Localizacao local = fazendeiro.getLocalizacao();
        System.out.println("  ┌──────────────────────────────────────────────────────┐");
        System.out.printf ("  │  %s%n", tempo.toString());
        System.out.printf ("  │  Fazendeiro: %-12s  Energia: %3d/270%n",
            fazendeiro.getNome(), fazendeiro.getEnergia());
        System.out.printf ("  │  Ouro: G%-8.0f  Local: %-12s%n",
            fazendeiro.getDinheiro(), local);
        System.out.println("  └──────────────────────────────────────────────────────┘");
        System.out.println();
    }

    private void exibirMenuLocal() {
        Localizacao local = fazendeiro.getLocalizacao();
        System.out.println(exibirMapaASCII(local));
        System.out.println("  ── Ações disponíveis ─────────────────────────────");
        switch (local) {
            case FAZENDA -> {
                System.out.println("  [p] Plantar      [c] Colher      [g] Irrigar");
                System.out.println("  [a] Cuidar animais");
            }
            case CIDADE -> {
                System.out.println("  [n] Conversar c/ Pierre   [o] Comprar   [v] Vender");
            }
            case PRAIA -> {
                System.out.println("  [f] Pescar   [n] Conversar c/ Willy");
            }
            case MINAS -> {
                System.out.println("  [n] Minerar   [l] Lutar");
            }
            case FLORESTA -> {
                System.out.println("  [t] Cortar árvore");
            }
        }
        System.out.println("  ─────────────────────────────────────────────────");
        System.out.println("  [m] Mapa/Mover   [i] Inventário   [h] Habilidades");
        System.out.println("  [r] Relatório    [d] Dormir       [s] Salvar   [q] Sair");
        System.out.print("  > ");
    }

    private String exibirMapaASCII(Localizacao localAtual) {
        // Mini-mapa fixo com marcador na posição atual
        String f = localAtual == Localizacao.FAZENDA  ? "[◆FAZENDA◆]" : "[ Fazenda  ]";
        String c = localAtual == Localizacao.CIDADE   ? "[◆CIDADE◆ ]" : "[ Cidade   ]";
        String p = localAtual == Localizacao.PRAIA    ? "[◆ PRAIA ◆]" : "[  Praia   ]";
        String m = localAtual == Localizacao.MINAS    ? "[◆ MINAS ◆]" : "[  Minas   ]";
        String o = localAtual == Localizacao.FLORESTA ? "[◆FLORESTA◆]": "[ Floresta ]";
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

    private void exibirInventario() {
        System.out.println("\n  ══ Inventário (" + fazendeiro.getInventario().getOcupado()
            + "/" + fazendeiro.getInventario().getCapacidade() + ") ═══════════════");
        System.out.println(fazendeiro.getInventario());
        System.out.printf("  Valor total dos itens: G%.0f%n",
            fazendeiro.getInventario().valorTotal());
        // Resumo por tipo via Streams
        fazendeiro.getInventario().contagemPorTipo()
            .forEach((tipo, qtd) -> System.out.printf("  • %-12s: %d item(ns)%n", tipo, qtd));
    }

    private void exibirHabilidades() {
        // Polimorfismo: chama agir() do Fazendeiro
        System.out.println("\n  ══ Status de " + fazendeiro.getNome() + " ════════════════");
        System.out.println("  " + fazendeiro.agir());
        System.out.println("\n  ── Habilidades ──");
        System.out.println(fazendeiro.getHabilidades());
    }

    private void exibirRelatorioFazenda() {
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

    // ══════════════════════════════════════════════════════════════
    //  Utilidades de UI
    // ══════════════════════════════════════════════════════════════

    private void exibirResultado(String mensagem) {
        System.out.println("\n  >> " + mensagem);
    }

    private void exibirErro(String mensagem) {
        System.out.println("\n  !! ERRO: " + mensagem);
    }

    private void pausar() {
        System.out.print("  [Enter para continuar]");
        scanner.nextLine();
    }

    private void limparTela() {
        for (int i = 0; i < 50; i++) System.out.println();
    }

    private int lerInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
