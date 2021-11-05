package xyz.grantlmul.xbfl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import xyz.grantlmul.xbfl.data.Profile;
import xyz.grantlmul.xbfl.web.Minecraft;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class ProfileController implements Initializable {
    @FXML CheckBox customDimensions;
    @FXML CheckBox customDirectory;
    @FXML CheckBox customJava;
    @FXML CheckBox customJvmArgs;
    @FXML CheckBox customLaunch;
    @FXML CheckBox enableAlpha;
    @FXML CheckBox enableBeta;
    @FXML CheckBox enableSnapshots;
    @FXML ComboBox<JsonObject> gameVersion;
    @FXML ComboBox<String> launchBehavior;
    @FXML TextField args;
    @FXML TextField directory;
    @FXML TextField hres;
    @FXML TextField jvm;
    @FXML TextField name;
    @FXML TextField vres;

    private JsonObject latestRelease = new JsonObject();
    private JsonObject latestSnapshot = new JsonObject();

    private UUID uuid;

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void loadProfile(Profile profile) {
        launchBehavior.getSelectionModel().select(profile.visibilitySetting());
        hres.setText(String.valueOf(profile.width().orElse(854)));
        vres.setText(String.valueOf(profile.height().orElse(480)));
        jvm.setText(profile.customJavaPath());
        args.setText(profile.customJvmArgs());
        name.setText(profile.name());
        directory.setText(profile.directory().toString());
        JsonObject o = new JsonObject();
        o.addProperty("id", profile.version());
        gameVersion.getSelectionModel().select(o);
        updateVersions();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        launchBehavior.getSelectionModel().selectFirst();
        hres.disableProperty().bind(customDimensions.selectedProperty().not());
        vres.disableProperty().bind(customDimensions.selectedProperty().not());
        directory.disableProperty().bind(customDirectory.selectedProperty().not());
        launchBehavior.disableProperty().bind(customLaunch.selectedProperty().not());
        args.disableProperty().bind(customJvmArgs.selectedProperty().not());
        jvm.disableProperty().bind(customJava.selectedProperty().not());

        gameVersion.setButtonCell(new ListCell<>() {
            private JsonObject it;
            {
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
            @Override
            protected void updateItem(JsonObject item, boolean empty) {
                super.updateItem(item, empty);
                it = item;
                if (item == null || empty)
                    setText("err");
                else {
                    if (it.get("id").getAsString() == "latest")
                        setText("Use Latest Version");
                    else
                        setText(it.get("id").getAsString());
                }
            }
        });
        gameVersion.setCellFactory(param -> new ListCell<>() {
            private JsonObject it;
            {
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
            @Override
            protected void updateItem(JsonObject item, boolean empty) {
                super.updateItem(item, empty);
                it = item;
                if (item == null || empty)
                    setText("err");
                else {
                    if (it.get("id").getAsString() == "latest")
                        setText("Use Latest Version");
                    else
                        setText(it.get("id").getAsString());
                }
            }
        });

        JsonObject manifest = null;
        try {
            manifest = Minecraft.getVersionManifest();
        } catch (IOException e) {
            e.printStackTrace();
        }
        versions = manifest.getAsJsonArray("versions");
    }

    private JsonArray versions;

    public void updateVersions() {
        gameVersion.getItems().clear();
        JsonObject l = new JsonObject();
        l.addProperty("id", "latest");
        gameVersion.getItems().add(l);
        for (JsonElement e : versions) {
            JsonObject object = e.getAsJsonObject();
            boolean yes = switch (object.get("type").getAsString()) {
                case "snapshot" -> enableSnapshots.isSelected();
                case "release" -> true;
                case "old_beta" -> enableBeta.isSelected();
                case "old_alpha" -> enableAlpha.isSelected();
            };
            if (yes)
                gameVersion.getItems().add(object);
        }
    }

    @FXML void cancel(MouseEvent mouseEvent) {
        Stage stage = (Stage) ((Button)mouseEvent.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML void openDir(MouseEvent mouseEvent) throws IOException {
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
        Profile profile = new Profile(
                name.getText(),
                Path.of(directory.getText()),
                hres.getText().isEmpty() ? OptionalInt.empty() : OptionalInt.of(Integer.parseInt(hres.getText())),
                vres.getText().isEmpty() ? OptionalInt.empty() : OptionalInt.of(Integer.parseInt(vres.getText())),
                launchBehavior.getSelectionModel().getSelectedIndex(),
                new boolean[]{enableSnapshots.isSelected(), enableBeta.isSelected(), enableAlpha.isSelected()},
                gameVersion.getSelectionModel().getSelectedItem().get("id").getAsString(),
                jvm.getText().isEmpty() ? null : jvm.getText(),
                args.getText().isEmpty() ? null : jvm.getText()
        );
        File profilesDir = FileUtils.getFile(App.dataDir(), "profiles");
        File profilesFile = FileUtils.getFile(profilesDir, "profiles.json");
        File profileDir = FileUtils.getFile(profilesDir, uuid.toString());
        File profileConfig = FileUtils.getFile(profileDir, "profile.json");
        if (!profileDir.isDirectory())
            profileDir.mkdir();
        profileConfig.delete();
        FileUtils.writeStringToFile(profileConfig, profile.toJson().toString(), StandardCharsets.UTF_8);
    }
}
