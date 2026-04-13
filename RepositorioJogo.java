package stardew.interfaces;

import stardew.model.Fazendeiro;
import stardew.model.GerenciadorTempo;
import stardew.model.Inventario;
import stardew.model.Fazenda;

import java.io.IOException;

/**
 * Adapter Pattern — abstração da camada de persistência.
 *
 * GameEngine depende desta interface, nunca da implementação concreta.
 * Isso aplica o Dependency Inversion Principle: módulos de alto nível
 * (GameEngine) não dependem de módulos de baixo nível (PersistenciaCSV).
 *
 * Implementações possíveis:
 *   - PersistenciaCSV  → salva em arquivos .csv (implementação atual)
 *   - RepositorioEmMemoria → para testes, sem I/O
 */
public interface RepositorioJogo {

    /**
     * Persiste o estado completo do jogo.
     * Deve ser atômico: ou tudo é salvo, ou nada é corrompido.
     */
    void salvar(Fazendeiro fazendeiro, GerenciadorTempo tempo) throws IOException;

    /** Carrega o fazendeiro usando o inventário e a fazenda já carregados. */
    Fazendeiro carregarFazendeiro(Inventario inventario, Fazenda fazenda) throws IOException;

    Fazenda     carregarFazenda()    throws IOException;
    Inventario  carregarInventario() throws IOException;
    GerenciadorTempo carregarTempo() throws IOException;

    /** Verifica se existe um save válido antes de tentar carregar. */
    boolean saveExiste();
}
