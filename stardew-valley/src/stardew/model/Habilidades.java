package stardew.model;

/**
 * Encapsula as 5 habilidades do fazendeiro.
 * Cada habilidade tem nível (1-10) e XP acumulado.
 * O bônus de habilidade influencia diretamente as ações do jogo.
 */
public class Habilidades {

    private int pesca;
    private int colheita;
    private int lenhador;
    private int minerador;
    private int lutador;

    private int xpPesca;
    private int xpColheita;
    private int xpLenhador;
    private int xpMinerador;
    private int xpLutador;

    public static final int XP_POR_NIVEL = 100;
    public static final int NIVEL_MAX    = 10;

    /** Enum interno que identifica cada habilidade. */
    public enum Tipo {
        PESCA("Pesca"),
        COLHEITA("Colheita"),
        LENHADOR("Lenhador"),
        MINERADOR("Minerador"),
        LUTADOR("Lutador");

        private final String nome;
        Tipo(String nome) { this.nome = nome; }
        public String getNome() { return nome; }
    }

    /** Construtor para novo jogo — todas as habilidades começam no nível 1. */
    public Habilidades() {
        this.pesca = 1; this.colheita = 1; this.lenhador = 1;
        this.minerador = 1; this.lutador = 1;
    }

    /** Construtor para carregamento do save. */
    public Habilidades(int pesca, int colheita, int lenhador, int minerador, int lutador,
                       int xpPesca, int xpColheita, int xpLenhador, int xpMinerador, int xpLutador) {
        this.pesca = pesca; this.colheita = colheita;
        this.lenhador = lenhador; this.minerador = minerador; this.lutador = lutador;
        this.xpPesca = xpPesca; this.xpColheita = xpColheita;
        this.xpLenhador = xpLenhador; this.xpMinerador = xpMinerador; this.xpLutador = xpLutador;
    }

    /**
     * Adiciona XP a uma habilidade.
     * @return true se o nível subiu.
     */
    public boolean ganharXp(Tipo tipo, int xp) {
        return switch (tipo) {
            case PESCA      -> { xpPesca      += xp; if (xpPesca      >= XP_POR_NIVEL && pesca      < NIVEL_MAX) { pesca++;      xpPesca      -= XP_POR_NIVEL; yield true; } yield false; }
            case COLHEITA   -> { xpColheita   += xp; if (xpColheita   >= XP_POR_NIVEL && colheita   < NIVEL_MAX) { colheita++;   xpColheita   -= XP_POR_NIVEL; yield true; } yield false; }
            case LENHADOR   -> { xpLenhador   += xp; if (xpLenhador   >= XP_POR_NIVEL && lenhador   < NIVEL_MAX) { lenhador++;   xpLenhador   -= XP_POR_NIVEL; yield true; } yield false; }
            case MINERADOR  -> { xpMinerador  += xp; if (xpMinerador  >= XP_POR_NIVEL && minerador  < NIVEL_MAX) { minerador++;  xpMinerador  -= XP_POR_NIVEL; yield true; } yield false; }
            case LUTADOR    -> { xpLutador    += xp; if (xpLutador    >= XP_POR_NIVEL && lutador    < NIVEL_MAX) { lutador++;    xpLutador    -= XP_POR_NIVEL; yield true; } yield false; }
        };
    }

    /**
     * Calcula o multiplicador de bônus de uma habilidade.
     * Nível 1 = 1.0x, Nível 10 = 1.9x (incremento de 10% por nível).
     */
    public double getBonus(Tipo tipo) {
        return 1.0 + (getNivel(tipo) - 1) * 0.10;
    }

    public int getNivel(Tipo tipo) {
        return switch (tipo) {
            case PESCA     -> pesca;
            case COLHEITA  -> colheita;
            case LENHADOR  -> lenhador;
            case MINERADOR -> minerador;
            case LUTADOR   -> lutador;
        };
    }

    public int getXp(Tipo tipo) {
        return switch (tipo) {
            case PESCA     -> xpPesca;
            case COLHEITA  -> xpColheita;
            case LENHADOR  -> xpLenhador;
            case MINERADOR -> xpMinerador;
            case LUTADOR   -> xpLutador;
        };
    }

    // Getters individuais (usados na persistência)
    public int getPesca()      { return pesca; }
    public int getColheita()   { return colheita; }
    public int getLenhador()   { return lenhador; }
    public int getMinerador()  { return minerador; }
    public int getLutador()    { return lutador; }
    public int getXpPesca()    { return xpPesca; }
    public int getXpColheita() { return xpColheita; }
    public int getXpLenhador() { return xpLenhador; }
    public int getXpMinerador(){ return xpMinerador; }
    public int getXpLutador()  { return xpLutador; }

    @Override
    public String toString() {
        return String.format(
            "  Pesca:     Nv.%2d  (%3d/%d xp)  |  Colheita:  Nv.%2d  (%3d/%d xp)%n" +
            "  Lenhador:  Nv.%2d  (%3d/%d xp)  |  Minerador: Nv.%2d  (%3d/%d xp)%n" +
            "  Lutador:   Nv.%2d  (%3d/%d xp)",
            pesca, xpPesca, XP_POR_NIVEL,
            colheita, xpColheita, XP_POR_NIVEL,
            lenhador, xpLenhador, XP_POR_NIVEL,
            minerador, xpMinerador, XP_POR_NIVEL,
            lutador, xpLutador, XP_POR_NIVEL
        );
    }
}
