package stardew.enums;

import java.util.List;

/**
 * Enum representando as localizações do mapa.
 * Cada local define quais ações o jogador pode executar.
 */
public enum Localizacao {

    FAZENDA("Fazenda") {
        @Override
        public List<String> getAcoesDisponiveis() {
            return List.of("plantar", "colher", "irrigar", "cuidar_animais",
                           "coletar_recurso", "dormir");
        }
    },
    CIDADE("Cidade") {
        @Override
        public List<String> getAcoesDisponiveis() {
            return List.of("conversar_npc", "comprar", "vender", "coletar_recurso");
        }
    },
    PRAIA("Praia") {
        @Override
        public List<String> getAcoesDisponiveis() {
            return List.of("pescar", "conversar_npc", "coletar_recurso");
        }
    },
    MINAS("Minas") {
        @Override
        public List<String> getAcoesDisponiveis() {
            return List.of("minerar", "lutar", "coletar_recurso");
        }
    },
    FLORESTA("Floresta") {
        @Override
        public List<String> getAcoesDisponiveis() {
            return List.of("cortar_arvore", "coletar_recurso");
        }
    };

    private final String nome;

    Localizacao(String nome) {
        this.nome = nome;
    }

    /** Retorna as ações disponíveis nesta localização. */
    public abstract List<String> getAcoesDisponiveis();

    /** Verifica se uma ação específica é permitida aqui. */
    public boolean permiteAcao(String acao) {
        return getAcoesDisponiveis().contains(acao);
    }

    @Override
    public String toString() {
        return nome;
    }
}
