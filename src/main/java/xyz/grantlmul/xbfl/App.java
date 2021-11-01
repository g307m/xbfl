package xyz.grantlmul.xbfl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

// 884, 541
public class App {

    public static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "XBFL Error", JOptionPane.ERROR_MESSAGE);
    }
    public static void showInfo(String message) {
        JOptionPane.showMessageDialog(null, message, "XBFL Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static final String REDIRECT_URI = "http%3A%2F%2Flocalhost%3A54321%2Fauth";
    public static final String REGISTER_URL = "https://sisu.xboxlive.com/connect/XboxLive/?state=signup&signup=1&cobrandId=8058f65d-ce06-4c30-9559-473c9275a65d&&tid=896928775&ru=https://www.minecraft.net/en-us/login?return_url=/en-us/profile&aid=1142970254";
    public static URL OAUTH_URL;

    public static File dataDir() {
        String workingDirectory;
        String osName = (System.getProperty("os.name")).toUpperCase();
        if (osName.contains("WIN"))
        {
            workingDirectory = System.getenv("AppData");
        }
        else
        {
            workingDirectory = System.getProperty("user.home");
            workingDirectory += "/Library/Application Support";
        }
        File dataDir = Path.of(workingDirectory, "XBF Launcher Data").toFile();
        if (!dataDir.isDirectory()) {
            dataDir.delete();
            dataDir.mkdir();
        }
        return dataDir;
    }

    public static Thread thread;
    public static JsonObject authConf;
    public static void main(String[] args) throws Exception {
        thread = Thread.currentThread();
        authConf = JsonParser.parseString(
                        new String(
                                App.class.getResourceAsStream("/authconf.json").readAllBytes()
                        ))
                .getAsJsonObject();
        StringBuilder bob = new StringBuilder("https://login.live.com/oauth20_authorize.srf?client_id=");
        bob.append(authConf.get("client_id").getAsString());
        bob.append("&response_type=code&redirect_uri=");
        bob.append(REDIRECT_URI);
        bob.append("&scope=XboxLive.signin%20offline_access");
        OAUTH_URL = new URL(bob.toString());
        System.out.println("Course set to " + OAUTH_URL);
        mainFrame = new JFrame();
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setSize(884, 541);
    mainFrame.add(
        new BackgroundPanel("/dirt.png", new GridLayout()));
        mainFrame.setVisible(true);
    }
    public static JFrame mainFrame;
}