package stardew.engine;

import stardew.enums.Localizacao;
import stardew.exceptions.FazendaException;
import stardew.model.Fazendeiro;
import stardew.model.GerenciadorTempo;
import stardew.model.NPC;

import java.util.Scanner;

/**
 * Ponto central do jogo: inicializa os subsistemas e mantém o loop rodando.
 * Toda lógica específica foi delegada a classes menores com responsabilidades claras:
 *   - GerenciadorSessao: novo jogo, carregar e salvar
 *   - Renderizador: toda saída visual no terminal
 *   - ControladorAcoes: menus de ação dentro de cada local
 */
public class GameEngine {

    private Fazendeiro fazendeiro;
    private GerenciadorTempo tempo;

    private final Scanner scanner;
    private final GerenciadorSessao sessao;
    private final Renderizador tela;
    private final ControladorAcoes acoes;

    private boolean rodando;

    public GameEngine() {
        this.scanner = new Scanner(System.in);
        PersistenciaCSV persistencia = new PersistenciaCSV();
        NPC pierre = new NPC("Pierre", "Lojista",  Localizacao.CIDADE);
        NPC willy  = new NPC("Willy",  "Pescador", Localizacao.PRAIA);
        this.tela   = new Renderizador(scanner);
        this.sessao = new GerenciadorSessao(persistencia, scanner);
        this.acoes  = new ControladorAcoes(scanner, tela, pierre, willy);
    }

    // ── Inicialização ─────────────────────────────────────────────

    public void iniciar() {
        tela.limparTela();
        tela.exibirSplash();
        sessao.iniciar();
        tela.pausar();
        fazendeiro = sessao.getFazendeiro();
        tempo      = sessao.getTempo();
        acoes.setEstado(fazendeiro, tempo);
        rodando = true;
        loopPrincipal();
    }

    // ── Loop principal ────────────────────────────────────────────

    private void loopPrincipal() {
        while (rodando) {
            tela.limparTela();
            tela.exibirHUD(fazendeiro, tempo);
            tela.exibirMenuLocal(fazendeiro);
            String input = scanner.nextLine().trim().toLowerCase();
            processarInput(input);
        }
    }

    private void processarInput(String input) {
        try {
            switch (input) {
                case "m" -> acoes.menuMover();
                case "i" -> tela.exibirInventario(fazendeiro);
                case "h" -> tela.exibirHabilidades(fazendeiro);
                case "r" -> tela.exibirRelatorioFazenda(fazendeiro, tempo);
                case "d" -> dormir();
                case "s" -> sessao.salvar(fazendeiro, tempo);
                case "q" -> confirmarSaida();
                default  -> acoes.processarAcaoLocal(input);
            }
        } catch (FazendaException e) {
            tela.exibirErro(e.getMessage());
        }
        if (!input.equals("d") && !input.equals("q")) tela.pausar();
    }

    // ── Fim de dia ────────────────────────────────────────────────

    private void dormir() {
        System.out.println("\n  Você foi dormir...");
        fazendeiro.descansar();
        fazendeiro.getFazenda().avancarDia(tempo.getEstacao());
        tempo.avancarDia();
        sessao.salvar(fazendeiro, tempo);
        tela.limparTela();
        tela.exibirManha(tempo, fazendeiro);
        tela.pausar();
    }

    private void confirmarSaida() {
        System.out.print("  Deseja salvar antes de sair? [s/n]: ");
        if ("s".equalsIgnoreCase(scanner.nextLine().trim())) {
            sessao.salvar(fazendeiro, tempo);
        }
        System.out.println("  Até a próxima, " + fazendeiro.getNome() + "!");
        rodando = false;
    }
}
