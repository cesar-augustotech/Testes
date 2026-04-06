package stardew.model;

import stardew.interfaces.Item;

/**
 * Classe abstrata base para todos os itens.
 * Implementa a interface Item e fornece comportamento comum.
 *
 * Conceitos aplicados: Classe Abstrata, Encapsulamento, Polimorfismo.
 */
public abstract class ItemBase implements Item {

    private final String nome;
    private double valor;

    protected ItemBase(String nome, double valor) {
        this.nome = nome;
        this.valor = valor;
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public double getValor() {
        return valor;
    }

    protected void setValor(double valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return String.format("[%-10s] %-22s G%.0f", getTipo(), nome, valor);
    }
}
