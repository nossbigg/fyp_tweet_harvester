package com.example.gibson.androidhtmlscheduler.controller;

import android.util.Log;

import com.example.gibson.androidhtmlscheduler.model.AbstractHTMLWorker;
import com.example.gibson.androidhtmlscheduler.model.HTMLSubWorker;
import com.example.gibson.androidhtmlscheduler.model.HTMLWorkerJsonUtils;
import com.example.gibson.androidhtmlscheduler.model.PlainHTMLWorker;
import com.example.gibson.androidhtmlscheduler.model.TweetHTMLWorker;
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
  public final String ROOT_DIR = HTMLWorkerService.ROOT_DIR;

  public void generateSampleWorkers() {
    // Create Plain HTML Worker
    PlainHTMLWorker plainHTMLWorker = new PlainHTMLWorker("Plain HTML Worker Sample");
    plainHTMLWorker.subWorkers.add(new HTMLSubWorker("Subworker 1", "www.example.com", "GET", 0, 0));
    plainHTMLWorker.subWorkers.add(new HTMLSubWorker("Subworker 2", "www.example.com", "GET", 0, 0));
    HTMLSubWorker htmlSubWorker = new HTMLSubWorker("Subworker 3", "www.example.com", "GET", 0, 0);
    htmlSubWorker.parameters.put("q", "katy perry");
    plainHTMLWorker.subWorkers.add(htmlSubWorker);

    // Create Plain HTML Worker
    PlainHTMLWorker plainHTMLWorker2 = new PlainHTMLWorker("Plain HTML Worker Sample (Batch)");
    plainHTMLWorker2.isBatch = true;
    plainHTMLWorker2.subWorkers.add(new HTMLSubWorker("Subworker 1", "www.example.com", "GET", 0, 0));
    plainHTMLWorker2.subWorkers.add(new HTMLSubWorker("Subworker 2", "www.example.com", "GET", 0, 0));

    // Create Tweet HTML Worker
    TweetHTMLWorker tweetHTMLWorker = new TweetHTMLWorker("CONSUMER_KEY", "CONSUMER_SECRET", "Tweet HTML Worker Sample");
    plainHTMLWorker.subWorkers.add(new HTMLSubWorker("Subworker 1", "www.google.com", "GET", 0, 0));
    plainHTMLWorker.subWorkers.add(new HTMLSubWorker("Subworker 2", "www.google.com", "GET", 0, 0));

    // Save all HTML workers to file
    List<AbstractHTMLWorker> abstractHTMLWorkerList = new ArrayList<>();
    abstractHTMLWorkerList.add(plainHTMLWorker);
    abstractHTMLWorkerList.add(plainHTMLWorker2);
    abstractHTMLWorkerList.add(tweetHTMLWorker);

    // gson object
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    for (AbstractHTMLWorker abstractHTMLWorker : abstractHTMLWorkerList) {
      // serialize worker
      String json = HTMLWorkerJsonUtils.getHTMLWorkerSerialized(abstractHTMLWorker);

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
    List<AbstractHTMLWorker> abstractHTMLWorkerList = new ArrayList<>();
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
      AbstractHTMLWorker abstractHTMLWorker =
          HTMLWorkerJsonUtils.getHTMLWorkerDeserialized(json);

      // skips if parsing failed
      if (abstractHTMLWorker == null) continue;

      // adds worker to list
      abstractHTMLWorkerList.add(abstractHTMLWorker);
    }

    // iterate through all workers
    for (AbstractHTMLWorker abstractHTMLWorker : abstractHTMLWorkerList) {
      Log.d("UNIQUE_TAG", abstractHTMLWorker.workerName);
      if (abstractHTMLWorker instanceof TweetHTMLWorker) {
        TweetHTMLWorker tweetHTMLWorker = (TweetHTMLWorker) abstractHTMLWorker;
        Log.d("UNIQUE_TAG", tweetHTMLWorker.CONSUMER_KEY);
      }
    }
  }
}
