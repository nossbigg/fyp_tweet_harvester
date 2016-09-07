package com.nossbigg.htmlminder.utils;

import android.util.Log;

import com.nossbigg.htmlminder.model.AbstractHTMLWorkerModel;
import com.nossbigg.htmlminder.model.HTMLSubWorkerModel;
import com.nossbigg.htmlminder.utils.HTMLWorkerModelUtils;
import com.nossbigg.htmlminder.model.PlainHTMLWorkerModel;
import com.nossbigg.htmlminder.model.TweetHTMLWorkerModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gibson on 8/29/2016.
 */
public class HTMLWorkerSampleGenerator {
  // FIXME
  public final String ROOT_DIR = "";

  public void generateSampleWorkers() {
    // Create Plain HTML Worker
    PlainHTMLWorkerModel plainHTMLWorker = new PlainHTMLWorkerModel("Plain HTML Worker Sample");
    plainHTMLWorker.subWorkers.add(new HTMLSubWorkerModel("Subworker 1", "www.example.com", "GET", 0, 0));
    plainHTMLWorker.subWorkers.add(new HTMLSubWorkerModel("Subworker 2", "www.example.com", "GET", 0, 0));
    HTMLSubWorkerModel htmlSubWorker = new HTMLSubWorkerModel("Subworker 3", "www.example.com", "GET", 0, 0);
    htmlSubWorker.parameters.put("q", "katy perry");
    plainHTMLWorker.subWorkers.add(htmlSubWorker);

    // Create Plain HTML Worker
    PlainHTMLWorkerModel plainHTMLWorker2 = new PlainHTMLWorkerModel("Plain HTML Worker Sample (Batch)");
    plainHTMLWorker2.isBatch = true;
    plainHTMLWorker2.subWorkers.add(new HTMLSubWorkerModel("Subworker 1", "www.example.com", "GET", 0, 0));
    plainHTMLWorker2.subWorkers.add(new HTMLSubWorkerModel("Subworker 2", "www.example.com", "GET", 0, 0));

    // Create Tweet HTML Worker
    TweetHTMLWorkerModel tweetHTMLWorker = new TweetHTMLWorkerModel("CONSUMER_KEY", "CONSUMER_SECRET", "Tweet HTML Worker Sample");
    plainHTMLWorker.subWorkers.add(new HTMLSubWorkerModel("Subworker 1", "www.google.com", "GET", 0, 0));
    plainHTMLWorker.subWorkers.add(new HTMLSubWorkerModel("Subworker 2", "www.google.com", "GET", 0, 0));

    // Save all HTML workers to file
    List<AbstractHTMLWorkerModel> abstractHTMLWorkerList = new ArrayList<>();
    abstractHTMLWorkerList.add(plainHTMLWorker);
    abstractHTMLWorkerList.add(plainHTMLWorker2);
    abstractHTMLWorkerList.add(tweetHTMLWorker);

    // gson object
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    for (AbstractHTMLWorkerModel abstractHTMLWorker : abstractHTMLWorkerList) {
      // serialize worker
      String json = HTMLWorkerModelUtils.HTMLWorkerModelToJson(abstractHTMLWorker);

      // write to file
      File file = new File(ROOT_DIR + "/" + abstractHTMLWorker.workerName + ".json");
      try {
        FileUtils.writeStringToFile(file, json);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void readWorkers() {
    // match only .json files
    List<String> listFileMatcher = new ArrayList<>();
    listFileMatcher.add("json");

    // get paths of all workers
    List<File> files = (List<File>) FileUtils.listFiles(
        new File(ROOT_DIR), listFileMatcher.toArray(new String[0]), true
    );

    // import worker jsons
    List<AbstractHTMLWorkerModel> abstractHTMLWorkerList = new ArrayList<>();
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
      AbstractHTMLWorkerModel abstractHTMLWorker =
          HTMLWorkerModelUtils.JsonToHTMLWorkerModel(json);

      // skips if parsing failed
      if (abstractHTMLWorker == null) continue;

      // adds worker to list
      abstractHTMLWorkerList.add(abstractHTMLWorker);
    }

    // iterate through all workers
    for (AbstractHTMLWorkerModel abstractHTMLWorker : abstractHTMLWorkerList) {
      Log.d("UNIQUE_TAG", abstractHTMLWorker.workerName);
      if (abstractHTMLWorker instanceof TweetHTMLWorkerModel) {
        TweetHTMLWorkerModel tweetHTMLWorker = (TweetHTMLWorkerModel) abstractHTMLWorker;
        Log.d("UNIQUE_TAG", tweetHTMLWorker.CONSUMER_KEY);
      }
    }
  }
}
