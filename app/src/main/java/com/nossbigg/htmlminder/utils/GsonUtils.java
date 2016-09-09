package com.nossbigg.htmlminder.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Gibson on 9/9/2016.
 */
public class GsonUtils {
  // Gson with normal output
  public static Gson gsonStandard = new GsonBuilder().create();

  // Gson with prettyOutput
  public static Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
}
