package stardew.exceptions;

import stardew.enums.Estacao;

/**
 * Lançada ao tentar plantar uma cultura fora da estação correta.
 */
public class ColheitaForaDeEstacaoException extends FazendaException {

    public ColheitaForaDeEstacaoException(String cultura, Estacao estacaoAtual) {
        super(String.format(
            "'%s' não pode ser plantada no %s! Culturas desta estação: %s.",
            cultura, estacaoAtual, estacaoAtual.getCulturas()
        ));
    }
}
