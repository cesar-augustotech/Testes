package stardew.persistence;

import stardew.enums.Estacao;
import stardew.enums.Localizacao;
import stardew.model.*;
import stardew.interfaces.Item;

import stardew.interfaces.RepositorioJogo;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsável pela persistência do jogo em arquivos CSV.
 *
 * Arquivos gerados:
 *   saves/fazendeiro.csv  — dados do jogador + habilidades
 *   saves/fazenda.csv     — parcelas e animais
 *   saves/inventario.csv  — itens do inventário
 *   saves/tempo.csv       — hora, dia, estação, ano
 *
 * Usa APENAS FileReader, BufferedReader, FileWriter e BufferedWriter.
 * Cada linha CSV segue o padrão: campo1,campo2,...
 */
/**
 * Adapter Pattern — adapta FileReader/BufferedReader para o contrato RepositorioJogo.
 * GameEngine depende de RepositorioJogo, não desta classe concreta.
 */
public class PersistenciaCSV implements RepositorioJogo {

    private static final String DIRETORIO = "saves/";
    private static final String FAZENDEIRO = DIRETORIO + "fazendeiro.csv";
    private static final String FAZENDA    = DIRETORIO + "fazenda.csv";
    private static final String INVENTARIO = DIRETORIO + "inventario.csv";
    private static final String TEMPO      = DIRETORIO + "tempo.csv";

    // ════════════════════════════════════════════════════════
    //  SALVAR
    // ════════════════════════════════════════════════════════

    @Override
    public void salvar(Fazendeiro fazendeiro, GerenciadorTempo tempo) throws IOException {
        criarDiretorio();
        salvarFazendeiro(fazendeiro);
        salvarFazenda(fazendeiro.getFazenda());
        salvarInventario(fazendeiro.getInventario());
        salvarTempo(tempo);
    }

