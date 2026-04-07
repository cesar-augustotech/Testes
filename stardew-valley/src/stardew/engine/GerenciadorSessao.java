package stardew.engine;

import stardew.model.Fazendeiro;
import stardew.model.GerenciadorTempo;

import java.io.IOException;
import java.util.Scanner;

/**
 * Gerencia o ciclo de vida de uma sessão: novo jogo, carregamento e salvamento.
 * Depois de chamar iniciar(), use getFazendeiro() e getTempo() para obter o estado.
 */
class GerenciadorSessao {

    private final PersistenciaCSV persistencia;
    private final Scanner scanner;

    private Fazendeiro fazendeiro;
    private GerenciadorTempo tempo;

    GerenciadorSessao(PersistenciaCSV persistencia, Scanner scanner) {
        this.persistencia = persistencia;
        this.scanner      = scanner;
    }

    // Pergunta ao jogador se quer continuar ou começar do zero
    void iniciar() {
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
    }

    private void carregarJogo() {
        try {
            tempo      = persistencia.carregarTempo();
            var inv    = persistencia.carregarInventario();
            var fazenda = persistencia.carregarFazenda();
            fazendeiro = persistencia.carregarFazendeiro(inv, fazenda);
            System.out.println("\n  Jogo carregado! " + tempo.getDataFormatada());
        } catch (IOException e) {
            System.out.println("  Erro ao carregar save: " + e.getMessage());
            System.out.println("  Iniciando novo jogo...");
            novoJogo();
        }
    }

    void salvar(Fazendeiro fazendeiro, GerenciadorTempo tempo) {
        try {
            persistencia.salvarTudo(fazendeiro, tempo);
            System.out.println("  >> Jogo salvo com sucesso!");
        } catch (IOException e) {
            System.out.println("  ERRO ao salvar: " + e.getMessage());
        }
    }

    Fazendeiro getFazendeiro() {
        return fazendeiro;
    }

    GerenciadorTempo getTempo() {
        return tempo;
    }
}
