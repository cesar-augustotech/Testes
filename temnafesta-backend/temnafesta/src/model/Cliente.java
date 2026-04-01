package model;

import interfaces.Validavel;
import exception.ValidacaoException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MODEL — Cliente
 *
 * POO aplicado:
 * - Herança: estende EntidadeBase
 * - Interface: implementa Validavel — garante dados mínimos antes de salvar
 * - Encapsulamento: atributos privados
 * - Relacionamento de classes:
 *     → Composição com Endereco (o endereço pertence ao cliente)
 *     → Agregação com Pedido (o cliente existe independentemente de pedidos)
 * - Polimorfismo de método: implementa descricaoResumida(), toCsvLine(), validar()
 *
 * Reflete a tabela 'cliente' do banco de dados.
 */
public class Cliente extends EntidadeBase implements Validavel {

    private String nome;
    private String telefone;
    private String whatsapp;
    private String instagram;
    private LocalDate dataCadastro;
    private String anotacoes;
    private Endereco endereco;   // Composição — endereço pertence ao cliente

    // Construtor padrão
    public Cliente() {
        super();
        this.dataCadastro = LocalDate.now();
    }

    // Construtor principal — novo cliente
    public Cliente(String nome, String telefone, String whatsapp,
                   String instagram, String anotacoes, Endereco endereco) {
        super();
        this.nome = nome;
        this.telefone = telefone;
        this.whatsapp = whatsapp;
        this.instagram = instagram;
        this.dataCadastro = LocalDate.now();
        this.anotacoes = anotacoes;
        this.endereco = endereco;
    }

    // Construtor de reconstrução via CSV
    public Cliente(int id, String nome, String telefone, String whatsapp,
                   String instagram, LocalDate dataCadastro, String anotacoes,
                   LocalDateTime dataCriacao, Endereco endereco) {
        super(id, dataCriacao);
        this.nome = nome;
        this.telefone = telefone;
        this.whatsapp = whatsapp;
        this.instagram = instagram;
        this.dataCadastro = dataCadastro;
        this.anotacoes = anotacoes;
        this.endereco = endereco;
    }

    // --- Getters e Setters ---

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }

    public String getInstagram() { return instagram; }
    public void setInstagram(String instagram) { this.instagram = instagram; }

    public LocalDate getDataCadastro() { return dataCadastro; }

    public String getAnotacoes() { return anotacoes; }
    public void setAnotacoes(String anotacoes) { this.anotacoes = anotacoes; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    /**
     * Retorna o contato principal do cliente para uso em notificações.
     * Prioridade: WhatsApp > Telefone
     */
    public String getContatoPrincipal() {
        return (whatsapp != null && !whatsapp.isBlank()) ? whatsapp : telefone;
    }

    // --- Implementação de Validavel (Interface) ---

    @Override
    public void validar() throws ValidacaoException {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("nome", "O nome do cliente é obrigatório.");
        }
        if ((telefone == null || telefone.isBlank()) && (whatsapp == null || whatsapp.isBlank())) {
            throw new ValidacaoException("contato", "Informe ao menos telefone ou WhatsApp.");
        }
    }

    @Override
    public boolean isValido() {
        try {
            validar();
            return true;
        } catch (ValidacaoException e) {
            return false;
        }
    }

    // --- Implementação dos métodos abstratos de EntidadeBase ---

    @Override
    public String descricaoResumida() {
        return "Cliente: " + nome +
               " | WhatsApp: " + (whatsapp != null ? whatsapp : "-") +
               " | Instagram: " + (instagram != null ? instagram : "-");
    }

    @Override
    public String toCsvLine() {
        // Formato: id;nome;telefone;whatsapp;instagram;dataCadastro;anotacoes;dataCriacao;enderecoId
        return getId() + ";" + nome + ";" +
               (telefone != null ? telefone : "") + ";" +
               (whatsapp != null ? whatsapp : "") + ";" +
               (instagram != null ? instagram : "") + ";" +
               dataCadastro + ";" +
               (anotacoes != null ? anotacoes.replace(";", ",") : "") + ";" +
               getDataCriacao() + ";" +
               (endereco != null ? endereco.getId() : "");
    }

    /**
     * Reconstrói um Cliente a partir de uma linha CSV.
     * O Endereco deve ser resolvido separadamente pelo Repository.
     */
    public static Cliente fromCsvLine(String linha) {
        String[] c = linha.split(";", -1);
        int id               = Integer.parseInt(c[0]);
        String nome          = c[1];
        String telefone      = c[2];
        String whatsapp      = c[3];
        String instagram     = c[4];
        LocalDate dataCad    = LocalDate.parse(c[5]);
        String anotacoes     = c[6];
        LocalDateTime dataCr = LocalDateTime.parse(c[7]);
        // c[8] = enderecoId — resolvido pelo Repository
        return new Cliente(id, nome, telefone, whatsapp, instagram,
                           dataCad, anotacoes, dataCr, null);
    }
}
