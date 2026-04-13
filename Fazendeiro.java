package stardew.model;

import stardew.enums.Estacao;
import stardew.enums.Localizacao;
import stardew.exceptions.AcaoInvalidaException;
import stardew.exceptions.ColheitaForaDeEstacaoException;
import stardew.exceptions.SaldoInsuficienteException;
import stardew.interfaces.Coletavel;
import stardew.interfaces.Item;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * O jogador. Herda de Personagem e é a classe central do jogo.
 *
 * Conceitos aplicados:
 *   - Herança de classe abstrata
 *   - Polimorfismo (override de agir())
 *   - Relacionamento de composição (Habilidades, Inventario, Fazenda)
 *   - Exceções personalizadas lançadas nos métodos de ação
 *   - Streams (delegados ao Inventário e Fazenda)
 */
public class Fazendeiro extends Personagem {

    private double dinheiro;
    private final Inventario inventario;
    private final Fazenda    fazenda;
    private final Habilidades habilidades;

    private static final Random RAND = new Random();

    // Custo de energia por ação
    private static final int ENERGIA_PLANTAR  = 5;
    private static final int ENERGIA_COLHER   = 3;
    private static final int ENERGIA_IRRIGAR  = 2;
    private static final int ENERGIA_PESCAR   = 8;
    private static final int ENERGIA_MINERAR  = 10;
    private static final int ENERGIA_LUTAR    = 12;
    private static final int ENERGIA_CORTAR   = 8;
    private static final int ENERGIA_COLETAR  = 3;
    private static final int ENERGIA_CUIDAR   = 2;

    /** Construtor para novo jogo. */
    public Fazendeiro(String nome, String nomeFazenda) {
        super(nome, 270, Localizacao.FAZENDA);
        this.dinheiro    = 500.0;
        this.inventario  = new Inventario(24);
        this.fazenda     = new Fazenda(nomeFazenda);
        this.habilidades = new Habilidades();
        equiparFerramentasIniciais();
    }

    /** Construtor para carregamento do save. */
    public Fazendeiro(String nome, double dinheiro, Inventario inventario,
                      Fazenda fazenda, Habilidades habilidades) {
        super(nome, 270, Localizacao.FAZENDA);
        this.dinheiro    = dinheiro;
        this.inventario  = inventario;
        this.fazenda     = fazenda;
        this.habilidades = habilidades;
    }

    private void equiparFerramentasIniciais() {
        inventario.adicionarItem(new Ferramenta("Enxada",     0, 100, "plantar"));
        inventario.adicionarItem(new Ferramenta("Regador",    0, 100, "irrigar"));
        inventario.adicionarItem(new Ferramenta("Foice",      0, 100, "colher"));
        inventario.adicionarItem(new Ferramenta("Picareta",   0, 100, "minerar"));
        inventario.adicionarItem(new Ferramenta("Vara de Pesca", 0, 100, "pescar"));
        inventario.adicionarItem(new Ferramenta("Espada",     0, 100, "lutar"));
        // Sementes iniciais
        inventario.adicionarItem(new Semente("Semente de Morango", 35, 8, Estacao.PRIMAVERA, "Morango"));
        inventario.adicionarItem(new Semente("Semente de Morango", 35, 8, Estacao.PRIMAVERA, "Morango"));
        inventario.adicionarItem(new Semente("Semente de Batata",  20, 6, Estacao.PRIMAVERA, "Batata"));
    }

    // ══════════════════════════════════════════════════════════════
    //  POLIMORFISMO — implementação do método abstrato de Personagem
    // ══════════════════════════════════════════════════════════════

    @Override
    public String agir() {
        return String.format(
            "%s está na %s com %d de energia e G%.0f.",
            getNome(), getLocalizacao(), getEnergia(), dinheiro
        );
    }

    // ══════════════════════════════════════════════════════════════
    //  AÇÕES — lançam AcaoInvalidaException se local errado
    // ══════════════════════════════════════════════════════════════

    /** Planta uma semente em uma parcela livre. */
    public String plantar(Semente semente, int idParcela, Estacao estacaoAtual)
            throws AcaoInvalidaException, ColheitaForaDeEstacaoException {

        validarAcao("plantar");
        if (!semente.podeSerPlantada(estacaoAtual)) {
            throw new ColheitaForaDeEstacaoException(semente.getCulturaGerada(), estacaoAtual);
        }
        consumirEnergia(ENERGIA_PLANTAR);
        Parcela parcela = fazenda.getParcela(idParcela);
        if (parcela == null)        return "Parcela não encontrada.";
        if (!parcela.estaLivre())   return "Parcela #" + idParcela + " já está ocupada!";
        inventario.removerItem(semente);
        parcela.plantarSemente(semente);
        boolean subiu = habilidades.ganharXp(Habilidades.Tipo.COLHEITA, 10);
        return String.format("Plantou %s na parcela #%d.%s",
            semente.getCulturaGerada(), idParcela,
            subiu ? " [NÍVEL UP: Colheita!]" : "");
    }

