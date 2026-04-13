package stardew.model;

import stardew.enums.Estacao;

/**
 * Representa uma semente plantável.
 * Herda de ItemBase → polimorfismo de classe.
 */
public class Semente extends ItemBase {

    private final int diasCrescimento;
    private final Estacao estacao;
    private final String culturaGerada;

    public Semente(String nome, double valor, int diasCrescimento,
                   Estacao estacao, String culturaGerada) {
        super(nome, valor);
        this.diasCrescimento = diasCrescimento;
        this.estacao = estacao;
        this.culturaGerada = culturaGerada;
    }

    /**
     * Verifica se esta semente pode ser plantada na estação atual.
     * Usado para lançar ColheitaForaDeEstacaoException se falso.
     */
    public boolean podeSerPlantada(Estacao estacaoAtual) {
        return this.estacao == estacaoAtual;
    }

    public int getDiasCrescimento() { return diasCrescimento; }
    public Estacao getEstacao()     { return estacao; }
    public String getCulturaGerada(){ return culturaGerada; }

    @Override
    public String getTipo() { return "Semente"; }

    @Override
    public String toString() {
        return String.format("[Semente] %-20s %s | %dd | G%.0f",
            getNome(), estacao, diasCrescimento, getValor());
    }
}
