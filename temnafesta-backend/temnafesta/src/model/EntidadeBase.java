package model;

import java.time.LocalDateTime;

/**
 * CLASSE ABSTRATA — EntidadeBase
 *
 * POO aplicado:
 * - Classe Abstrata: não pode ser instanciada diretamente
 * - Herança: todos os Models estendem esta classe
 * - Encapsulamento: atributos privados com getters/setters
 * - Polimorfismo de classe: cada subclasse implementa toString() de forma diferente
 * - Método abstrato: força todas as subclasses a implementarem descricaoResumida()
 */
public abstract class EntidadeBase {

    // Encapsulamento: atributo privado, acesso somente via getter/setter
    private int id;
    private LocalDateTime dataCriacao;

    // Construtor padrão — inicializa dataCriacao automaticamente
    public EntidadeBase() {
        this.dataCriacao = LocalDateTime.now();
    }

    // Construtor com id — usado na reconstrução a partir do CSV
    public EntidadeBase(int id) {
        this.id = id;
        this.dataCriacao = LocalDateTime.now();
    }

    // Construtor completo — usado na reconstrução a partir do CSV com data original
    public EntidadeBase(int id, LocalDateTime dataCriacao) {
        this.id = id;
        this.dataCriacao = dataCriacao;
    }

    // --- Getters e Setters (Encapsulamento) ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    /**
     * MÉTODO ABSTRATO — Polimorfismo de método
     * Cada subclasse DEVE implementar sua própria versão.
     * Retorna uma linha formatada para exibição resumida da entidade.
     */
    public abstract String descricaoResumida();

    /**
     * MÉTODO ABSTRATO — Polimorfismo de método
     * Cada subclasse DEVE implementar sua própria versão.
     * Retorna a linha formatada para persistência em CSV.
     */
    public abstract String toCsvLine();

    /**
     * toString() sobrescrito — Polimorfismo de método
     * Padrão de exibição compartilhado por todas as entidades.
     */
    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + " | id=" + id +
               " | criado em=" + dataCriacao + "]\n" + descricaoResumida();
    }
}
