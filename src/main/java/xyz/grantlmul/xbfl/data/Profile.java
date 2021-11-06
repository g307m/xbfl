package xyz.grantlmul.xbfl.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public record Profile(String name,
                      Path directory,
                      OptionalInt width,
                      OptionalInt height,
                      int visibilitySetting,
                      boolean[] versionSelect,
                      String version,
                      @Nullable String customJavaPath,
                      @Nullable String customJvmArgs) {
    public static Profile fromJson(JsonObject source) {
        OptionalInt w = source.has("width") ? OptionalInt.of(source.get("width").getAsInt()): OptionalInt.empty();
        OptionalInt h = source.has("height") ? OptionalInt.of(source.get("height").getAsInt()): OptionalInt.empty();
        JsonArray s = source.get("versionSelection").getAsJsonArray();
        boolean vs[] = new boolean[3];
        for (int i = 0; i < 3; i++) {
            vs[i] = s.get(i).getAsBoolean();
        }
        String jp = source.has("javaPath") ? source.get("javaPath").getAsString() : null;
        String ja = source.has("javaArgs") ? source.get("javaArgs").getAsString() : null;
        return new Profile(
                source.get("name").getAsString(),
                Path.of(source.get("dir").getAsString()),
                w,
                h,
                source.get("visibility").getAsInt(),
                vs,
                source.get("version").getAsString(),
                jp,
                ja
        );
    }
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("dir", directory.toString());
        json.addProperty("width", width.orElse(854));
        json.addProperty("height", width.orElse(480));
        json.addProperty("visibility", visibilitySetting);
        JsonArray vs = new JsonArray();
        vs.add(versionSelect[0]);
        vs.add(versionSelect[1]);
        vs.add(versionSelect[2]);
        json.add("versionSelection", vs);
        json.addProperty("version", version);
        if (customJavaPath != null)
            json.addProperty("javaPath", customJavaPath);
        if (customJvmArgs != null)
            json.addProperty("javaArgs", customJvmArgs);
        return json;
    }
}
