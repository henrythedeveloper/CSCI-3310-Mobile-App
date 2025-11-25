package com.example.csci_3310_nav;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JsonUtils {

    // Default loader for Building Locations
    public static List<CampusLocation> loadLocations(Context context) {
        return loadLocations(context, "campus_locations.json");
    }

    // Overloaded loader for specific files (like Accessibility)
    public static List<CampusLocation> loadLocations(Context context, String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        Gson gson = new Gson();
        Type listType = new TypeToken<List<CampusLocation>>() {}.getType();
        return gson.fromJson(json, listType);
    }
}