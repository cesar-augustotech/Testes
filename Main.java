package stardew;

import stardew.engine.GameEngine;
import stardew.persistence.PersistenciaCSV;

/**
 * Ponto de entrada — composição root.
 *
 * A única classe que conhece tanto GameEngine quanto PersistenciaCSV.
 * Aplica injeção de dependência: cria o Adapter concreto (PersistenciaCSV)
 * e o injeta no GameEngine via RepositorioJogo.
 *
 * Para usar outro formato de save (ex: JSON), só aqui muda:
 *   new GameEngine(new PersistenciaJSON())
 */
public class Main {
    public static void main(String[] args) {
        new GameEngine(new PersistenciaCSV()).iniciar();
    }
}
