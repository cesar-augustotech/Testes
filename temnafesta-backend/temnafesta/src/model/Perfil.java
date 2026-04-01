package model;

import enums.PerfilUsuario;

/**
 * MODEL — Perfil
 *
 * POO aplicado:
 * - Herança: estende EntidadeBase
 * - Encapsulamento: atributos privados
 * - Relacionamento: composto pelo Usuario (associação)
 * - Polimorfismo de método: implementa descricaoResumida() e toCsvLine()
 *
 * Reflete a tabela 'perfil' do banco de dados.
 */
public class Perfil extends EntidadeBase {

    private PerfilUsuario tipo; // Enum — substitui o VARCHAR(50) do banco

    // Construtor padrão
    public Perfil() {
        super();
    }

    // Construtor com id — reconstrução a partir de CSV
    public Perfil(int id, PerfilUsuario tipo) {
        super(id);
        this.tipo = tipo;
    }

    // Construtor completo
    public Perfil(PerfilUsuario tipo) {
        super();
        this.tipo = tipo;
    }

    // --- Getters e Setters ---

    public PerfilUsuario getTipo() { return tipo; }
    public void setTipo(PerfilUsuario tipo) { this.tipo = tipo; }

    public String getNome() { return tipo.getDescricao(); }

    public boolean isPodeGerenciarUsuarios() {
        return tipo.isPodeGerenciarUsuarios();
    }

    // --- Implementação dos métodos abstratos de EntidadeBase ---

    @Override
    public String descricaoResumida() {
        return "Perfil: " + tipo.getDescricao() +
               " | Pode gerenciar usuários: " + (isPodeGerenciarUsuarios() ? "Sim" : "Não");
    }

    @Override
    public String toCsvLine() {
        // Formato: id;tipo
        return getId() + ";" + tipo.name();
    }

    /**
     * Reconstrói um Perfil a partir de uma linha CSV.
     * MÉTODO ESTÁTICO — fábrica para deserialização
     */
    public static Perfil fromCsvLine(String linha) {
        String[] campos = linha.split(";");
        int id = Integer.parseInt(campos[0]);
        PerfilUsuario tipo = PerfilUsuario.valueOf(campos[1]);
        return new Perfil(id, tipo);
    }
}