    /** fazendeiro.csv: nome,dinheiro,energia,local,nvPesca,nvColheita,nvLenhador,nvMinerador,nvLutador,
     *                  xpPesca,xpColheita,xpLenhador,xpMinerador,xpLutador */
    private void salvarFazendeiro(Fazendeiro f) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FAZENDEIRO))) {
            bw.write("nome,dinheiro,energia,local,nvPesca,nvColheita,nvLenhador,nvMinerador,nvLutador," +
                     "xpPesca,xpColheita,xpLenhador,xpMinerador,xpLutador");
            bw.newLine();
            Habilidades h = f.getHabilidades();
            bw.write(String.join(",",
                escapar(f.getNome()),
                String.valueOf(f.getDinheiro()),
                String.valueOf(f.getEnergia()),
                f.getLocalizacao().name(),
                String.valueOf(h.getPesca()),
                String.valueOf(h.getColheita()),
                String.valueOf(h.getLenhador()),
                String.valueOf(h.getMinerador()),
                String.valueOf(h.getLutador()),
                String.valueOf(h.getXpPesca()),
                String.valueOf(h.getXpColheita()),
                String.valueOf(h.getXpLenhador()),
                String.valueOf(h.getXpMinerador()),
                String.valueOf(h.getXpLutador())
            ));
            bw.newLine();
        }
    }

    /** fazenda.csv — seções separadas por cabeçalho:
     *   PARCELAS: id,irrigada,nomeSemente,valorSemente,dias,diasCrescimento,estacaoSemente,culturaGerada
     *   ANIMAIS:  nome,tipo,felicidade */
    private void salvarFazenda(Fazenda fazenda) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FAZENDA))) {
            bw.write("nome_fazenda," + escapar(fazenda.getNome())); bw.newLine();

            bw.write("PARCELAS"); bw.newLine();
            bw.write("id,irrigada,nomeSemente,valorSemente,dias,diasCrescimento,estacaoSemente,culturaGerada");
            bw.newLine();
            for (Parcela p : fazenda.getParcelas()) {
                if (p.getSemente() == null) {
                    bw.write(p.getId() + "," + p.isIrrigada() + ",,,0,0,,");
                } else {
                    Semente s = p.getSemente();
                    bw.write(String.join(",",
                        String.valueOf(p.getId()),
                        String.valueOf(p.isIrrigada()),
                        escapar(s.getNome()),
                        String.valueOf(s.getValor()),
                        String.valueOf(p.getDiasPlantado()),
                        String.valueOf(s.getDiasCrescimento()),
                        s.getEstacao().name(),
                        escapar(s.getCulturaGerada())
                    ));
                }
                bw.newLine();
            }

            bw.write("ANIMAIS"); bw.newLine();
            bw.write("nome,tipo,felicidade"); bw.newLine();
            for (Animal a : fazenda.getAnimais()) {
                bw.write(String.join(",",
                    escapar(a.getNomeAnimal()),   // nome puro, sem "(tipo)"
                    escapar(a.getTipoAnimal()),   // tipo puro: galinha, vaca etc.
                    String.valueOf(a.getFelicidade())
                ));
                bw.newLine();
            }
        }
    }

    /** inventario.csv: tipo,nome,valor,[camposExtras...] */
    private void salvarInventario(Inventario inv) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(INVENTARIO))) {
            bw.write("tipo,nome,valor,extra1,extra2,extra3"); bw.newLine();
            for (Item item : inv.getItens()) {
                if ("Animal".equals(item.getTipo())) continue; // animais ficam na fazenda
                String linha = switch (item.getTipo()) {
                    case "Semente" -> {
                        Semente s = (Semente) item;
                        yield String.join(",",
                            "Semente", escapar(s.getNome()), String.valueOf(s.getValor()),
                            String.valueOf(s.getDiasCrescimento()),
                            s.getEstacao().name(), escapar(s.getCulturaGerada()));
                    }
                    case "Colheita" -> {
                        Colheita c = (Colheita) item;
                        String est = c.getEstacao() != null ? c.getEstacao().name() : "";
                        yield String.join(",",
                            "Colheita", escapar(c.getNome()), String.valueOf(c.getValor()),
                            String.valueOf(c.getQualidade()), est, "");
                    }
                    case "Ferramenta" -> {
                        Ferramenta f = (Ferramenta) item;
                        yield String.join(",",
                            "Ferramenta", escapar(f.getNome()), String.valueOf(f.getValor()),
                            String.valueOf(f.getDurabilidade()),
                            String.valueOf(f.getDurabilidadeMax()),
                            escapar(f.getTipoFerramenta()));
                    }
                    case "Recurso" -> {
                        RecursoSilvestre r = (RecursoSilvestre) item;
                        yield String.join(",",
                            "Recurso", escapar(r.getNome()), String.valueOf(r.getValor()),
                            escapar(r.getCategoria()), "", "");
                    }
                    default -> String.join(",", item.getTipo(), escapar(item.getNome()),
                                String.valueOf(item.getValor()), "", "", "");
                };
                bw.write(linha); bw.newLine();
            }
        }
    }

    /** tempo.csv: hora,dia,ano,estacao */
    private void salvarTempo(GerenciadorTempo t) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TEMPO))) {
            bw.write("hora,dia,ano,estacao"); bw.newLine();
            bw.write(String.join(",",
                String.valueOf(t.getHora()),
                String.valueOf(t.getDia()),
                String.valueOf(t.getAno()),
                t.getEstacao().name()
            ));
            bw.newLine();
        }
    }

    // ════════════════════════════════════════════════════════
    //  CARREGAR
    // ════════════════════════════════════════════════════════

    @Override
    public boolean saveExiste() {
        return new File(FAZENDEIRO).exists() &&
               new File(TEMPO).exists() &&
               new File(INVENTARIO).exists() &&
               new File(FAZENDA).exists();
    }

    /** Carrega o tempo do arquivo tempo.csv. */
    @Override
    public GerenciadorTempo carregarTempo() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(TEMPO))) {
            br.readLine(); // cabeçalho
            String linha = br.readLine();
            if (linha == null) throw new IOException("tempo.csv vazio.");
            String[] c = linha.split(",", -1);
            return new GerenciadorTempo(
                Integer.parseInt(c[0].trim()),
                Integer.parseInt(c[1].trim()),
                Integer.parseInt(c[2].trim()),
                Estacao.valueOf(c[3].trim())
            );
        }
    }

    /** Carrega o inventário do arquivo inventario.csv. */
    @Override
    public Inventario carregarInventario() throws IOException {
        Inventario inv = new Inventario(24);
        try (BufferedReader br = new BufferedReader(new FileReader(INVENTARIO))) {
            br.readLine(); // cabeçalho
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.isBlank()) continue;
                String[] c = linha.split(",", -1);
                Item item = reconstruirItem(c);
                if (item != null) inv.adicionarItem(item);
            }
        }
        return inv;
    }

    private Item reconstruirItem(String[] c) {
        if (c.length < 3) return null;
        String tipo  = c[0].trim();
        String nome  = c[1].trim();
        double valor = parseDouble(c[2]);
        return switch (tipo) {
            case "Semente"    -> new Semente(nome, valor,
                                    parseInt(c[3]), Estacao.valueOf(c[4].trim()), c[5].trim());
            case "Colheita"   -> {
                String estStr = c[4].trim();
                Estacao est = estStr.isEmpty() ? null : Estacao.valueOf(estStr);
                yield new Colheita(nome, valor, parseInt(c[3]), est);
            }
            case "Ferramenta" -> new Ferramenta(nome, valor,
                                    parseInt(c[3]), parseInt(c[4]), c[5].trim());
            case "Recurso"    -> new RecursoSilvestre(nome, valor, c[3].trim(),
                                    List.of(Localizacao.FAZENDA));
            default           -> null;
        };
    }

    /** Carrega fazenda e animais de fazenda.csv. */
    @Override
    public Fazenda carregarFazenda() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(FAZENDA))) {
            String primeiraLinha = br.readLine();
            String nomeFazenda = "Minha Fazenda";
            if (primeiraLinha != null && primeiraLinha.startsWith("nome_fazenda,")) {
                nomeFazenda = primeiraLinha.split(",", 2)[1].trim();
            }

            List<Parcela> parcelas = new ArrayList<>();
            List<Animal>  animais  = new ArrayList<>();
            String secao = "";
            String linha;

            while ((linha = br.readLine()) != null) {
                if (linha.isBlank()) continue;
                if (linha.startsWith("PARCELAS")) { secao = "PARCELAS"; br.readLine(); continue; }
                if (linha.startsWith("ANIMAIS"))  { secao = "ANIMAIS";  br.readLine(); continue; }

                String[] c = linha.split(",", -1);
                if ("PARCELAS".equals(secao)) {
                    int id = parseInt(c[0]);
                    boolean irrig = Boolean.parseBoolean(c[1].trim());
                    Semente s = null;
                    if (c.length > 2 && !c[2].isBlank()) {
                        s = new Semente(c[2].trim(), parseDouble(c[3]),
                                parseInt(c[5]), Estacao.valueOf(c[6].trim()), c[7].trim());
                    }
                    int dias = c.length > 4 ? parseInt(c[4]) : 0;
                    parcelas.add(new Parcela(id, irrig, s, dias));
                } else if ("ANIMAIS".equals(secao)) {
                    if (c.length >= 3) {
                        animais.add(new Animal(c[0].trim(), c[1].trim(), parseInt(c[2])));
                    }
                }
            }
            return new Fazenda(nomeFazenda, parcelas, animais);
        }
    }

    /** Carrega fazendeiro (incluindo habilidades) de fazendeiro.csv. */
    @Override
    public Fazendeiro carregarFazendeiro(Inventario inv, Fazenda fazenda) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(FAZENDEIRO))) {
            br.readLine(); // cabeçalho
            String linha = br.readLine();
            if (linha == null) throw new IOException("fazendeiro.csv vazio.");
            String[] c = linha.split(",", -1);
            String nome = c[0].trim();
            double dinheiro = parseDouble(c[1]);
            // c[2] = energia (restaurada pelo construtor)
            // c[3] = localizacao

            Habilidades h = new Habilidades(
                parseInt(c[4]), parseInt(c[5]), parseInt(c[6]), parseInt(c[7]), parseInt(c[8]),
                parseInt(c[9]), parseInt(c[10]), parseInt(c[11]), parseInt(c[12]), parseInt(c[13])
            );
            return new Fazendeiro(nome, dinheiro, inv, fazenda, h);
        }
    }

    // ════════════════════════════════════════════════════════
    //  Utilitários
    // ════════════════════════════════════════════════════════

    private void criarDiretorio() {
        new File(DIRETORIO).mkdirs();
    }

    private static String escapar(String valor) {
        if (valor == null) return "";
        // Substitui vírgulas por ponto-e-vírgula para não quebrar o CSV
        return valor.replace(",", ";");
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private static double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }
}
