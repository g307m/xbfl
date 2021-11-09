package xyz.grantlmul.xbfl;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.UUID;

public class ProfileManager implements Observable {
    private final LinkedList<Profile> profiles = new LinkedList<>();

    public ProfileManager() throws IOException {
        if (!FileUtils.getFile(App.dataDir(), "profiles").isDirectory()) {
            Profile defaultProfile = new Profile();
            defaultProfile.uuid.setValue(UUID.randomUUID());
            defaultProfile.name.set("default");
            defaultProfile.directory.setValue(getProfilePath(defaultProfile.uuid.getValue()));
            defaultProfile.resolution.get().h.setValue(String.valueOf(854));
            defaultProfile.resolution.get().v.setValue(String.valueOf(480));
            defaultProfile.visibility.set(0);
            defaultProfile.snapshotEnabled.setValue(false);
            defaultProfile.betaEnabled.setValue(false);
            defaultProfile.alphaEnabled.setValue(false);
            addProfile(defaultProfile);
            saveProfiles();
        } else {
            JsonArray profileIds = JsonParser.parseString(FileUtils.readFileToString(FileUtils.getFile(App.dataDir(), "profiles.json"), StandardCharsets.UTF_8)).getAsJsonArray();
            profileIds.forEach(profileId -> {
                try {
                    addProfile(Profile.fromJson(JsonParser.parseString(FileUtils.readFileToString(FileUtils.getFile(App.dataDir(), "profiles", profileId.getAsString(), "profile.json"), StandardCharsets.UTF_8)).getAsJsonObject(), UUID.fromString(profileId.getAsString())));
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                }
            });
        }
    }

    void saveProfiles() throws IOException {
        File profilesFile = FileUtils.getFile(App.dataDir(), "profiles.json");
        File profilesDir = FileUtils.getFile(App.dataDir(), "profiles");
        if (!profilesFile.isFile() || !profilesDir.isDirectory()) {
            profilesFile.delete();
            profilesDir.delete();
            profilesFile.createNewFile();
            profilesDir.mkdir();
        }

        JsonArray profilesJson = new JsonArray();
        for (Profile profile : profiles) {
            profilesJson.add(profile.uuid.getValue().toString());
            File profileDir = FileUtils.getFile(profilesDir, profile.uuid.getValue().toString());
            FileUtils.writeStringToFile(FileUtils.getFile(profileDir, "profile.json"), profile.toJson().toString(), StandardCharsets.UTF_8);
        }
        FileUtils.writeStringToFile(profilesFile, profilesJson.toString(), StandardCharsets.UTF_8);
    }

    public Profile getProfile(UUID uuid) {
        for (Profile profile : profiles) {
            if (profile.uuid.getValue().equals(uuid))
                return profile;
        }
        return null;
    }

    public Profile addProfile(Profile profile) {
        if (profiles.add(profile)) {
            listeners.forEach(a -> a.profileAdded(profile));
            profileList.getValue().add(profile);
        }
        return profile;
    }

    public boolean removeProfile(Profile profile) {
        boolean res = profiles.remove(profile);
        if (res) {
            listeners.forEach(a -> a.profileRemoved(profile));
            profileList.getValue().remove(profile);
        }
        return res;
    }

    private final LinkedList<ProfileManagerUpdateListener> listeners = new LinkedList<>();
    public final ObservableValue<ObservableList<Profile>> profileList = new SimpleObjectProperty<>(FXCollections.observableArrayList());

    public void addListener(ProfileManagerUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ProfileManagerUpdateListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void addListener(InvalidationListener listener) { }

    @Override
    public void removeListener(InvalidationListener listener) { }

    public String getProfilePath(UUID value) {
        return FileUtils.getFile(App.dataDir(), "profiles", value.toString()).getAbsolutePath();
    }

    public ObservableValue<ObservableList<Profile>> getProfiles() {
        return profileList;
    }
}
