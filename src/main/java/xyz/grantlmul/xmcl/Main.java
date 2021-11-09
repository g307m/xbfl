package xyz.grantlmul.xmcl;

import xyz.grantlmul.xmcl.backend.Launcher;
import xyz.grantlmul.xmcl.gui.XmclWindow;

import javax.swing.*;

public class Main {
    private static final Launcher launcher = new Launcher();
    public static Launcher getLauncher() {
        return launcher;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            final XmclWindow window = new XmclWindow();
            window.setVisible(true);
        });
    }
}
