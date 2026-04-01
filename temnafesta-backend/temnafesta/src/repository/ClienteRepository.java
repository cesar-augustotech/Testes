package repository;

import interfaces.Persistivel;
import model.Cliente;
import exception.CsvException;
import exception.EntidadeNaoEncontradaException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY — ClienteRepository
 *
 * POO aplicado:
 * - Interface: implementa Persistivel<Cliente>
 * - Encapsulamento: toda lógica de I/O isolada aqui
 * - Polimorfismo de classe: ClienteRepository é um Persistivel
 * - Persistência: usa CsvUtil (FileReader + BufferedReader internamente)
 *
 * Responsável por salvar e carregar clientes do arquivo CSV.
 * O Endereço do cliente NÃO é persistido aqui (escopo simplificado).
 */
public class ClienteRepository implements Persistivel<Cliente> {

    private static final String CAMINHO_ARQUIVO = CsvUtil.DIRETORIO + "clientes.csv";
    private static final String CABECALHO =
        "id;nome;telefone;whatsapp;instagram;dataCadastro;anotacoes;dataCriacao;enderecoId";

    // Lista em memória — carregada uma vez e mantida sincronizada
    private List<Cliente> cache;

    public ClienteRepository() {
        this.cache = new ArrayList<>();
        try {
            this.cache = carregarTodos();
        } catch (Exception e) {
            System.err.println("[ClienteRepository] Aviso: " + e.getMessage());
        }
    }

    // --- Implementação da interface Persistivel ---

    @Override
    public void salvarTodos(List<Cliente> clientes) {
        List<String> linhas = new ArrayList<>();
        for (Cliente c : clientes) {
            linhas.add(c.toCsvLine());
        }
        try {
            CsvUtil.escreverLinhas(CAMINHO_ARQUIVO, CABECALHO, linhas);
            this.cache = new ArrayList<>(clientes); // Sincroniza cache
        } catch (CsvException e) {
            System.err.println("[ClienteRepository] Erro ao salvar: " + e.getMessage());
        }
    }

    @Override
    public List<Cliente> carregarTodos() {
        List<Cliente> clientes = new ArrayList<>();
        try {
            List<String> linhas = CsvUtil.lerLinhas(CAMINHO_ARQUIVO);
            for (String linha : linhas) {
                clientes.add(Cliente.fromCsvLine(linha));
            }
        } catch (CsvException e) {
            System.err.println("[ClienteRepository] Erro ao carregar: " + e.getMessage());
        }
        return clientes;
    }

    @Override
    public String getCaminhoArquivo() {
        return CAMINHO_ARQUIVO;
    }

    // --- Operações CRUD ---

    /**
     * Salva um novo cliente, gerando id automático.
     * Persiste no CSV imediatamente.
     */
    public Cliente salvar(Cliente cliente) {
        List<String> linhasRaw = new ArrayList<>();
        try { linhasRaw = CsvUtil.lerLinhas(CAMINHO_ARQUIVO); }
        catch (CsvException ignored) {}

        int novoId = CsvUtil.proximoId(linhasRaw);
        cliente.setId(novoId);
        cache.add(cliente);
        salvarTodos(cache);
        return cliente;
    }

    /**
     * Atualiza um cliente existente pelo id.
     * Lança EntidadeNaoEncontradaException se não encontrar.
     */
    public Cliente atualizar(Cliente clienteAtualizado) {
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).getId() == clienteAtualizado.getId()) {
                cache.set(i, clienteAtualizado);
                salvarTodos(cache);
                return clienteAtualizado;
            }
        }
        throw new EntidadeNaoEncontradaException("Cliente", clienteAtualizado.getId());
    }

    /** Busca cliente por id. Retorna Optional para tratamento seguro. */
    public Optional<Cliente> buscarPorId(int id) {
        return cache.stream().filter(c -> c.getId() == id).findFirst();
    }

    /** Busca clientes pelo nome (busca parcial, case-insensitive). */
    public List<Cliente> buscarPorNome(String nome) {
        List<Cliente> resultado = new ArrayList<>();
        for (Cliente c : cache) {
            if (c.getNome().toLowerCase().contains(nome.toLowerCase())) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    /** Remove cliente pelo id. */
    public void remover(int id) {
        boolean removido = cache.removeIf(c -> c.getId() == id);
        if (!removido) throw new EntidadeNaoEncontradaException("Cliente", id);
        salvarTodos(cache);
    }

    /** Retorna todos os clientes em memória. */
    public List<Cliente> listarTodos() {
        return new ArrayList<>(cache);
    }
}
