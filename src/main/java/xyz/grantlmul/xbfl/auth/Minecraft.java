package xyz.grantlmul.xbfl.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import xyz.grantlmul.xbfl.App;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Minecraft {
    public static JsonObject userData;
    public static JsonObject refreshUserData() throws IOException {
        File accdat = new File(App.dataDir(), "accountdata.json");
        if (!accdat.exists())
            return null;
        String accdatstr = FileUtils.readFileToString(accdat, StandardCharsets.UTF_8);
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
    }
}
