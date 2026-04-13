package stardew.model;

import stardew.interfaces.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Inventário do fazendeiro.
 * Demonstra uso de Streams para filtragem e agregação de dados.
 */
public class Inventario {

    private final List<Item> itens;
    private final int capacidade;

    public Inventario(int capacidade) {
        this.capacidade = capacidade;
        this.itens = new ArrayList<>();
    }

    // ── Operações básicas ────────────────────────────────────────

    public boolean adicionarItem(Item item) {
        if (estaCheia()) return false;
        itens.add(item);
        return true;
    }

    public boolean removerItem(Item item) {
        return itens.remove(item);
    }

    // ── Stream API ───────────────────────────────────────────────

    /** Filtra itens pelo tipo (ex: "Semente", "Colheita"). */
    public Stream<Item> filtrarPorTipo(String tipo) {
        return itens.stream()
                .filter(i -> i.getTipo().equalsIgnoreCase(tipo));
    }

    /** Retorna o valor total de todos os itens via Stream reduce. */
    public double valorTotal() {
        return itens.stream()
                .mapToDouble(Item::getValor)
                .sum();
    }

    /** Agrupa itens por tipo usando Collectors.groupingBy. */
    public Map<String, Long> contagemPorTipo() {
        return itens.stream()
                .collect(Collectors.groupingBy(Item::getTipo, Collectors.counting()));
    }

    /** Busca item por nome ignorando caixa. */
    public Optional<Item> buscarPorNome(String nome) {
        return itens.stream()
                .filter(i -> i.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }

    /** Retorna lista coletada de um tipo específico. */
    public List<Item> listarPorTipo(String tipo) {
        return filtrarPorTipo(tipo).collect(Collectors.toList());
    }

    /** Retorna o item mais valioso via Stream max. */
    public Optional<Item> itemMaisValioso() {
        return itens.stream()
                .max((a, b) -> Double.compare(a.getValor(), b.getValor()));
    }

    // ── Getters ──────────────────────────────────────────────────

    public List<Item> getItens()  { return List.copyOf(itens); }
    public int getCapacidade()    { return capacidade; }
    public int getOcupado()       { return itens.size(); }
    public boolean estaCheia()    { return itens.size() >= capacidade; }
    public boolean estaVazia()    { return itens.isEmpty(); }

    @Override
    public String toString() {
        if (estaVazia()) return "  [inventário vazio]";
        return itens.stream()
                .map(i -> "  • " + i)
                .collect(Collectors.joining("\n"));
    }
}
