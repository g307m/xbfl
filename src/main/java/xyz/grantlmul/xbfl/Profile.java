package xyz.grantlmul.xbfl;

import com.google.gson.JsonObject;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.SingleSelectionModel;
import org.apache.commons.io.FileUtils;
import xyz.grantlmul.xbfl.web.Minecraft;

import java.util.UUID;

public class Profile implements Observable {
    public Profile(Profile profile) {
        loadOldData(profile);
    }

    public ObjectProperty<UUID> uuid = new SimpleObjectProperty<>(UUID.randomUUID());
    public StringProperty name = new SimpleStringProperty("New Profile");
    public BooleanProperty useCustomDirectory = new SimpleBooleanProperty();
    public StringProperty directory = new SimpleStringProperty(FileUtils.getFile(App.dataDir(), "profiles", uuid.toString()).getAbsolutePath());
    public BooleanProperty useCustomResolution = new SimpleBooleanProperty();
    public ObjectProperty<Dimensions> resolution = new SimpleObjectProperty<>(new Dimensions(854, 480));
    public BooleanProperty useCustomVisibility = new SimpleBooleanProperty();
    public BooleanProperty snapshotEnabled = new SimpleBooleanProperty();
    public BooleanProperty betaEnabled = new SimpleBooleanProperty();
    public BooleanProperty alphaEnabled = new SimpleBooleanProperty();
    public BooleanProperty useCustomJava = new SimpleBooleanProperty();
    public StringProperty javaExecutable = new SimpleStringProperty(FileUtils.getFile(System.getProperty("java.home"), "bin", (System.getProperty("os.name").toUpperCase().contains("WIN") ? "java.exe" : "java")).getAbsolutePath());
    public BooleanProperty useCustomArgs = new SimpleBooleanProperty();
    public StringProperty javaArguments = new SimpleStringProperty("-Xmx2G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M");
    public IntegerProperty visibility = new SimpleIntegerProperty();
    public StringProperty gameVersion = new SimpleStringProperty("latest");
    
    public void setName(String name) {
        this.name.set(name);
    }
    public String getName() {
        return this.name.get();
    }
    public void setGameVersion(String version) {
        this.gameVersion.set(version);
    }
    public String getGameVersion() {
        return this.gameVersion.get();
    }

    public Profile() {
    }

    public Profile(UUID uuid) {
        this.uuid.set(uuid);
        directory.set(FileUtils.getFile(App.dataDir(), "profiles", uuid.toString()).getAbsolutePath());
    }

    @Override
    public void addListener(InvalidationListener listener) {

    }

    @Override
    public void removeListener(InvalidationListener listener) {

    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", this.name.getValue());
        if (useCustomDirectory.get())
            json.addProperty("directory", this.directory.getValue());
        if (useCustomResolution.get()) {
            JsonObject dims = new JsonObject();
            dims.addProperty("width", this.resolution.getValue().h.getValue());
            dims.addProperty("height", this.resolution.getValue().v.getValue());
            json.add("resolution", dims);
        }
        if (useCustomVisibility.get())
            json.addProperty("visibility", this.visibility.getValue());
        json.addProperty("snapshots", this.snapshotEnabled.getValue());
        json.addProperty("beta", this.betaEnabled.getValue());
        json.addProperty("alpha", this.alphaEnabled.getValue());
        json.addProperty("version", this.gameVersion.getValue());
        if (useCustomJava.get())
            json.addProperty("javaExecutable", javaExecutable.getValue());
        if (useCustomArgs.get())
            json.addProperty("javaArguments", javaArguments.getValue());
        return json;
    }
    public static Profile fromJson(JsonObject json, UUID uuid) {
        Profile profile = new Profile(uuid);
        profile.name.setValue(json.get("name").getAsString());
        if (json.has("directory"))
            profile.directory.setValue(json.get("directory").getAsString());
        if (json.has("resolution")) {
            JsonObject res = json.getAsJsonObject("resolution");
            profile.resolution.setValue(new Dimensions(res.get("width").getAsInt(), res.get("height").getAsInt()));
        }
        if (json.has("visibility")) {
            profile.visibility.set(json.get("visibility").getAsInt());
            profile.useCustomVisibility.setValue(true);
        }
        profile.snapshotEnabled.setValue(json.get("snapshots").getAsBoolean());
        profile.betaEnabled.setValue(json.get("beta").getAsBoolean());
        profile.alphaEnabled.setValue(json.get("alpha").getAsBoolean());
        profile.gameVersion.setValue(json.get("version").getAsString());
        if (json.has("javaExecutable")) {
            profile.javaExecutable.setValue(json.get("javaExecutable").getAsString());
            profile.useCustomJava.setValue(true);
        }
        if (json.has("javaArguments")) {
            profile.javaArguments.setValue(json.get("javaArguments").getAsString());
            profile.useCustomArgs.setValue(true);
        }

        return profile;
    }

    public void loadOldData(Profile profile) {
        this.uuid.set((profile.uuid.getValue()));
        this.name.set((profile.name.getValue()));
        this.useCustomDirectory.set((profile.useCustomDirectory.getValue()));
        this.directory.set((profile.directory.getValue()));
        this.useCustomResolution.set((profile.useCustomResolution.getValue()));
        this.resolution.set((profile.resolution.getValue()));
        this.useCustomVisibility.set((profile.useCustomVisibility.getValue()));
        this.snapshotEnabled.set((profile.snapshotEnabled.getValue()));
        this.betaEnabled.set((profile.betaEnabled.getValue()));
        this.alphaEnabled.set((profile.alphaEnabled.getValue()));
        this.useCustomJava.set((profile.useCustomJava.getValue()));
        this.javaExecutable.set((profile.javaExecutable.getValue()));
        this.useCustomArgs.set((profile.useCustomArgs.getValue()));
        this.javaArguments.set((profile.javaArguments.getValue()));
        this.visibility.set((profile.visibility.getValue()));
        this.gameVersion.set((profile.gameVersion.getValue()));
    }
}
