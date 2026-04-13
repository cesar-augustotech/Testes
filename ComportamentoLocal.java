package stardew.engine.comportamento;

import stardew.exceptions.FazendaException;
import stardew.model.Fazendeiro;
import stardew.model.GerenciadorTempo;

/**
 * Strategy Pattern — define o contrato de comportamento de cada localização.
 *
 * Cada local do mapa implementa esta interface com seu próprio menu
 * e sua própria lógica de processamento de input. GameEngine não
 * precisa conhecer os locais individualmente — apenas chama
 * estrategiaAtual.exibirMenu() e estrategiaAtual.processar().
 *
 * Aplicação do Open/Closed Principle:
 *   - Adicionar TAVERNA = criar ComportamentoTaverna + registrar no mapa.
 *   - GameEngine.java não é tocado.
 */
public interface ComportamentoLocal {

    /** Exibe o menu de ações disponíveis neste local. */
    void exibirMenu();

    /**
     * Processa o input do jogador dentro deste local.
     *
     * @param input     tecla digitada pelo jogador (lowercase)
     * @param fazendeiro referência ao jogador
     * @param tempo     referência ao gerenciador de tempo
     * @throws FazendaException para erros de regra de negócio
     */
    void processar(String input, Fazendeiro fazendeiro, GerenciadorTempo tempo)
            throws FazendaException;
}
