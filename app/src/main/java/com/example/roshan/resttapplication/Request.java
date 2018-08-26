package com.example.roshan.resttapplication;

import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

public class Request {

    Request(String request,String source) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("request",request);
        json.put("source",source);
    }
    Request(String request,String source, String dest) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("request",request);
        json.put("source",source);
        json.put("target",dest);
    }

   /* private String jsonToString(JSONObject json){
        return json.toString();
    }*/
}
