package stardew.interfaces;

/**
 * Interface que define o contrato de qualquer item do jogo.
 * Implementada por ItemBase e suas subclasses.
 */
public interface Item {

    /** Retorna o nome de exibição do item. */
    String getNome();

    /** Retorna o valor de mercado do item em ouro (G). */
    double getValor();

    /** Retorna a categoria do item (ex: "Semente", "Colheita", etc.). */
    String getTipo();
}
