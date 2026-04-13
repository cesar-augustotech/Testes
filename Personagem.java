package stardew.model;

import stardew.enums.Localizacao;

/**
 * Classe abstrata base para todos os personagens do jogo.
 *
 * Conceitos aplicados:
 *   - Classe Abstrata (não pode ser instanciada diretamente)
 *   - Encapsulamento (atributos privados + getters/setters)
 *   - Polimorfismo (agir() é abstrato → cada subclasse define o comportamento)
 *   - Herança (Fazendeiro e NPC herdam daqui)
 */
public abstract class Personagem {

    private String nome;
    private int energia;
    private final int energiaMax;
    private Localizacao localizacao;

    protected Personagem(String nome, int energiaMax, Localizacao localizacaoInicial) {
        this.nome          = nome;
        this.energiaMax    = energiaMax;
        this.energia       = energiaMax;
        this.localizacao   = localizacaoInicial;
    }

    // ── Métodos abstratos (polimorfismo de método) ────────────────

    /**
     * Ação principal do personagem.
     * Cada subclasse implementa de forma diferente (polimorfismo).
     */
    public abstract String agir();

    // ── Métodos concretos compartilhados ──────────────────────────

    /**
     * Move o personagem para uma nova localização.
     * Custa 2 pontos de energia.
     */
    public void moverPara(Localizacao destino) {
        this.localizacao = destino;
        consumirEnergia(2);
    }

    /** Descansa: restaura toda a energia. */
    public void descansar() {
        this.energia = energiaMax;
    }

    public void consumirEnergia(int quantidade) {
        this.energia = Math.max(0, energia - quantidade);
    }

    public void restaurarEnergia(int quantidade) {
        this.energia = Math.min(energiaMax, energia + quantidade);
    }

    public boolean temEnergia(int quantidade) {
        return energia >= quantidade;
    }

    // ── Getters e Setters ─────────────────────────────────────────

    public String getNome()              { return nome; }
    public void setNome(String nome)     { this.nome = nome; }
    public int getEnergia()              { return energia; }
    public int getEnergiaMax()           { return energiaMax; }
    public Localizacao getLocalizacao()  { return localizacao; }
    public void setLocalizacao(Localizacao l) { this.localizacao = l; }

    @Override
    public String toString() {
        return String.format("%s | Energia: %d/%d | Local: %s",
            nome, energia, energiaMax, localizacao);
    }
}
