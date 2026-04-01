package enums;

/**
 * ENUM — PerfilUsuario
 * Perfis de acesso ao sistema.
 * POO: Enum com atributo e método — encapsula o comportamento do perfil.
 */
public enum PerfilUsuario {

    ADMIN("Administrador", true),
    OPERADOR("Operador", false);

    private final String descricao;
    private final boolean podeGerenciarUsuarios;

    // Construtor do Enum
    PerfilUsuario(String descricao, boolean podeGerenciarUsuarios) {
        this.descricao = descricao;
        this.podeGerenciarUsuarios = podeGerenciarUsuarios;
    }

    public String getDescricao() { return descricao; }
    public boolean isPodeGerenciarUsuarios() { return podeGerenciarUsuarios; }

    @Override
    public String toString() { return descricao; }
}
