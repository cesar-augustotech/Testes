package stardew.model;

import stardew.interfaces.Item;

/**
 * Representa um animal criado na fazenda.
 * Produz itens (ovos, leite, etc.) com qualidade proporcional à felicidade.
 *
 * Implementa Item para poder figurar no estoque do NPC e ser comprado pelo Fazendeiro.
 * Ao ser comprado, é movido do Inventário para a lista de animais da Fazenda.
 */
public class Animal implements Item {

    private final String nome;
    private final String tipo;
    private int felicidade; // 0–100
    private boolean alimentadoHoje;

    // Preços de compra por tipo
    private static final double PRECO_GALINHA = 800;
    private static final double PRECO_VACA    = 1500;
    private static final double PRECO_CABRA   = 2000;
    private static final double PRECO_PORCO   = 4000;

    public Animal(String nome, String tipo) {
        this.nome = nome;
        this.tipo = tipo;
        this.felicidade = 50;
        this.alimentadoHoje = false;
    }

    /** Construtor para carregamento do save. */
    public Animal(String nome, String tipo, int felicidade) {
        this.nome = nome;
        this.tipo = tipo;
        this.felicidade = felicidade;
        this.alimentadoHoje = false;
    }

    // ── Implementação de Item ────────────────────────────────────

    @Override
    public String getNome() { return nome + " (" + tipo + ")"; }

    /** Retorna o preço de compra do animal conforme seu tipo. */
    @Override
    public double getValor() {
        return switch (tipo.toLowerCase()) {
            case "galinha" -> PRECO_GALINHA;
            case "vaca"    -> PRECO_VACA;
            case "cabra"   -> PRECO_CABRA;
            case "porco"   -> PRECO_PORCO;
            default        -> 500;
        };
    }

    @Override
    public String getTipo() { return "Animal"; }

    // ── Lógica do animal ─────────────────────────────────────────

    /**
     * Produz um item baseado no tipo do animal.
     * Qualidade e valor escalam com a felicidade.
     * Retorna null se felicidade < 30.
     */
    public Item produzir() {
        if (felicidade < 30) return null;
        int qualidade = calcularQualidade();
        double fator = felicidade / 100.0;
        return switch (tipo.toLowerCase()) {
            case "galinha" -> new Colheita("Ovo",              (int)(50  * fator), qualidade, null);
            case "vaca"    -> new Colheita("Leite",            (int)(125 * fator), qualidade, null);
            case "cabra"   -> new Colheita("Queijo de Cabra",  (int)(200 * fator), qualidade, null);
            case "porco"   -> new Colheita("Trufa de Fazenda", (int)(625 * fator), qualidade, null);
            default        -> null;
        };
    }

    private int calcularQualidade() {
        if (felicidade >= 80) return 3;
        if (felicidade >= 50) return 2;
        return 1;
    }

    public void alimentar() {
        alimentadoHoje = true;
        felicidade = Math.min(100, felicidade + 10);
    }

    /** Ao final do dia: penaliza felicidade se não foi alimentado. */
    public void avancarDia() {
        if (!alimentadoHoje) {
            felicidade = Math.max(0, felicidade - 10);
        }
        alimentadoHoje = false;
    }

    // Getters específicos de Animal (além dos de Item)
    public String getNomeAnimal()     { return nome; }   // nome sem o sufixo do tipo
    public String getTipoAnimal()     { return tipo; }   // ex: "galinha", "vaca"
    public int getFelicidade()        { return felicidade; }
    public boolean isAlimentadoHoje() { return alimentadoHoje; }

    @Override
    public String toString() {
        String humor = felicidade >= 70 ? ":)" : felicidade >= 40 ? ":|" : ":(";
        return String.format("%-10s (%-7s) Felicidade: %3d%%  %s  G%.0f",
            nome, tipo, felicidade, humor, getValor());
    }
}
