package xyz.grantlmul.xbfl;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import xyz.grantlmul.xbfl.web.Minecraft;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {
    @FXML CheckBox useCustomResolution;
    @FXML CheckBox useCustomDirectory;
    @FXML CheckBox useCustomJava;
    @FXML CheckBox useCustomArgs;
    @FXML CheckBox useCustomVisibility;
    @FXML CheckBox enableAlpha;
    @FXML CheckBox enableBeta;
    @FXML CheckBox enableSnapshots;
    @FXML ComboBox<JsonObject> gameVersion;
    @FXML ComboBox<Integer> launchBehavior;
    @FXML TextField javaArguments;
    @FXML TextField directory;
    @FXML TextField gameWidth;
    @FXML TextField javaExecutable;
    @FXML TextField name;
    @FXML TextField hameHeight;
    @FXML AnchorPane rootPane;

    private Profile profile;
    private Profile oldProfile;

    public void loadProfile(Profile profile) {
        this.profile = profile;
        oldProfile = new Profile(profile);
        name.textProperty().bindBidirectional(this.profile.name);
        directory.textProperty().bindBidirectional(this.profile.directory);
        gameWidth.textProperty().bindBidirectional(this.profile.resolution.get().h);
        hameHeight.textProperty().bindBidirectional(this.profile.resolution.get().v);
        launchBehavior.getSelectionModel().select(this.profile.visibility.get());
        enableSnapshots.selectedProperty().bindBidirectional(this.profile.snapshotEnabled);
        enableBeta.selectedProperty().bindBidirectional(this.profile.betaEnabled);
        enableAlpha.selectedProperty().bindBidirectional(this.profile.alphaEnabled);
        javaExecutable.textProperty().bindBidirectional(this.profile.javaExecutable);
        javaArguments.textProperty().bindBidirectional(this.profile.javaArguments);
        useCustomJava.selectedProperty().bindBidirectional(this.profile.useCustomJava);
        useCustomResolution.selectedProperty().bindBidirectional(this.profile.useCustomResolution);
        useCustomVisibility.selectedProperty().bindBidirectional(this.profile.useCustomVisibility);
        useCustomDirectory.selectedProperty().bindBidirectional(this.profile.useCustomDirectory);
        useCustomArgs.selectedProperty().bindBidirectional(this.profile.useCustomArgs);
        refreshVersions();
        for (JsonObject version : gameVersion.getItems()) {
            if (Objects.equals(version.get("id").getAsString(), this.profile.gameVersion.getValue())) {
                this.gameVersion.getSelectionModel().select(version);
                break;
            }
        }
    }

    @FXML
    private void refreshVersions() {
        this.gameVersion.setItems(new FilteredList<>(Minecraft.getVersions(), s -> switch (s.get("type").getAsString()) {
            case "snapshot" -> this.profile.snapshotEnabled.get();
            case "release" -> true;
            case "old_beta" -> this.profile.betaEnabled.get();
            case "old_alpha" -> this.profile.alphaEnabled.get();
            default -> true;
        }));
    }

    private static final Callback<ListView<JsonObject>, ListCell<JsonObject>> cellFactory = new Callback<>() {
        @Override
        public ListCell<JsonObject> call(ListView<JsonObject> param) {
            return new ListCell<>() {
                {
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                }

                @Override
                protected void updateItem(JsonObject item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty)
                        setText("");
                    else {
                        if (Objects.equals(item.get("id").getAsString(), "latest"))
                            setText("Use Latest Version");
                        else
                            setText(item.get("id").getAsString());
                    }
                }
            };
        }
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        launchBehavior.getSelectionModel().selectFirst();
        gameWidth.disableProperty().bind(useCustomResolution.selectedProperty().not());
        hameHeight.disableProperty().bind(useCustomResolution.selectedProperty().not());
        directory.disableProperty().bind(useCustomDirectory.selectedProperty().not());
        launchBehavior.disableProperty().bind(useCustomVisibility.selectedProperty().not());
        javaArguments.disableProperty().bind(useCustomArgs.selectedProperty().not());
        javaExecutable.disableProperty().bind(useCustomJava.selectedProperty().not());
        gameVersion.setButtonCell(cellFactory.call(null));
        gameVersion.setCellFactory(cellFactory);
        Platform.runLater(() -> rootPane.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::cancelEdits));
    }

    private void cancelEdits(Event event) {
        this.profile.loadOldData(this.oldProfile);
    }

    @FXML void cancel(MouseEvent mouseEvent) {
        this.cancelEdits(mouseEvent);
        ((Stage)((Button)mouseEvent.getSource()).getScene().getWindow()).close();
    }

    @FXML void openDir() throws IOException {
        File dir = new File(directory.getText());
        if (!dir.exists())
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Invalid game directory!");
                alert.show();
            });
        else
            Desktop.getDesktop().open(dir);
    }

    @FXML void save(MouseEvent mouseEvent) throws IOException {
        if (gameVersion.getSelectionModel().isEmpty()) {
            this.profile.snapshotEnabled.setValue(oldProfile.snapshotEnabled.getValue());
            this.profile.betaEnabled.setValue(oldProfile.betaEnabled.getValue());
            this.profile.alphaEnabled.setValue(oldProfile.alphaEnabled.getValue());
            this.profile.gameVersion.setValue(oldProfile.gameVersion.getValue());
        }
        this.profile.gameVersion.setValue(this.gameVersion.getValue().get("id").getAsString());
        App.profileManager.saveProfiles();
        ((Stage)((Button)mouseEvent.getSource()).getScene().getWindow()).close();
    }
}
