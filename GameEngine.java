package stardew.engine;

import stardew.engine.comportamento.*;
import stardew.enums.Localizacao;
import stardew.exceptions.FazendaException;
import stardew.interfaces.EventoDiaObserver;
import stardew.interfaces.RepositorioJogo;
import stardew.model.*;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

/**
 * Motor principal do jogo — orquestrador enxuto.
 *
 * Refatorações aplicadas:
 *
 * ── SOLID ────────────────────────────────────────────────────────
 * SRP: Renderização extraída para Renderer. Comportamento por local
 *      extraído para ComportamentoLocal. GameEngine apenas orquestra.
 * OCP: Novos locais não exigem alterar GameEngine — apenas criar
 *      novo ComportamentoLocal e registrar no mapa de comportamentos.
 * DIP: Depende de RepositorioJogo (interface), não de PersistenciaCSV
 *      (implementação). Injetado pelo construtor (injeção de dependência).
 *
 * ── Design Patterns ──────────────────────────────────────────────
 * Strategy   — mapa de ComportamentoLocal elimina os switches de Localizacao.
 * Observer   — registra EventoDiaObserver em GerenciadorTempo para desacoplar
 *              a virada de dia. Registra NivelUpObserver em Habilidades.
 * Adapter    — RepositorioJogo abstrai o formato de persistência.
 */
public class GameEngine {

    // ── Dependências injetadas (DIP) ──────────────────────────────
    private final RepositorioJogo repositorio;
    private final Scanner         scanner;

    // ── Estado do jogo ────────────────────────────────────────────
    private Fazendeiro       fazendeiro;
    private GerenciadorTempo tempo;
    private boolean          rodando;

    /**
     * Strategy Pattern — mapa de comportamentos por localização.
     * Substitui os dois switches paralelos sobre Localizacao que
     * existiam no GameEngine original.
     */
    private Map<Localizacao, ComportamentoLocal> comportamentos;

    // ── Construtor com injeção de dependência ─────────────────────

    public GameEngine(RepositorioJogo repositorio) {
        this.repositorio = repositorio;
        this.scanner     = new Scanner(System.in);
    }

    // ══════════════════════════════════════════════════════════════
    //  ENTRADA DO JOGO
    // ══════════════════════════════════════════════════════════════

    public void iniciar() {
        Renderer.limparTela();
        Renderer.exibirSplash();

        if (repositorio.saveExiste()) {
            System.out.println("  [1] Continuar jogo salvo");
            System.out.println("  [2] Novo jogo");
            System.out.print("  > ");
            if ("1".equals(scanner.nextLine().trim())) carregarJogo();
            else novoJogo();
        } else {
            novoJogo();
        }

        inicializarComportamentos();
        registrarObservers();

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
        Renderer.pausar(scanner);
    }

