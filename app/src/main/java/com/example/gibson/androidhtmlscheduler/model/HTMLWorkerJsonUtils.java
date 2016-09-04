package com.example.gibson.androidhtmlscheduler.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.EnumUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Gibson on 8/30/2016.
 */
public class HTMLWorkerJsonUtils {
  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public static String getHTMLWorkerSerialized(AbstractHTMLWorker abstractHTMLWorker){
    return gson.toJson(abstractHTMLWorker, abstractHTMLWorker.getClass());
  }

  public static AbstractHTMLWorker getHTMLWorkerDeserialized(String json){
    // get object type + check if valid json
    JSONObject jo;
    String htmlWorkerTypeString = "";
    Boolean isValidEnum = false;
    try {
      jo = new JSONObject(json);
      htmlWorkerTypeString = jo.getString("htmlWorkerType");
      htmlWorkerTypeString = htmlWorkerTypeString.toUpperCase();
      isValidEnum = EnumUtils.isValidEnum(HTMLWorkerType.class, htmlWorkerTypeString);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    // skips if no valid class found
    if (!isValidEnum) return null;

    // parse json to object
    htmlWorkerTypeString = htmlWorkerTypeString.toUpperCase();
    Gson gson = new Gson();
    switch (htmlWorkerTypeString) {
      case "PLAIN":
        return gson.fromJson(json, PlainHTMLWorker.class);
      case "TWEET":
        return gson.fromJson(json, TweetHTMLWorker.class);
      default:
        return null;
    }
  }
}
