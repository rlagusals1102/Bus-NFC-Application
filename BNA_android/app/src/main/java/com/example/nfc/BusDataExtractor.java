package com.example.nfc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BusDataExtractor {
    public static List<String> extractRoutesForStation(String dataFile, String stationName) throws IOException, JSONException {

        String jsonData = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            jsonData = new String(Files.readAllBytes(Paths.get(dataFile)));
        }
        JSONArray busData = new JSONArray(jsonData);

        List<String> routes = new ArrayList<>();
        for (int i = 0; i < busData.length(); i++) {
            JSONObject route = busData.getJSONObject(i);
            if (route.getString("정류소명").equals(stationName)) {
                routes.add(route.getString("노선명"));
            }
        }

        return routes;
    }


}