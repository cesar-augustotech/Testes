package service;

import model.Cliente;
import model.Endereco;
import repository.ClienteRepository;
import exception.EntidadeNaoEncontradaException;
import java.util.List;
import java.util.Optional;

/**
 * SERVICE — ClienteService
 *
 * POO aplicado:
 * - Encapsulamento: validação e persistência isoladas
 * - Relacionamento: orquestra Cliente + Endereco (composição)
 * - Polimorfismo de método: cadastrar com e sem endereço (sobrecarga)
 */
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * Cadastra um novo cliente com endereço.
     * POLIMORFISMO DE MÉTODO — sobrecarga (overloading)
     */
    public Cliente cadastrar(String nome, String telefone, String whatsapp,
                              String instagram, String anotacoes, Endereco endereco) {
        Cliente cliente = new Cliente(nome, telefone, whatsapp, instagram, anotacoes, endereco);
        cliente.validar(); // Lança ValidacaoException se inválido
        return clienteRepository.salvar(cliente);
    }

    /** Cadastra cliente sem endereço (mínimo viável). */
    public Cliente cadastrar(String nome, String whatsapp) {
        return cadastrar(nome, null, whatsapp, null, null, null);
    }

    public Cliente atualizar(Cliente cliente) {
        cliente.validar();
        return clienteRepository.atualizar(cliente);
    }

    public Optional<Cliente> buscarPorId(int id) {
        return clienteRepository.buscarPorId(id);
    }

    public List<Cliente> buscarPorNome(String nome) {
        return clienteRepository.buscarPorNome(nome);
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.listarTodos();
    }

    public void remover(int id) {
        clienteRepository.remover(id);
    }
}
