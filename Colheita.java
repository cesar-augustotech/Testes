package stardew.model;

import stardew.enums.Estacao;

/**
 * Representa um item colhido de uma parcela ou animal.
 * A qualidade influencia o preço de venda.
 */
public class Colheita extends ItemBase {

    private final int qualidade; // 1=Normal, 2=Prata, 3=Ouro
    private final Estacao estacao; // pode ser null para produtos animais

    public Colheita(String nome, double valorBase, int qualidade, Estacao estacao) {
        super(nome, aplicarMultiplicadorQualidade(valorBase, qualidade));
        this.qualidade = qualidade;
        this.estacao = estacao;
    }

    /** Polimorfismo de método: sobrescreve comportamento de cálculo de preço. */
    private static double aplicarMultiplicadorQualidade(double valorBase, int qualidade) {
        return switch (qualidade) {
            case 2  -> valorBase * 1.25; // Prata: +25%
            case 3  -> valorBase * 1.50; // Ouro:  +50%
            default -> valorBase;        // Normal: sem bônus
        };
    }

    public int getQualidade()  { return qualidade; }
    public Estacao getEstacao(){ return estacao; }

    public String getNomeQualidade() {
        return switch (qualidade) {
            case 2  -> "★★  Prata";
            case 3  -> "★★★ Ouro";
            default -> "★   Normal";
        };
    }

    @Override
    public String getTipo() { return "Colheita"; }

    @Override
    public String toString() {
        return String.format("[Colheita] %-18s (%s) G%.0f",
            getNome(), getNomeQualidade(), getValor());
    }
}
