package model;

import interfaces.Validavel;
import interfaces.Notificavel;
import exception.ValidacaoException;
import exception.CampanhaEncerradaException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MODEL — Campanha
 *
 * POO aplicado:
 * - Herança: estende EntidadeBase
 * - Interface: implementa Validavel e Notificavel
 * - Encapsulamento: limitePedidos e pedidosAceitos controlados via métodos
 * - Relacionamento: tem um Cardapio (composição); tem vários Pedidos (agregação)
 * - Polimorfismo de método: implementa descricaoResumida(), toCsvLine(),
 *                           gerarMensagemWhatsApp(), validar()
 *
 * Reflete a tabela 'campanha' do banco de dados.
 * Regras de negócio:
 * - Campanha encerrada: manual OU ao atingir limite de pedidos
 * - Link público pertence à campanha, não ao cardápio
 */
public class Campanha extends EntidadeBase implements Validavel, Notificavel {

    private String nome;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private int limitePedidos;
    private int pedidosAceitos;  // Controlado internamente
    private boolean isAtiva;
    private String linkPublico;

    // Relacionamento: campanha possui um cardápio (composição)
    private Cardapio cardapio;

    // Construtor padrão
    public Campanha() {
        super();
        this.isAtiva = false;
        this.pedidosAceitos = 0;
    }

    // Construtor principal — nova campanha
    public Campanha(String nome, LocalDate dataInicio, LocalDate dataFim, int limitePedidos) {
        super();
        this.nome = nome;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.limitePedidos = limitePedidos;
        this.pedidosAceitos = 0;
        this.isAtiva = true;
        this.linkPublico = gerarLinkPublico();
    }

    // Construtor de reconstrução via CSV
    public Campanha(int id, String nome, LocalDate dataInicio, LocalDate dataFim,
                    int limitePedidos, int pedidosAceitos, boolean isAtiva,
                    String linkPublico, LocalDateTime dataCriacao) {
        super(id, dataCriacao);
        this.nome = nome;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.limitePedidos = limitePedidos;
        this.pedidosAceitos = pedidosAceitos;
        this.isAtiva = isAtiva;
        this.linkPublico = linkPublico;
    }

    // --- Getters e Setters ---

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }

    public int getLimitePedidos() { return limitePedidos; }
    public void setLimitePedidos(int limitePedidos) { this.limitePedidos = limitePedidos; }

    public int getPedidosAceitos() { return pedidosAceitos; }

    public boolean isAtiva() { return isAtiva; }

    public String getLinkPublico() { return linkPublico; }

    public Cardapio getCardapio() { return cardapio; }
    public void setCardapio(Cardapio cardapio) { this.cardapio = cardapio; }

    // --- Regras de negócio ---

    /**
     * Incrementa o contador de pedidos aceitos.
     * Encerra a campanha automaticamente se o limite for atingido.
     * Lança CampanhaEncerradaException se a campanha não estiver disponível.
     */
    public void registrarNovoPedido() {
        if (!isAtiva) {
            throw new CampanhaEncerradaException(nome, "campanha encerrada manualmente");
        }
        if (pedidosAceitos >= limitePedidos) {
            this.isAtiva = false;
            throw new CampanhaEncerradaException(nome, "limite de pedidos atingido");
        }
        this.pedidosAceitos++;
        if (pedidosAceitos >= limitePedidos) {
            this.isAtiva = false; // Encerra automaticamente
        }
    }

    /** Encerra a campanha manualmente. */
    public void encerrar() {
        this.isAtiva = false;
    }

    /** Percentual de ocupação da campanha (KPI). */
    public double getTaxaOcupacao() {
        return limitePedidos > 0 ? (pedidosAceitos * 100.0) / limitePedidos : 0;
    }

    /** Gera o link público único baseado no nome e id. */
    private String gerarLinkPublico() {
        String slug = nome.toLowerCase()
                         .replaceAll("[^a-z0-9]", "-")
                         .replaceAll("-+", "-");
        return "temnafesta.com/cardapio/" + slug + "-" + getId();
    }

    // --- Implementação de Validavel ---

    @Override
    public void validar() throws ValidacaoException {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("nome", "O nome da campanha é obrigatório.");
        }
        if (dataInicio == null) {
            throw new ValidacaoException("dataInicio", "A data de início é obrigatória.");
        }
        if (dataFim == null || dataFim.isBefore(dataInicio)) {
            throw new ValidacaoException("dataFim", "A data de fim deve ser após a data de início.");
        }
        if (limitePedidos <= 0) {
            throw new ValidacaoException("limitePedidos", "O limite de pedidos deve ser positivo.");
        }
    }

    @Override
    public boolean isValido() {
        try { validar(); return true; }
        catch (ValidacaoException e) { return false; }
    }

    // --- Implementação de Notificavel ---

    @Override
    public String gerarMensagemWhatsApp() {
        return "📢 *" + nome + "* está aberta!\n" +
               "📅 De " + dataInicio + " até " + dataFim + "\n" +
               "🍫 Acesse o cardápio: " + linkPublico + "\n" +
               "⚠️ Vagas limitadas: " + pedidosAceitos + "/" + limitePedidos;
    }

    @Override
    public boolean requerNotificacao() {
        // Notifica quando atingiu 80% ou mais do limite
        return isAtiva && getTaxaOcupacao() >= 80.0;
    }

    // --- Implementação dos métodos abstratos de EntidadeBase ---

    @Override
    public String descricaoResumida() {
        return "Campanha: " + nome +
               " | " + dataInicio + " → " + dataFim +
               " | Pedidos: " + pedidosAceitos + "/" + limitePedidos +
               " (" + String.format("%.1f", getTaxaOcupacao()) + "%)" +
               " | " + (isAtiva ? "ATIVA" : "ENCERRADA");
    }

    @Override
    public String toCsvLine() {
        // id;nome;dataInicio;dataFim;limitePedidos;pedidosAceitos;isAtiva;linkPublico;dataCriacao
        return getId() + ";" + nome + ";" + dataInicio + ";" + dataFim + ";" +
               limitePedidos + ";" + pedidosAceitos + ";" + isAtiva + ";" +
               linkPublico + ";" + getDataCriacao();
    }

    public static Campanha fromCsvLine(String linha) {
        String[] c = linha.split(";", -1);
        return new Campanha(
            Integer.parseInt(c[0]),
            c[1],
            LocalDate.parse(c[2]),
            LocalDate.parse(c[3]),
            Integer.parseInt(c[4]),
            Integer.parseInt(c[5]),
            Boolean.parseBoolean(c[6]),
            c[7],
            LocalDateTime.parse(c[8])
        );
    }
}
