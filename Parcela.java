package stardew.model;

/**
 * Representa uma parcela de terra na fazenda.
 * Pode conter uma semente em crescimento.
 */
public class Parcela {

    private final int id;
    private boolean irrigada;
    private Semente semente;
    private int diasPlantado;

    /** Construtor para nova parcela vazia. */
    public Parcela(int id) {
        this.id = id;
        this.irrigada = false;
        this.semente = null;
        this.diasPlantado = 0;
    }

    /** Construtor para carregamento do save. */
    public Parcela(int id, boolean irrigada, Semente semente, int diasPlantado) {
        this.id = id;
        this.irrigada = irrigada;
        this.semente = semente;
        this.diasPlantado = diasPlantado;
    }

    public void irrigar() {
        this.irrigada = true;
    }

    /** Planta uma semente. Retorna false se a parcela já estiver ocupada. */
    public boolean plantarSemente(Semente semente) {
        if (this.semente != null) return false;
        this.semente = semente;
        this.diasPlantado = 0;
        return true;
    }

    /** Verifica se a cultura está pronta para ser colhida. */
    public boolean estaProxima() {
        return semente != null && diasPlantado >= semente.getDiasCrescimento();
    }

    /**
     * Colhe a parcela.
     * A qualidade depende de irrigação + nível de colheita do fazendeiro.
     */
    public Colheita colher(stardew.enums.Estacao estacaoAtual, int nivelColheita) {
        if (!estaProxima()) return null;
        int qualidade = calcularQualidade(nivelColheita);
        double valorBase = semente.getValor() * 3.0;
        Colheita colheita = new Colheita(semente.getCulturaGerada(), valorBase, qualidade, estacaoAtual);
        // Limpa a parcela após colher
        this.semente = null;
        this.diasPlantado = 0;
        this.irrigada = false;
        return colheita;
    }

    private int calcularQualidade(int nivelColheita) {
        if (irrigada && nivelColheita >= 8) return 3; // Ouro
        if (irrigada && nivelColheita >= 4) return 2; // Prata
        return 1;                                      // Normal
    }

    /** Avança um dia: cresce se foi irrigada, depois remove a irrigação. */
    public void avancarDia() {
        if (semente != null && irrigada) {
            diasPlantado++;
        }
        irrigada = false; // irrigação não persiste entre dias
    }

    // Getters
    public int getId()             { return id; }
    public boolean isIrrigada()    { return irrigada; }
    public Semente getSemente()    { return semente; }
    public int getDiasPlantado()   { return diasPlantado; }
    public boolean estaLivre()     { return semente == null; }

    @Override
    public String toString() {
        if (semente == null) {
            return String.format("Parcela #%2d  [vazia]", id);
        }
        String status = estaProxima() ? " >> PRONTA!" : "";
        String agua  = irrigada ? " [irrigada]" : "";
        return String.format("Parcela #%2d  %-12s  %2d/%2d dias%s%s",
            id, semente.getCulturaGerada(), diasPlantado,
            semente.getDiasCrescimento(), agua, status);
    }
}