    private void carregarJogo() {
        try {
            tempo      = repositorio.carregarTempo();
            var inv    = repositorio.carregarInventario();
            var fazenda= repositorio.carregarFazenda();
            fazendeiro = repositorio.carregarFazendeiro(inv, fazenda);
            System.out.println("\n  Jogo carregado! " + tempo.getDataFormatada());
            Renderer.pausar(scanner);
        } catch (IOException e) {
            System.out.println("  Erro ao carregar save: " + e.getMessage());
            System.out.println("  Iniciando novo jogo...");
            novoJogo();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  INICIALIZAÇÃO: Strategy + Observer
    // ══════════════════════════════════════════════════════════════

    /**
     * Strategy Pattern — registra todas as estratégias de localização.
     * Aberto para extensão: adicionar Localizacao.TAVERNA = criar
     * ComportamentoTaverna e incluir neste Map. Sem alterar mais nada.
     */
    private void inicializarComportamentos() {
        comportamentos = Map.of(
            Localizacao.FAZENDA,  new ComportamentoFazenda(scanner),
            Localizacao.CIDADE,   new ComportamentoCidade(scanner),
            Localizacao.PRAIA,    new ComportamentoPraia(scanner),
            Localizacao.MINAS,    new ComportamentoMinas(scanner),
            Localizacao.FLORESTA, new ComportamentoFloresta(scanner)
        );
    }

    /**
     * Observer Pattern — conecta os objetos do jogo por eventos,
     * sem acoplamento direto entre eles.
     *
     * Antes (dormir() com 15 linhas):
     *   fazendeiro.descansar();
     *   fazendeiro.getFazenda().avancarDia(tempo.getEstacao());
     *   tempo.avancarDia();
     *   salvarJogo();
     *   exibirBoasManha();
     *
     * Depois: tempo.avancarDia() e os observers cuidam do resto.
     */
    private void registrarObservers() {

        // Observer 1: Fazendeiro restaura energia
        tempo.addObserver((EventoDiaObserver) t -> fazendeiro.descansar());

        // Observer 2: Fazenda processa crescimento de parcelas e animais
        tempo.addObserver(t -> fazendeiro.getFazenda().avancarDia(t.getEstacao()));

        // Observer 3: auto-save silencioso ao virar o dia
        tempo.addObserver(t -> {
            try {
                repositorio.salvar(fazendeiro, t);
            } catch (IOException e) {
                Renderer.exibirErro("Falha no auto-save: " + e.getMessage());
            }
        });

        // Observer 4: tela de bom dia (após estado já processado pelos observers anteriores)
        tempo.addObserver(t -> {
            Renderer.limparTela();
            long prontas = fazendeiro.getFazenda().parcelasProximasDeColher().count();
            Renderer.exibirBoasManha(t, prontas);
            Renderer.pausar(scanner);
        });

        // Observer 5: nível up de habilidade → exibe destaque na UI
        // NivelUpObserver é @FunctionalInterface: lambda é suficiente
        fazendeiro.getHabilidades().addObserver((tipo, nivel) ->
            Renderer.exibirNivelUp(tipo.getNome(), nivel)
        );
    }

    // ══════════════════════════════════════════════════════════════
    //  LOOP PRINCIPAL
    // ══════════════════════════════════════════════════════════════

    private void loopPrincipal() {
        while (rodando) {
            Renderer.limparTela();
            Renderer.exibirHUD(fazendeiro, tempo);
            Renderer.exibirMapaASCII(fazendeiro.getLocalizacao());

            // Strategy: delega exibição do menu ao comportamento atual
            ComportamentoLocal comportamentoAtual = comportamentos.get(fazendeiro.getLocalizacao());
            comportamentoAtual.exibirMenu();

            Renderer.exibirMenuGlobal();
            String input = scanner.nextLine().trim().toLowerCase();
            processarInput(input, comportamentoAtual);
        }
    }

    /**
     * Processa comandos globais (m, i, h, r, d, s, q).
     * Qualquer outra tecla é delegada ao ComportamentoLocal atual.
     * Exceções de regra de negócio são capturadas aqui — ponto único.
     */
    private void processarInput(String input, ComportamentoLocal comportamentoAtual) {
        try {
            switch (input) {
                case "m" -> menuMover();
                case "i" -> { Renderer.exibirInventario(fazendeiro); Renderer.pausar(scanner); }
                case "h" -> { Renderer.exibirHabilidades(fazendeiro); Renderer.pausar(scanner); }
                case "r" -> { Renderer.exibirRelatorioFazenda(fazendeiro, tempo); Renderer.pausar(scanner); }
                case "d" -> dormir();
                case "s" -> salvarJogo();
                case "q" -> confirmarSaida();
                // Strategy em ação: GameEngine não sabe o que "f" faz na Praia ou "l" nas Minas
                default  -> {
                    comportamentoAtual.processar(input, fazendeiro, tempo);
                    Renderer.pausar(scanner);
                }
            }
        } catch (FazendaException e) {
            Renderer.exibirErro(e.getMessage());
            Renderer.pausar(scanner);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  AÇÕES GLOBAIS (independentes de localização)
    // ══════════════════════════════════════════════════════════════

    private void menuMover() {
        System.out.println("\n  ══ Mapa ══════════════════════════════");
        System.out.println("  [1] Fazenda    [2] Cidade");
        System.out.println("  [3] Praia      [4] Minas    [5] Floresta");
        System.out.print("  > ");
        Localizacao destino = switch (scanner.nextLine().trim()) {
            case "1" -> Localizacao.FAZENDA;
            case "2" -> Localizacao.CIDADE;
            case "3" -> Localizacao.PRAIA;
            case "4" -> Localizacao.MINAS;
            case "5" -> Localizacao.FLORESTA;
            default  -> null;
        };
        if (destino == null) {
            System.out.println("  Destino inválido.");
        } else if (destino == fazendeiro.getLocalizacao()) {
            System.out.println("  Você já está aqui!");
        } else {
            fazendeiro.moverPara(destino);
            tempo.avancarHora(1);
            Renderer.exibirResultado("Você foi para " + destino + ".");
        }
        Renderer.pausar(scanner);
    }

    /**
     * Após refatoração com Observer, dormir() tem apenas 2 linhas úteis.
     * Antes: 15 linhas coordenando fazendeiro, fazenda, tempo, save e UI.
     * Agora: tempo.avancarDia() dispara os 4 observers em sequência.
     */
    private void dormir() {
        System.out.println("\n  Você foi dormir...");
        tempo.avancarDia();
        // Observers cuidam de: descansar, avancarDia(fazenda), salvar, exibirBoasManha
    }

    private void salvarJogo() {
        try {
            repositorio.salvar(fazendeiro, tempo);
            Renderer.exibirResultado("Jogo salvo com sucesso!");
        } catch (IOException e) {
            Renderer.exibirErro("Falha ao salvar: " + e.getMessage());
        }
        Renderer.pausar(scanner);
    }

    private void confirmarSaida() {
        System.out.print("  Deseja salvar antes de sair? [s/n]: ");
        if ("s".equalsIgnoreCase(scanner.nextLine().trim())) salvarJogo();
        System.out.println("  Até a próxima, " + fazendeiro.getNome() + "!");
        rodando = false;
    }
}
