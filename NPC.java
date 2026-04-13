package stardew.model;

import stardew.enums.Estacao;
import stardew.enums.Localizacao;
import stardew.interfaces.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Personagem não-jogável.
 * Herda de Personagem — demonstra herança e polimorfismo de método (agir()).
 *
 * NPCs vendem sementes e animais na Cidade.
 */
public class NPC extends Personagem {

    private final String profissao;
    private final Localizacao localFixo;
    private int amizade; // 0–100
    private final List<Item> estoque;

    private static final List<String[]> DIALOGOS = List.of(
        new String[]{"Que bela manhã para plantar, não é?", "O outono chegou cedo este ano."},
        new String[]{"Vi um peixe enorme na praia ontem!", "Cuidado com os monstros nas minas."},
        new String[]{"Precisa de alguma coisa? Estou aqui!", "O inverno está chegando — faça suas reservas."}
    );

    public NPC(String nome, String profissao, Localizacao localFixo) {
        super(nome, 100, localFixo);
        this.profissao = profissao;
        this.localFixo = localFixo;
        this.amizade   = 0;
        this.estoque   = new ArrayList<>();
        popularEstoque();
    }

    private void popularEstoque() {
        // Sementes de todas as estações
        estoque.add(new Semente("Semente de Morango",   35,  8, Estacao.PRIMAVERA, "Morango"));
        estoque.add(new Semente("Semente de Batata",     20,  6, Estacao.PRIMAVERA, "Batata"));
        estoque.add(new Semente("Semente de Couve",      30,  7, Estacao.PRIMAVERA, "Couve-Flor"));
        estoque.add(new Semente("Semente de Mirtilo",    80,  13, Estacao.VERAO,    "Mirtilo"));
        estoque.add(new Semente("Semente de Melão",      40,  12, Estacao.VERAO,    "Melão"));
        estoque.add(new Semente("Semente de Tomate",     25,  11, Estacao.VERAO,    "Tomate"));
        estoque.add(new Semente("Semente de Abóbora",    50,  13, Estacao.OUTONO,   "Abóbora"));
        estoque.add(new Semente("Semente de Cranberry",  120, 7,  Estacao.OUTONO,   "Cranberry"));
        estoque.add(new Semente("Semente de Inhame",     30,  10, Estacao.OUTONO,   "Inhame"));
        // Animais
        estoque.add(new Animal("Galinha Branca",  "galinha", 50));
        estoque.add(new Animal("Vaca Preta",      "vaca",    50));
        estoque.add(new Animal("Cabra Marrom",    "cabra",   50));
    }

    /**
     * Polimorfismo de método: implementação de agir() para NPC.
     */
    @Override
    public String agir() {
        return dialogar();
    }

    /** Retorna um diálogo baseado no nível de amizade. */
    public String dialogar() {
        amizade = Math.min(100, amizade + 5);
        int idx  = Math.min(amizade / 40, DIALOGOS.size() - 1);
        int sub  = (int)(Math.random() * DIALOGOS.get(idx).length);
        return String.format("[%s - %s]: \"%s\"", getNome(), profissao, DIALOGOS.get(idx)[sub]);
    }

    /** Retorna o item do estoque sem removê-lo. */
    public Item verItem(String nome) {
        return estoque.stream()
                .filter(i -> i.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }

    public List<Item> getEstoque()  { return List.copyOf(estoque); }
    public String getProfissao()    { return profissao; }
    public int getAmizade()         { return amizade; }

    @Override
    public String toString() {
        return String.format("[NPC] %-10s — %s (Amizade: %d)", getNome(), profissao, amizade);
    }
}
