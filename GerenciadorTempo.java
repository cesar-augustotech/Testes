package stardew.model;

import stardew.enums.Estacao;
import stardew.interfaces.EventoDiaObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Gerencia o tempo do jogo: hora, dia, estação e ano.
 * O dia tem 20 horas úteis (6h–2h). Ao dormir, avança para o próximo dia.
 */
/**
 * Observer Pattern — sujeito observável.
 * Notifica EventoDiaObserver ao avançar dia (e ao mudar estação).
 */
public class GerenciadorTempo {

    private final List<EventoDiaObserver> observers = new ArrayList<>();

    public void addObserver(EventoDiaObserver obs) {
        observers.add(obs);
    }

    private int hora;   // 6 a 26 (26 = 2h do dia seguinte)
    private int dia;    // 1 a 28 (cada estação tem 28 dias)
    private int ano;
    private Estacao estacao;

    private static final int HORA_INICIO    = 6;
    private static final int HORA_LIMITE    = 26; // 2h – forçar sono
    private static final int DIAS_ESTACAO   = 28;

    /** Construtor para novo jogo. */
    public GerenciadorTempo() {
        this.hora    = HORA_INICIO;
        this.dia     = 1;
        this.ano     = 1;
        this.estacao = Estacao.PRIMAVERA;
    }

    /** Construtor para carregamento do save. */
    public GerenciadorTempo(int hora, int dia, int ano, Estacao estacao) {
        this.hora    = hora;
        this.dia     = dia;
        this.ano     = ano;
        this.estacao = estacao;
    }

    /**
     * Avança o relógio em `horas` unidades.
     * @return true se o limite de hora foi atingido (forçar dormir).
     */
    public boolean avancarHora(int horas) {
        hora += horas;
        return hora >= HORA_LIMITE;
    }

    /** Avança para o próximo dia, notificando todos os observers registrados. */
    public void avancarDia() {
        hora = HORA_INICIO;
        dia++;
        boolean mudouEstacao = dia > DIAS_ESTACAO;
        if (mudouEstacao) {
            dia = 1;
            avancarEstacao();
        }
        // Observer Pattern: notifica cada assinante na ordem de registro
        observers.forEach(o -> {
            o.onDiaAvancou(this);
            if (mudouEstacao) o.onEstacaoMudou(estacao);
        });
    }

    private void avancarEstacao() {
        estacao = switch (estacao) {
            case PRIMAVERA -> Estacao.VERAO;
            case VERAO     -> Estacao.OUTONO;
            case OUTONO    -> Estacao.INVERNO;
            case INVERNO   -> { ano++; yield Estacao.PRIMAVERA; }
        };
    }

    /** Verifica se é tarde da noite (após 22h). */
    public boolean isNoite() { return hora >= 22; }

    /** Verifica se o tempo limite foi atingido. */
    public boolean isDormindo() { return hora >= HORA_LIMITE; }

    /** Retorna a hora formatada em padrão 12h. */
    public String getHoraFormatada() {
        int h = hora % 24;
        String periodo = h < 12 ? "AM" : "PM";
        int h12 = h == 0 ? 12 : h > 12 ? h - 12 : h;
        return String.format("%2d:00 %s", h12, periodo);
    }

    public String getDataFormatada() {
        return String.format("Dia %2d de %s, Ano %d", dia, estacao, ano);
    }

    // Getters
    public int getHora()       { return hora; }
    public int getDia()        { return dia; }
    public int getAno()        { return ano; }
    public Estacao getEstacao(){ return estacao; }

    @Override
    public String toString() {
        return String.format("%s  |  %s", getDataFormatada(), getHoraFormatada());
    }
}
