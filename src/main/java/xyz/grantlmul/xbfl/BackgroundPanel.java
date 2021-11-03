package xyz.grantlmul.xbfl;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;

public class BackgroundPanel extends JPanel implements ImageObserver {
    public BackgroundPanel(String imageResourcePath) throws IOException {
        super();
        setBackground(imageResourcePath);
    }

    public BackgroundPanel(String imageResourcePath, LayoutManager layoutManager) throws IOException {
        this(imageResourcePath);
        setLayout(layoutManager);
    }

    private BufferedImage background;

    public void setBackground(String path) throws IOException {
        background = ImageIO.read(getClass().getResourceAsStream(path));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(new TexturePaint(background, g.getClipBounds()));
        g.drawImage(background, 0,0,this);
    }
}
