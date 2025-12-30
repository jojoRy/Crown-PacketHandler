package kr.crownrpg.packethandler.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class JsonUtils {

    private static final Gson GSON = new Gson();

    private JsonUtils() {}

    public static JsonObject parse(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    public static Gson gson() {
        return GSON;
    }
}
