package com.example.gibson.androidhtmlscheduler.controller;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.gibson.androidhtmlscheduler.model.AbstractHTMLWorker;
import com.example.gibson.androidhtmlscheduler.model.HTMLWorkerJsonUtils;
import com.example.gibson.androidhtmlscheduler.model.HTMLWorkerType;
import com.example.gibson.androidhtmlscheduler.model.TweetHTMLWorker;
import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gibson on 8/31/2016.
 */
public class HTMLWorkerService extends Service {
  // Constants
  public static final String ROOT_DIR = initializeRootDirectoryVariable();

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startid) {
    init();

    return 0;
  }

  public void init() {
    // Get all config files
    List<AbstractHTMLWorker> abstractHTMLWorkerList = getWorkers(ROOT_DIR);

    // start services accordingly
    for (AbstractHTMLWorker abstractHTMLWorker : abstractHTMLWorkerList) {
      startWorker(abstractHTMLWorker);
    }
  }

  public List<AbstractHTMLWorker> getWorkers(String rootDirectory) {
    // match only .json files
    List<String> listFileMatcher = new ArrayList<>();
    listFileMatcher.add("json");

    // get paths of all workers
    List<File> files = (List<File>) FileUtils.listFiles(
        new File(rootDirectory), listFileMatcher.toArray(new String[0]), true
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

    // return list of workers
    return abstractHTMLWorkerList;
  }

  //TODO implement isBatch functionality
  public void startWorker(final AbstractHTMLWorker abstractHTMLWorker) {
    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {

        if (abstractHTMLWorker.htmlWorkerType == HTMLWorkerType.PLAIN) {

        } else if (abstractHTMLWorker.htmlWorkerType == HTMLWorkerType.TWEET) {

        }


        return null;
      }
    }.execute();
  }

  public void startSubWorker() {
    //pull data from endpoint

  }

  private Runnable subWorkerRunnable = new Runnable(){
    @Override
    public void run() {
      // do request


      // save
    }
  };

  /**
   * Returns the appropriate storage directory path
   *
   * @return
   */
  private static String initializeRootDirectoryVariable() {
    return Environment.getExternalStorageDirectory().getAbsolutePath()
        + "/HTMLMinder/";
  }
}
