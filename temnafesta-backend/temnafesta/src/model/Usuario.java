package model;

import java.time.LocalDateTime;

/**
 * MODEL — Usuario
 *
 * POO aplicado:
 * - Herança: estende EntidadeBase
 * - Encapsulamento: senha nunca exposta via getter simples
 * - Relacionamento de classes: tem um Perfil (associação por composição)
 * - Polimorfismo de método: implementa descricaoResumida() e toCsvLine()
 *
 * Reflete a tabela 'usuario' do banco de dados.
 */
public class Usuario extends EntidadeBase {

    private String nome;
    private String email;
    private String senha;         // Armazenada como hash em produção real
    private boolean isAtivo;
    private Perfil perfil;        // Relacionamento: Usuario possui um Perfil

    // Construtor padrão
    public Usuario() {
        super();
        this.isAtivo = true;
    }

    // Construtor principal — criação de novo usuário
    public Usuario(String nome, String email, String senha, Perfil perfil) {
        super();
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.isAtivo = true;
        this.perfil = perfil;
    }

    // Construtor de reconstrução via CSV
    public Usuario(int id, String nome, String email, String senha,
                   boolean isAtivo, LocalDateTime dataCriacao, Perfil perfil) {
        super(id, dataCriacao);
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.isAtivo = isAtivo;
        this.perfil = perfil;
    }

    // --- Getters e Setters (Encapsulamento) ---

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Senha: sem getter público — apenas verificação direta
    public boolean verificarSenha(String senhaInformada) {
        return this.senha.equals(senhaInformada);
    }
    public void setSenha(String senha) { this.senha = senha; }
    // Expõe apenas para serialização CSV — uso restrito
    protected String getSenhaInterna() { return senha; }

    public boolean isAtivo() { return isAtivo; }
    public void setAtivo(boolean ativo) { isAtivo = ativo; }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

    // --- Implementação dos métodos abstratos de EntidadeBase ---

    @Override
    public String descricaoResumida() {
        return "Usuário: " + nome + " | Email: " + email +
               " | Perfil: " + (perfil != null ? perfil.getNome() : "N/A") +
               " | Ativo: " + (isAtivo ? "Sim" : "Não");
    }

    @Override
    public String toCsvLine() {
        // Formato: id;nome;email;senha;isAtivo;dataCriacao;perfilId
        return getId() + ";" + nome + ";" + email + ";" + getSenhaInterna() + ";" +
               isAtivo + ";" + getDataCriacao() + ";" +
               (perfil != null ? perfil.getId() : "");
    }
}
