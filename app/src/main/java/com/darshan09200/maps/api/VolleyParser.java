package com.darshan09200.maps.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class VolleyParser {

    public HashMap<String, String> parseDistance(JSONObject jsonObject) {
        JSONArray jsonArray = null;
        try {
            jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getDuration(jsonArray);
    }

    private HashMap<String, String> getDuration(JSONArray jsonArray) {
        HashMap<String, String> distanceDurationDict = new HashMap<>();
        String distance = "";
        String duration = "";

        try {
            duration = jsonArray.getJSONObject(0).getJSONObject("duration").getString("text");
            distance = jsonArray.getJSONObject(0).getJSONObject("distance").getString("text");

            distanceDurationDict.put("duration", duration);
            distanceDurationDict.put("distance", distance);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return distanceDurationDict;
    }

}
