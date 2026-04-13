package stardew.interfaces;

import stardew.enums.Localizacao;

import java.util.List;

/**
 * Interface para itens que podem ser coletados do mundo.
 * Implementada por RecursoSilvestre.
 * Demonstra polimorfismo de interface.
 */
public interface Coletavel {

    /** Realiza a coleta e retorna o item obtido. */
    Item coletar();

    /** Retorna a lista de locais onde este recurso pode ser coletado. */
    List<Localizacao> getLocaisValidos();

    /** Verifica se pode ser coletado em uma localização específica. */
    default boolean podeColetarEm(Localizacao localizacao) {
        return getLocaisValidos().contains(localizacao);
    }
}
