package com.darshan09200.maps.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;

public class VolleySingleton {
    private static VolleySingleton instance;
    private static Context ctx;
    private RequestQueue requestQueue;

    private VolleySingleton(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
        }
        return instance;
    }

    public static HashMap<String, String> getDirection(JSONObject jsonObject) {
        HashMap<String, String> distances = null;
        VolleyParser directionParser = new VolleyParser();
        distances = directionParser.parseDistance(jsonObject);

        return distances;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}