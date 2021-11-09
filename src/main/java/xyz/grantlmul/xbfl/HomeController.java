package xyz.grantlmul.xbfl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;
import xyz.grantlmul.xbfl.web.Minecraft;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.OptionalInt;
import java.util.ResourceBundle;
import java.util.UUID;

public class HomeController implements Initializable {
    @FXML Button playButton;
    @FXML ComboBox<Profile> profileList;
    @FXML Text usernameText;
    @FXML TextArea logOutput;
    @FXML TabPane windowTabs;
    @FXML TableView<Profile> profileTable;

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

    private final Callback<ListView<Profile>, ListCell<Profile>> cellFactory = new Callback<>() {
        @Override
        public ListCell<Profile> call(ListView<Profile> param) {
            return new ListCell<>() {
                {
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                }

                @Override
                protected void updateItem(Profile item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!(empty || item == null))
                        textProperty().bind(item.name);
                }
            };
        }
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(int b) {
                Platform.runLater(() -> logOutput.appendText(String.valueOf((char) b)));
            }
        };
        System.setOut(new PrintStream(outputStream, true));
        System.out.println("Redirecting output to application.");
        this.usernameText.setText(Minecraft.userData.get("name").getAsString());
        profileList.itemsProperty().bind(App.profileManager.profileList);
        profileList.setCellFactory(cellFactory);
        profileList.setButtonCell(cellFactory.call(null));
        profileList.getSelectionModel().select(0);
        profileTable.itemsProperty().set(App.profileManager.profileList.getValue());
    }

    public void handleEditClick(MouseEvent mouseEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("profile.fxml"));
        Parent parent = loader.load();
        ProfileController controller = loader.getController();
        controller.loadProfile(profileList.getValue());
        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setTitle("Profile Editor");
        stage.getIcons().clear();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/favicon.png")));
        stage.show();
    }

    public void handleNewClick(MouseEvent mouseEvent) throws IOException {
        Profile profile = App.profileManager.addProfile(new Profile());
        profileList.getSelectionModel().select(profile);
        handleEditClick(mouseEvent);
    }
}
