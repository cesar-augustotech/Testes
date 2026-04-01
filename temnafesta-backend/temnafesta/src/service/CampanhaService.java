package service;

import model.Campanha;
import model.Cardapio;
import repository.CampanhaRepository;
import exception.ValidacaoException;
import exception.EntidadeNaoEncontradaException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * SERVICE — CampanhaService
 *
 * POO aplicado:
 * - Encapsulamento: regras de negócio de campanha isoladas aqui
 * - Polimorfismo de método: abrirCampanha sobrecarregado (com e sem cardápio)
 * - Exceções personalizadas: ValidacaoException para dados inválidos
 *
 * Regras de negócio:
 * - Só pode haver UMA campanha ativa por vez
 * - Encerramento pode ser manual ou automático (limite atingido)
 * - Link público pertence à campanha
 */
public class CampanhaService {

    private final CampanhaRepository campanhaRepository;

    public CampanhaService(CampanhaRepository campanhaRepository) {
        this.campanhaRepository = campanhaRepository;
    }

    /**
     * Abre uma nova campanha.
     * Valida que não há outra campanha ativa no momento.
     * POLIMORFISMO DE MÉTODO — sobrecarga: com e sem cardápio inicial
     */
    public Campanha abrirCampanha(String nome, LocalDate dataInicio,
                                   LocalDate dataFim, int limitePedidos) {
        // Regra: apenas uma campanha ativa por vez
        Optional<Campanha> ativa = campanhaRepository.buscarAtiva();
        if (ativa.isPresent()) {
            throw new ValidacaoException("campanha",
                "Já existe uma campanha ativa: '" + ativa.get().getNome() +
                "'. Encerre-a antes de abrir uma nova.");
        }

        Campanha campanha = new Campanha(nome, dataInicio, dataFim, limitePedidos);
        campanha.validar(); // Lança ValidacaoException se inválida
        return campanhaRepository.salvar(campanha);
    }

    /** Abre campanha já com um cardápio vinculado. */
    public Campanha abrirCampanha(String nome, LocalDate dataInicio,
                                   LocalDate dataFim, int limitePedidos, Cardapio cardapio) {
        Campanha campanha = abrirCampanha(nome, dataInicio, dataFim, limitePedidos);
        campanha.setCardapio(cardapio);
        return campanhaRepository.atualizar(campanha);
    }

    /** Encerra uma campanha manualmente. */
    public Campanha encerrarCampanha(int campanhaId) {
        Campanha campanha = campanhaRepository.buscarPorId(campanhaId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Campanha", campanhaId));

        if (!campanha.isAtiva()) {
            throw new ValidacaoException("campanha",
                "A campanha '" + campanha.getNome() + "' já está encerrada.");
        }

        campanha.encerrar();
        return campanhaRepository.atualizar(campanha);
    }

    /** Retorna a campanha ativa atual. */
    public Optional<Campanha> buscarAtiva() {
        return campanhaRepository.buscarAtiva();
    }

    /** Retorna histórico de campanhas encerradas. */
    public List<Campanha> listarEncerradas() {
        return campanhaRepository.listarEncerradas();
    }

    public List<Campanha> listarTodas() {
        return campanhaRepository.listarTodos();
    }

    /**
     * Retorna o link público copiável da campanha ativa.
     * Lança exceção se não houver campanha ativa.
     */
    public String getLinkPublicoAtivo() {
        return campanhaRepository.buscarAtiva()
            .map(Campanha::getLinkPublico)
            .orElseThrow(() -> new ValidacaoException("campanha",
                "Não há campanha ativa no momento."));
    }
}
