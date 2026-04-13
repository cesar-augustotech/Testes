package stardew.model;

/**
 * Representa uma ferramenta utilizável pelo fazendeiro.
 * Possui sistema de durabilidade.
 */
public class Ferramenta extends ItemBase {

    private int durabilidade;
    private final int durabilidadeMax;
    private final String tipoFerramenta;

    /** Construtor para novo item (durabilidade inicial = máximo). */
    public Ferramenta(String nome, double valor, int durabilidade, String tipoFerramenta) {
        super(nome, valor);
        this.durabilidade = durabilidade;
        this.durabilidadeMax = durabilidade;
        this.tipoFerramenta = tipoFerramenta;
    }

    /** Construtor para carregamento do save (durabilidade atual pode diferir do máximo). */
    public Ferramenta(String nome, double valor, int durabilidade, int durabilidadeMax, String tipoFerramenta) {
        super(nome, valor);
        this.durabilidade = durabilidade;
        this.durabilidadeMax = durabilidadeMax;
        this.tipoFerramenta = tipoFerramenta;
    }

    /** Usa a ferramenta, consumindo 1 ponto de durabilidade. */
    public void usar() {
        if (durabilidade > 0) durabilidade--;
    }

    /** Restaura a durabilidade ao máximo. */
    public void reparar() {
        durabilidade = durabilidadeMax;
    }

    public boolean estaDanificada() { return durabilidade == 0; }
    public int getDurabilidade()    { return durabilidade; }
    public int getDurabilidadeMax() { return durabilidadeMax; }
    public String getTipoFerramenta(){ return tipoFerramenta; }

    @Override
    public String getTipo() { return "Ferramenta"; }

    @Override
    public String toString() {
        return String.format("[Ferramenta] %-16s (%d/%d) G%.0f",
            getNome(), durabilidade, durabilidadeMax, getValor());
    }
}
