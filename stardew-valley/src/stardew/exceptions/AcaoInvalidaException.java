package stardew.exceptions;

import stardew.enums.Localizacao;

/**
 * Lançada quando o jogador tenta executar uma ação
 * que não está disponível na localização atual.
 */
public class AcaoInvalidaException extends FazendaException {

    public AcaoInvalidaException(String acao, Localizacao localizacaoAtual) {
        super(String.format(
            "Não é possível '%s' em %s! Ações disponíveis: %s.",
            acao, localizacaoAtual, localizacaoAtual.getAcoesDisponiveis()
        ));
    }
}
