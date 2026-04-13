package stardew.model;

import stardew.enums.Localizacao;
import stardew.interfaces.Coletavel;
import stardew.interfaces.Item;

import java.util.List;

/**
 * Recurso coletável do mundo (madeira, cogumelos, minérios, peixes, etc).
 * Demonstra múltipla implementação: herda ItemBase E implementa Coletavel.
 */
public class RecursoSilvestre extends ItemBase implements Coletavel {

    private final String categoria;
    private final List<Localizacao> locaisValidos;

    public RecursoSilvestre(String nome, double valor,
                            String categoria, List<Localizacao> locaisValidos) {
        super(nome, valor);
        this.categoria = categoria;
        this.locaisValidos = List.copyOf(locaisValidos);
    }

    /**
     * Polimorfismo de método: implementa Coletavel.coletar().
     * Retorna uma nova instância do mesmo recurso.
     */
    @Override
    public Item coletar() {
        return new RecursoSilvestre(getNome(), getValor(), categoria, locaisValidos);
    }

    @Override
    public List<Localizacao> getLocaisValidos() {
        return locaisValidos;
    }

    public String getCategoria() { return categoria; }

    @Override
    public String getTipo() { return "Recurso"; }

    @Override
    public String toString() {
        return String.format("[Recurso] %-20s [%s] G%.0f",
            getNome(), categoria, getValor());
    }
}
