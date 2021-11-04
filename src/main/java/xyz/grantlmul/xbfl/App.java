package xyz.grantlmul.xbfl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import xyz.grantlmul.xbfl.auth.Minecraft;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

/**
 * The 'real' main class called by {@link Launcher}
 */
public class App extends Application {

    public static final String REDIRECT_URI = "http://localhost:54321/auth";
    public static final String REGISTER_URL = "https://sisu.xboxlive.com/connect/XboxLive/?state=signup&signup=1&cobrandId=8058f65d-ce06-4c30-9559-473c9275a65d&&tid=896928775&ru=https://www.minecraft.net/en-us/login?return_url=/en-us/profile&aid=1142970254";
    public static URL OAUTH_URL;

    private static Stage mainStage;
    public static Stage getMainStage() {
        return mainStage;
    }
    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        try {
            Parent parent = null;
            JsonObject userData = Minecraft.refreshUserData();
            if (userData == null)
                parent = FXMLLoader.load(getClass().getResource("login.fxml"));
            else
                parent = FXMLLoader.load(getClass().getResource("home.fxml"));
            Scene scene = new Scene(parent, 884, 541);
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("styles.css").toString());
            primaryStage.setTitle("XBF Launcher " + getClass().getPackage().getImplementationVersion());
            primaryStage.getIcons().clear();
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/favicon.png")));
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("fat L");
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.exit(0);
    }

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
    public static void main(String[] args) throws IOException {
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
        launch(args);
    }
}