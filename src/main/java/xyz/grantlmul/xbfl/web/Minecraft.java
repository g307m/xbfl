package xyz.grantlmul.xbfl.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import xyz.grantlmul.xbfl.App;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Minecraft {
    public static JsonObject userData;

    public static JsonObject refreshUserData() {
        try {
            File accdat = new File(App.dataDir(), "accountdata.json");
            if (!accdat.exists())
                return null;
            String accdatstr = null;
            accdatstr = FileUtils.readFileToString(accdat, StandardCharsets.UTF_8);
            JsonObject accdato = JsonParser.parseString(accdatstr).getAsJsonObject();
            userData = accdato.deepCopy();
            String access_token = accdato.get("access_token").getAsString();
            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.minecraftservices.com/minecraft/profile").openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + access_token);
            String responseBody = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
            JsonObject nude = JsonParser.parseString(responseBody).getAsJsonObject();
            if (nude.has("error"))
                return null;
            else {
                nude.addProperty("access_token", access_token);
                return nude;
            }
        } catch (IOException e) {
            return null;
        }
    }

    static JsonObject versionManifest;

    public static void refreshVersionManifest() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet("https://launchermeta.mojang.com/mc/game/version_manifest.json");
        CloseableHttpResponse response = client.execute(request);
        String responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        versionManifest = JsonParser.parseString(responseBody).getAsJsonObject();
        versions.clear();
        versions.add(l);
        versionManifest.getAsJsonArray("versions").forEach(jsonElement -> versions.get().add(jsonElement.getAsJsonObject()));
    }

    private static final JsonObject l = new JsonObject();
    static {
        l.addProperty("id", "latest");
        l.addProperty("type", "latest");
    }

    public static JsonObject getVersionManifest() {
        return versionManifest;
    }

    private static ListProperty<JsonObject> versions = new SimpleListProperty<>(new ModifiableObservableListBase<>() {
        private ArrayList<JsonObject> internal = new ArrayList<>();
        @Override
        public int size() {
            return internal.size();
        }

        @Override
        protected void doAdd(int index, JsonObject element) {
            internal.add(index, element);
        }

        @Override
        protected JsonObject doSet(int index, JsonObject element) {
            return internal.set(index, element);
        }

        @Override
        protected JsonObject doRemove(int index) {
            return internal.remove(index);
        }

        @Override
        public JsonObject get(int index) {
            return internal.get(index);
        }
    });
    public static ListProperty<JsonObject> getVersions() {
        return versions;
    }
}
