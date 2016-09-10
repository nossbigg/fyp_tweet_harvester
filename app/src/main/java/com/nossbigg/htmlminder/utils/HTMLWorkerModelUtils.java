package com.nossbigg.htmlminder.utils;

import com.google.gson.Gson;
import com.nossbigg.htmlminder.model.AbstractHTMLWorkerModel;
import com.nossbigg.htmlminder.model.HTMLSubWorkerModel;
import com.nossbigg.htmlminder.model.HTMLWorkerType;
import com.nossbigg.htmlminder.model.PlainHTMLWorkerModel;
import com.nossbigg.htmlminder.model.TweetHTMLWorkerModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gibson on 9/6/2016.
 */
public class HTMLWorkerModelUtils {
  public static List<AbstractHTMLWorkerModel> getWorkerModelsFromDirectory(String workersDirectory) {
    // match only .json files
    List<String> listFileMatcher = new ArrayList<>();
    listFileMatcher.add("json");

    // get paths of all workers
    List<File> files = new ArrayList<>();
    files.addAll(FileUtils.listFiles(new File(workersDirectory)
        , listFileMatcher.toArray(new String[0])
        , true
    ));

    // import worker jsons
    List<AbstractHTMLWorkerModel> htmlWorkerModelArrayList = new ArrayList<>();
    Gson gson = new Gson();
    for (File f : files) {
      // get file content
      String json = "";
      try {
        json = FileUtils.readFileToString(f);
      } catch (IOException e) {
        e.printStackTrace();
        continue;
      }

      // parse json to object
      AbstractHTMLWorkerModel abstractHTMLWorkerModel =
          HTMLWorkerModelUtils.JsonToHTMLWorkerModel(json);

      // skips if parsing failed
      if (abstractHTMLWorkerModel == null) continue;

      // ADDITIONAL VARIABLES
      // store json config file path
      abstractHTMLWorkerModel.jsonConfigFilePath = f.getAbsolutePath();

      // adds worker to list
      htmlWorkerModelArrayList.add(abstractHTMLWorkerModel);
    }

    // return list of workers
    return htmlWorkerModelArrayList;
  }

  public static String HTMLWorkerModelToJson(AbstractHTMLWorkerModel abstractHTMLWorker) {
    return GsonUtils.gsonPretty.toJson(abstractHTMLWorker, abstractHTMLWorker.getClass());
  }

  public static AbstractHTMLWorkerModel JsonToHTMLWorkerModel(String json, boolean isFillEmptyFields) {
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
    AbstractHTMLWorkerModel abstractHTMLWorkerModel;
    switch (htmlWorkerTypeString) {
      case "PLAIN":
        abstractHTMLWorkerModel = gson.fromJson(json, PlainHTMLWorkerModel.class);
        break;
      case "TWEET":
        abstractHTMLWorkerModel = gson.fromJson(json, TweetHTMLWorkerModel.class);
        break;
      default:
        // invalid state, no worker type defined
        return null;
    }

    // return if no need to fill empty fields
    if (!isFillEmptyFields) return abstractHTMLWorkerModel;

    // find empty fields and fill them with defaults (from constructor)
    switch (htmlWorkerTypeString) {
      case "PLAIN":
        abstractHTMLWorkerModel = new PlainHTMLWorkerModel((PlainHTMLWorkerModel) abstractHTMLWorkerModel);
        break;
      case "TWEET":
        abstractHTMLWorkerModel = new TweetHTMLWorkerModel((TweetHTMLWorkerModel) abstractHTMLWorkerModel);
        break;
    }

    // fill in single subworker if no subworkers
    if (abstractHTMLWorkerModel.subWorkers.isEmpty()) {
      HTMLSubWorkerModel htmlSubWorkerModel = new HTMLSubWorkerModel("", "", "", 0, 0);
      // fill in parameter
      htmlSubWorkerModel.parameters.put("", "");
      abstractHTMLWorkerModel.subWorkers.add(htmlSubWorkerModel);
    }

    return abstractHTMLWorkerModel;
  }

  public static AbstractHTMLWorkerModel JsonToHTMLWorkerModel(String json) {
    return JsonToHTMLWorkerModel(json, false);
  }

  public static void MakeAllJsonWorkerModelsPretty(String rootDirectory) {
    // match only .json files
    List<String> listFileMatcher = new ArrayList<>();
    listFileMatcher.add("json");

    // get paths of all workers
    List<File> files = (List<File>) FileUtils.listFiles(
        new File(rootDirectory), listFileMatcher.toArray(new String[0]), true
    );

    // check worker json files
    for (File f : files) {
      // make pretty
      MakeJsonWorkerModelPretty(f);
    }
  }

  public static void MakeJsonWorkerModelPretty(File f) {
    // get file content
    String json = "";
    try {
      json = FileUtils.readFileToString(f);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    // parse json to object (fill empty fields)
    AbstractHTMLWorkerModel abstractHTMLWorkerModel =
        HTMLWorkerModelUtils.JsonToHTMLWorkerModel(json, true);

    // if not equal, rewrite to file with pretty output
    String prettyJson = HTMLWorkerModelToJson(abstractHTMLWorkerModel);
    if (!StringUtils.equals(json, prettyJson)) {
      try {
        FileUtilsCustom.saveToFile(f.getAbsolutePath(), prettyJson, false);
      } catch (IOException e) {
        // skip writing of file
      }
    }
  }
}