    /** Irriga uma parcela. */
    public String irrigar(int idParcela) throws AcaoInvalidaException {
        validarAcao("irrigar");
        consumirEnergia(ENERGIA_IRRIGAR);
        Parcela p = fazenda.getParcela(idParcela);
        if (p == null) return "Parcela não encontrada.";
        p.irrigar();
        return "Parcela #" + idParcela + " irrigada!";
    }

    /** Colhe uma parcela pronta. */
    public String colher(int idParcela, Estacao estacaoAtual) throws AcaoInvalidaException {
        validarAcao("colher");
        Parcela p = fazenda.getParcela(idParcela);
        if (p == null) return "Parcela não encontrada.";
        if (!p.estaProxima()) return "Parcela #" + idParcela + " ainda não está pronta!";
        consumirEnergia(ENERGIA_COLHER);
        Colheita c = p.colher(estacaoAtual, habilidades.getNivel(Habilidades.Tipo.COLHEITA));
        if (c == null) return "Nada para colher.";
        inventario.adicionarItem(c);
        boolean subiu = habilidades.ganharXp(Habilidades.Tipo.COLHEITA, 20);
        return String.format("Colheu %s (%s)!%s",
            c.getNome(), c.getNomeQualidade(),
            subiu ? " [NÍVEL UP: Colheita!]" : "");
    }

    /** Cuida dos animais (alimenta todos). */
    public String cuidarAnimais() throws AcaoInvalidaException {
        validarAcao("cuidar_animais");
        List<Animal> animais = fazenda.getAnimaisInterno();
        if (animais.isEmpty()) return "Você não tem animais.";
        animais.forEach(a -> {
            a.alimentar();
            consumirEnergia(ENERGIA_CUIDAR);
        });
        // Coleta produção dos animais felizes
        StringBuilder sb = new StringBuilder("Animais cuidados!\n");
        animais.stream()
               .map(Animal::produzir)
               .filter(item -> item != null)
               .forEach(item -> {
                   inventario.adicionarItem(item);
                   sb.append("  + ").append(item.getNome()).append("\n");
               });
        return sb.toString().trim();
    }

    /** Pesca na Praia. Usa bônus de Pesca para calcular resultado. */
    public String pescar() throws AcaoInvalidaException {
        validarAcao("pescar");
        if (!temEnergia(ENERGIA_PESCAR)) return "Sem energia para pescar!";
        consumirEnergia(ENERGIA_PESCAR);

        double bonus = habilidades.getBonus(Habilidades.Tipo.PESCA);
        double chance = 0.5 + (bonus - 1.0) * 2; // 50% base + 10% por nível
        if (RAND.nextDouble() > chance) {
            habilidades.ganharXp(Habilidades.Tipo.PESCA, 5);
            return "Você não pescou nada desta vez...";
        }

        String[] peixes = { "Anchova", "Sardinha", "Atum", "Salmão", "Peixe Raro" };
        double[] valores = { 30, 75, 200, 350, 900 };
        // Peixes mais raros (índices maiores) têm menor chance
        int idx = (int)(RAND.nextDouble() * bonus) % peixes.length;
        Item peixe = new RecursoSilvestre(peixes[idx], valores[idx],
                "Peixe", List.of(Localizacao.PRAIA));
        inventario.adicionarItem(peixe);
        boolean subiu = habilidades.ganharXp(Habilidades.Tipo.PESCA, 20);
        return String.format("Pescou um(a) %s! (G%.0f)%s",
            peixe.getNome(), peixe.getValor(),
            subiu ? " [NÍVEL UP: Pesca!]" : "");
    }

    /** Minera nas Minas. Retorna um recurso mineral. */
    public String minerar() throws AcaoInvalidaException {
        validarAcao("minerar");
        if (!temEnergia(ENERGIA_MINERAR)) return "Sem energia para minerar!";
        consumirEnergia(ENERGIA_MINERAR);

        double bonus = habilidades.getBonus(Habilidades.Tipo.MINERADOR);
        String[] minerios = { "Pedra", "Carvão", "Minério de Cobre", "Minério de Ferro", "Minério de Ouro" };
        double[] valores   = { 5,       15,        50,                  100,                500 };
        int idx = Math.min(minerios.length - 1,
                  (int)(RAND.nextDouble() * bonus * minerios.length / 2));
        Item mineral = new RecursoSilvestre(minerios[idx], valores[idx],
                "Mineral", List.of(Localizacao.MINAS));
        inventario.adicionarItem(mineral);
        boolean subiu = habilidades.ganharXp(Habilidades.Tipo.MINERADOR, 15);
        return String.format("Minerou %s! (G%.0f)%s",
            mineral.getNome(), mineral.getValor(),
            subiu ? " [NÍVEL UP: Minerador!]" : "");
    }

