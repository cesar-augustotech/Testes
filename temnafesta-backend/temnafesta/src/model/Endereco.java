package model;

/**
 * MODEL — Endereco
 *
 * POO aplicado:
 * - Herança: estende EntidadeBase
 * - Encapsulamento: atributos privados
 * - Relacionamento: composto pelo Cliente (composição — sem Cliente não existe)
 * - Polimorfismo de método: implementa descricaoResumida() e toCsvLine()
 *
 * Reflete a tabela 'endereco' do banco de dados.
 */
public class Endereco extends EntidadeBase {

    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;       // CHAR(2) no banco — ex: "SP"

    // Construtor padrão
    public Endereco() {
        super();
    }

    // Construtor principal
    public Endereco(String cep, String logradouro, String numero,
                    String complemento, String bairro, String cidade, String estado) {
        super();
        this.cep = cep;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
    }

    // Construtor de reconstrução via CSV
    public Endereco(int id, String cep, String logradouro, String numero,
                    String complemento, String bairro, String cidade, String estado) {
        super(id);
        this.cep = cep;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
    }

    // --- Getters e Setters ---

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    /**
     * Retorna o endereço formatado em linha única.
     * POLIMORFISMO DE MÉTODO — sobrecarga (overloading)
     */
    public String formatado() {
        return logradouro + ", " + numero + " - " + bairro + ", " + cidade + "/" + estado;
    }

    public String formatado(boolean incluirCep) {
        return incluirCep ? "CEP " + cep + " — " + formatado() : formatado();
    }

    // --- Implementação dos métodos abstratos de EntidadeBase ---

    @Override
    public String descricaoResumida() {
        return formatado(true);
    }

    @Override
    public String toCsvLine() {
        // Formato: id;cep;logradouro;numero;complemento;bairro;cidade;estado
        return getId() + ";" + cep + ";" + logradouro + ";" + numero + ";" +
               (complemento != null ? complemento : "") + ";" +
               bairro + ";" + cidade + ";" + estado;
    }
}
