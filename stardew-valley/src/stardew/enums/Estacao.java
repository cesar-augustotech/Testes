package stardew.enums;

import java.util.List;

/**
 * Enum representando as quatro estações do jogo.
 * Cada estação define quais culturas podem ser plantadas.
 */
public enum Estacao {

    PRIMAVERA("Primavera") {
        @Override
        public List<String> getCulturas() {
            return List.of("Morango", "Couve-Flor", "Batata", "Tulipa");
        }
    },
    VERAO("Verão") {
        @Override
        public List<String> getCulturas() {
            return List.of("Mirtilo", "Melão", "Girassol", "Tomate");
        }
    },
    OUTONO("Outono") {
        @Override
        public List<String> getCulturas() {
            return List.of("Abóbora", "Cranberry", "Inhame", "Aster");
        }
    },
    INVERNO("Inverno") {
        @Override
        public List<String> getCulturas() {
            return List.of(); // sem culturas no inverno
        }
    };

    private final String nome;

    Estacao(String nome) {
        this.nome = nome;
    }

    /** Retorna as culturas disponíveis nesta estação. */
    public abstract List<String> getCulturas();

    public boolean suportaCultura(String cultura) {
        return getCulturas().stream()
                .anyMatch(c -> c.equalsIgnoreCase(cultura));
    }

    @Override
    public String toString() {
        return nome;
    }
}