    /** Combate por turnos simplificado com um Inimigo. */
    public String lutar(Inimigo inimigo) throws AcaoInvalidaException {
        validarAcao("lutar");
        if (!temEnergia(ENERGIA_LUTAR)) return "Sem energia para lutar!";
        consumirEnergia(ENERGIA_LUTAR);

        double bonus = habilidades.getBonus(Habilidades.Tipo.LUTADOR);
        int danoJogador = (int)(15 * bonus) + RAND.nextInt(10);
        inimigo.receberDano(danoJogador);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Você atacou %s causando %d de dano!\n", inimigo.getNome(), danoJogador));

        if (inimigo.estaMorto()) {
            Item drop = inimigo.drop();
            inventario.adicionarItem(drop);
            boolean subiu = habilidades.ganharXp(Habilidades.Tipo.LUTADOR, inimigo.getXpRecompensa());
            sb.append(String.format("  %s foi derrotado! Drop: %s%s",
                inimigo.getNome(), drop.getNome(),
                subiu ? " [NÍVEL UP: Lutador!]" : ""));
        } else {
            int danoRecebido = inimigo.atacar();
            consumirEnergia(danoRecebido / 3);
            sb.append(String.format("  %s contra-atacou causando %d de dano! HP restante: %d/%d",
                inimigo.getNome(), danoRecebido, inimigo.getVida(), inimigo.getVidaMax()));
        }
        return sb.toString().trim();
    }

    /** Corta árvore na Floresta. */
    public String cortarArvore() throws AcaoInvalidaException {
        validarAcao("cortar_arvore");
        if (!temEnergia(ENERGIA_CORTAR)) return "Sem energia!";
        consumirEnergia(ENERGIA_CORTAR);
        double bonus = habilidades.getBonus(Habilidades.Tipo.LENHADOR);
        int qtd = 1 + (int)(bonus * RAND.nextInt(3));
        for (int i = 0; i < qtd; i++) {
            inventario.adicionarItem(new RecursoSilvestre("Madeira", 10, "Madeira",
                    List.of(Localizacao.FLORESTA, Localizacao.FAZENDA)));
        }
        boolean subiu = habilidades.ganharXp(Habilidades.Tipo.LENHADOR, 15);
        return String.format("Coletou %dx Madeira!%s", qtd,
            subiu ? " [NÍVEL UP: Lenhador!]" : "");
    }

    /** Coleta um recurso silvestre (disponível em múltiplos locais). */
    public String coletarRecurso(Coletavel recurso) throws AcaoInvalidaException {
        validarAcao("coletar_recurso");
        if (!recurso.podeColetarEm(getLocalizacao())) {
            return "Este recurso não está disponível aqui!";
        }
        consumirEnergia(ENERGIA_COLETAR);
        Item item = recurso.coletar();
        inventario.adicionarItem(item);
        habilidades.ganharXp(Habilidades.Tipo.COLHEITA, 5);
        return "Coletou: " + item.getNome();
    }

    /** Vende um item. Adiciona o valor ao dinheiro. */
    public String vender(String nomeItem) throws AcaoInvalidaException, SaldoInsuficienteException {
        validarAcao("vender");
        Optional<Item> opt = inventario.buscarPorNome(nomeItem);
        if (opt.isEmpty()) return "Item '" + nomeItem + "' não encontrado no inventário.";
        Item item = opt.get();
        inventario.removerItem(item);
        dinheiro += item.getValor();
        return String.format("Vendeu %s por G%.0f. Saldo: G%.0f",
            item.getNome(), item.getValor(), dinheiro);
    }

    /** Compra um item (de um NPC). */
    public void comprar(Item item) throws SaldoInsuficienteException {
        if (dinheiro < item.getValor()) {
            throw new SaldoInsuficienteException(dinheiro, item.getValor());
        }
        dinheiro -= item.getValor();
        inventario.adicionarItem(item);
    }

    // ── Validação de ação por localização ────────────────────────

    private void validarAcao(String acao) throws AcaoInvalidaException {
        if (!getLocalizacao().permiteAcao(acao)) {
            throw new AcaoInvalidaException(acao, getLocalizacao());
        }
    }

    // Getters
    public double getDinheiro()          { return dinheiro; }
    public Inventario getInventario()    { return inventario; }
    public Fazenda getFazenda()          { return fazenda; }
    public Habilidades getHabilidades()  { return habilidades; }
}
