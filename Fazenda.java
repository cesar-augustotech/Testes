package stardew.model;

import stardew.enums.Estacao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Representa a fazenda do jogador.
 * Agrega Parcelas e Animais — demonstra relacionamento de composição.
 * Usa Streams para relatórios e filtragens.
 */
public class Fazenda {

    private final String nome;
    private final List<Parcela> parcelas;
    private final List<Animal> animais;

    private static final int NUM_PARCELAS = 12;

    /** Construtor para nova fazenda. */
    public Fazenda(String nome) {
        this.nome = nome;
        this.parcelas = new ArrayList<>();
        this.animais  = new ArrayList<>();
        for (int i = 1; i <= NUM_PARCELAS; i++) {
            parcelas.add(new Parcela(i));
        }
    }

    /** Construtor para carregamento do save. */
    public Fazenda(String nome, List<Parcela> parcelas, List<Animal> animais) {
        this.nome = nome;
        this.parcelas = new ArrayList<>(parcelas);
        this.animais  = new ArrayList<>(animais);
    }

    // ── Lógica de jogo ───────────────────────────────────────────

    /** Avança um dia: processa parcelas e animais. */
    public void avancarDia(Estacao estacao) {
        parcelas.forEach(Parcela::avancarDia);
        animais.forEach(Animal::avancarDia);
    }

    public void adicionarAnimal(Animal animal) {
        animais.add(animal);
    }

    // ── Streams ──────────────────────────────────────────────────

    public Stream<Parcela> parcelasLivres() {
        return parcelas.stream().filter(Parcela::estaLivre);
    }

    public Stream<Parcela> parcelasProximasDeColher() {
        return parcelas.stream().filter(Parcela::estaProxima);
    }

    public Stream<Parcela> parcelasPlantadas() {
        return parcelas.stream().filter(p -> !p.estaLivre());
    }

    public Stream<Animal> animaisNaoAlimentados() {
        return animais.stream().filter(a -> !a.isAlimentadoHoje());
    }

    /** Gera um relatório textual via Streams. */
    public String relatorio(Estacao estacaoAtual) {
        long livre  = parcelasLivres().count();
        long pronta = parcelasProximasDeColher().count();
        long semComida = animaisNaoAlimentados().count();

        double producaoEstimada = parcelasPlantadas()
                .filter(p -> p.getSemente() != null)
                .mapToDouble(p -> p.getSemente().getValor() * 3.0)
                .sum();

        List<String> culturas = parcelasPlantadas()
                .filter(p -> p.getSemente() != null)
                .map(p -> p.getSemente().getCulturaGerada())
                .distinct()
                .collect(Collectors.toList());

        return String.format(
            "  Fazenda: %-18s | %s%n" +
            "  Parcelas: %d livres, %d prontas para colher%n" +
            "  Animais: %d total, %d sem alimentação hoje%n" +
            "  Culturas ativas: %s%n" +
            "  Produção estimada: G%.0f",
            nome, estacaoAtual,
            livre, pronta,
            animais.size(), semComida,
            culturas.isEmpty() ? "nenhuma" : String.join(", ", culturas),
            producaoEstimada
        );
    }

    // ── Getters ──────────────────────────────────────────────────

    public String getNome()              { return nome; }
    public List<Parcela> getParcelas()   { return List.copyOf(parcelas); }
    public List<Animal>  getAnimais()    { return List.copyOf(animais); }

    /** Busca uma parcela pelo ID. */
    public Parcela getParcela(int id) {
        return parcelas.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /** Expõe lista mutável apenas para uso interno (persistência/lógica). */
    public List<Parcela> getParcelasInterno() { return parcelas; }
    public List<Animal>  getAnimaisInterno()  { return animais; }
}
