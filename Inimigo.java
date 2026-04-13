package stardew.model;

import stardew.enums.Localizacao;
import stardew.interfaces.Item;

import java.util.List;
import java.util.Random;

/**
 * Representa um inimigo encontrado nas Minas.
 * Possui sistema de combate por turnos e drop de itens.
 */
public class Inimigo {

    private final String nome;
    private int vida;
    private final int vidaMax;
    private final int dano;
    private final int xpRecompensa;

    private static final Random RAND = new Random();

    public Inimigo(String nome, int vida, int dano, int xpRecompensa) {
        this.nome = nome;
        this.vida = vida;
        this.vidaMax = vida;
        this.dano = dano;
        this.xpRecompensa = xpRecompensa;
    }

    /** Inimigo ataca: retorna o dano causado (com variação aleatória). */
    public int atacar() {
        return dano + RAND.nextInt(Math.max(1, dano / 3));
    }

    public void receberDano(int dano) {
        vida = Math.max(0, vida - dano);
    }

    public boolean estaMorto() { return vida <= 0; }

    /** Ao morrer, dropa um item aleatório das Minas. */
    public Item drop() {
        String[] drops  = { "Carvão", "Minério de Cobre", "Minério de Ferro", "Cristal", "Gema Rara" };
        double[] valores = { 15,        50,                   100,                200,       500 };
        int idx = RAND.nextInt(drops.length);
        return new RecursoSilvestre(drops[idx], valores[idx], "Mineral",
                List.of(Localizacao.MINAS));
    }

    public String getNome()       { return nome; }
    public int getVida()          { return vida; }
    public int getVidaMax()       { return vidaMax; }
    public int getDano()          { return dano; }
    public int getXpRecompensa()  { return xpRecompensa; }

    @Override
    public String toString() {
        return String.format("%s  HP: %d/%d  Dano: %d  XP: %d",
            nome, vida, vidaMax, dano, xpRecompensa);
    }
}
