package xyz.grantlmul.xmcl.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class XmclWindow extends JFrame {
    JPanel cards;

    public XmclWindow() {
        super("XMC Launcher " + XmclWindow.class.getPackage().getImplementationVersion());

        // avoid something breaking
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                XmclWindow.this.setVisible(false);
                XmclWindow.this.dispose();
            }
        });

        cards = new JPanel(new CardLayout());
        cards.add(new HomePanel());
        cards.add(new LoginPanel());

        add(cards, BorderLayout.CENTER);
    }
}
