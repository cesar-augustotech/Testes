package repository;

import interfaces.Persistivel;
import model.Pedido;
import enums.StatusProducao;
import exception.CsvException;
import exception.EntidadeNaoEncontradaException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY — PedidoRepository
 *
 * POO aplicado:
 * - Interface: implementa Persistivel<Pedido>
 * - Polimorfismo de classe: é um Persistivel
 * - Persistência: usa CsvUtil (FileReader + BufferedReader internamente)
 *
 * Nota: itens e histórico de cada pedido são gerenciados em memória.
 * Em uma versão futura com banco de dados, seriam tabelas separadas.
 */
public class PedidoRepository implements Persistivel<Pedido> {

    private static final String CAMINHO_ARQUIVO = CsvUtil.DIRETORIO + "pedidos.csv";
    private static final String CABECALHO =
        "id;dataPedido;dataEntrega;valorTotal;observacao;canalOrigem;" +
        "statusProducao;clienteId;usuarioId;campanhaId;dataCriacao";

    private List<Pedido> cache;

    public PedidoRepository() {
        this.cache = new ArrayList<>();
        try { this.cache = carregarTodos(); }
        catch (Exception e) { System.err.println("[PedidoRepository] Aviso: " + e.getMessage()); }
    }

    @Override
    public void salvarTodos(List<Pedido> pedidos) {
        List<String> linhas = new ArrayList<>();
        for (Pedido p : pedidos) linhas.add(p.toCsvLine());
        try {
            CsvUtil.escreverLinhas(CAMINHO_ARQUIVO, CABECALHO, linhas);
            this.cache = new ArrayList<>(pedidos);
        } catch (CsvException e) {
            System.err.println("[PedidoRepository] Erro ao salvar: " + e.getMessage());
        }
    }

    @Override
    public List<Pedido> carregarTodos() {
        List<Pedido> pedidos = new ArrayList<>();
        try {
            for (String linha : CsvUtil.lerLinhas(CAMINHO_ARQUIVO))
                pedidos.add(Pedido.fromCsvLine(linha));
        } catch (CsvException e) {
            System.err.println("[PedidoRepository] Erro ao carregar: " + e.getMessage());
        }
        return pedidos;
    }

    @Override
    public String getCaminhoArquivo() { return CAMINHO_ARQUIVO; }

    public Pedido salvar(Pedido pedido) {
        List<String> linhasRaw = new ArrayList<>();
        try { linhasRaw = CsvUtil.lerLinhas(CAMINHO_ARQUIVO); }
        catch (CsvException ignored) {}
        pedido.setId(CsvUtil.proximoId(linhasRaw));
        cache.add(pedido);
        salvarTodos(cache);
        return pedido;
    }

    public Pedido atualizar(Pedido pedidoAtualizado) {
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).getId() == pedidoAtualizado.getId()) {
                cache.set(i, pedidoAtualizado);
                salvarTodos(cache);
                return pedidoAtualizado;
            }
        }
        throw new EntidadeNaoEncontradaException("Pedido", pedidoAtualizado.getId());
    }

    public Optional<Pedido> buscarPorId(int id) {
        return cache.stream().filter(p -> p.getId() == id).findFirst();
    }

    /** Filtra pedidos por status de produção. */
    public List<Pedido> listarPorStatus(StatusProducao status) {
        List<Pedido> resultado = new ArrayList<>();
        for (Pedido p : cache) if (p.getStatusProducao() == status) resultado.add(p);
        return resultado;
    }

    /** Filtra pedidos com retirada em uma data específica (KPI: Retiradas do Dia). */
    public List<Pedido> listarPorDataRetirada(LocalDate data) {
        List<Pedido> resultado = new ArrayList<>();
        for (Pedido p : cache) {
            if (p.getDataEntrega() != null &&
                p.getDataEntrega().toLocalDate().equals(data)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    /** Retorna pedidos com retirada amanhã (alerta de notificação). */
    public List<Pedido> listarRetiradaAmanha() {
        return listarPorDataRetirada(LocalDate.now().plusDays(1));
    }

    /** Retorna pedidos aguardando pagamento há mais de 24h (KPI crítico). */
    public List<Pedido> listarPagamentosPendentesCriticos() {
        List<Pedido> resultado = new ArrayList<>();
        for (Pedido p : cache) if (p.isPagamentoPendenteCritico()) resultado.add(p);
        return resultado;
    }

    /** Filtra pedidos por cliente. */
    public List<Pedido> listarPorCliente(int clienteId) {
        List<Pedido> resultado = new ArrayList<>();
        for (Pedido p : cache) {
            if (p.getCliente() != null && p.getCliente().getId() == clienteId)
                resultado.add(p);
        }
        return resultado;
    }

    /** Filtra pedidos por campanha. */
    public List<Pedido> listarPorCampanha(int campanhaId) {
        List<Pedido> resultado = new ArrayList<>();
        for (Pedido p : cache) if (p.getCampanhaId() == campanhaId) resultado.add(p);
        return resultado;
    }

    public List<Pedido> listarTodos() { return new ArrayList<>(cache); }
}
