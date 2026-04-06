package stardew;

import stardew.engine.GameGUI;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameGUI janelaJogo = new GameGUI();
            janelaJogo.setVisible(true);
        });
    }
}