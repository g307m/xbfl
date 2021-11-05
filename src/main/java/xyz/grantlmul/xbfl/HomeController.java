package xyz.grantlmul.xbfl;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xyz.grantlmul.xbfl.data.Profile;
import xyz.grantlmul.xbfl.web.Minecraft;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {
    @FXML ComboBox<Profile> profileList;
    @FXML Text usernameText;
    @FXML TextArea logOutput;
    @FXML TabPane windowTabs;

    public void handleLogOutClick(MouseEvent mouseEvent) throws IOException {
        File dataFile = new File(App.dataDir(), "accountdata.json");
        dataFile.delete();
        Minecraft.userData = null;
        Parent parent = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(parent, 884, 541);
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("styles.css").toString());
        App.getMainStage().setScene(scene);
    }

    public void handlePlayClick(MouseEvent mouseEvent) {
        System.out.println("Starting game!");
        windowTabs.getSelectionModel().select(1);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                Platform.runLater(() -> {
                    logOutput.appendText(String.valueOf((char) b));
                });
            }
        };
        System.setOut(new PrintStream(outputStream, true));
        System.out.println("Redirecting output to application.");
        this.usernameText.setText(Minecraft.userData.get("name").getAsString());
    }

    public void handleEditClick(MouseEvent mouseEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("profile.fxml"));
        Parent parent = loader.load();
        ProfileController controller = loader.getController();
        controller.loadProfile();
        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setTitle("Profile Editor");
        stage.getIcons().clear();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/favicon.png")));
        stage.show();
    }
}
