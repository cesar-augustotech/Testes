package repository;

import interfaces.Persistivel;
import model.Campanha;
import exception.CsvException;
import exception.EntidadeNaoEncontradaException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY — CampanhaRepository
 *
 * POO aplicado:
 * - Interface: implementa Persistivel<Campanha>
 * - Polimorfismo de classe: é um Persistivel
 * - Persistência: usa CsvUtil (FileReader + BufferedReader internamente)
 */
public class CampanhaRepository implements Persistivel<Campanha> {

    private static final String CAMINHO_ARQUIVO = CsvUtil.DIRETORIO + "campanhas.csv";
    private static final String CABECALHO =
        "id;nome;dataInicio;dataFim;limitePedidos;pedidosAceitos;isAtiva;linkPublico;dataCriacao";

    private List<Campanha> cache;

    public CampanhaRepository() {
        this.cache = new ArrayList<>();
        try { this.cache = carregarTodos(); }
        catch (Exception e) { System.err.println("[CampanhaRepository] Aviso: " + e.getMessage()); }
    }

    @Override
    public void salvarTodos(List<Campanha> campanhas) {
        List<String> linhas = new ArrayList<>();
        for (Campanha c : campanhas) linhas.add(c.toCsvLine());
        try {
            CsvUtil.escreverLinhas(CAMINHO_ARQUIVO, CABECALHO, linhas);
            this.cache = new ArrayList<>(campanhas);
        } catch (CsvException e) {
            System.err.println("[CampanhaRepository] Erro ao salvar: " + e.getMessage());
        }
    }

    @Override
    public List<Campanha> carregarTodos() {
        List<Campanha> campanhas = new ArrayList<>();
        try {
            for (String linha : CsvUtil.lerLinhas(CAMINHO_ARQUIVO))
                campanhas.add(Campanha.fromCsvLine(linha));
        } catch (CsvException e) {
            System.err.println("[CampanhaRepository] Erro ao carregar: " + e.getMessage());
        }
        return campanhas;
    }

    @Override
    public String getCaminhoArquivo() { return CAMINHO_ARQUIVO; }

    public Campanha salvar(Campanha campanha) {
        List<String> linhasRaw = new ArrayList<>();
        try { linhasRaw = CsvUtil.lerLinhas(CAMINHO_ARQUIVO); }
        catch (CsvException ignored) {}
        campanha.setId(CsvUtil.proximoId(linhasRaw));
        cache.add(campanha);
        salvarTodos(cache);
        return campanha;
    }

    public Campanha atualizar(Campanha campanhaAtualizada) {
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).getId() == campanhaAtualizada.getId()) {
                cache.set(i, campanhaAtualizada);
                salvarTodos(cache);
                return campanhaAtualizada;
            }
        }
        throw new EntidadeNaoEncontradaException("Campanha", campanhaAtualizada.getId());
    }

    public Optional<Campanha> buscarPorId(int id) {
        return cache.stream().filter(c -> c.getId() == id).findFirst();
    }

    /** Retorna a campanha ativa atual (deve existir no máximo uma). */
    public Optional<Campanha> buscarAtiva() {
        return cache.stream().filter(Campanha::isAtiva).findFirst();
    }

    public List<Campanha> listarTodos() { return new ArrayList<>(cache); }

    /** Retorna campanhas encerradas (histórico). */
    public List<Campanha> listarEncerradas() {
        List<Campanha> encerradas = new ArrayList<>();
        for (Campanha c : cache) if (!c.isAtiva()) encerradas.add(c);
        return encerradas;
    }
}
