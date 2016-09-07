package com.example.gibson.androidhtmlscheduler.controller;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.gibson.androidhtmlscheduler.model.AbstractHTMLWorkerModel;
import com.example.gibson.androidhtmlscheduler.utils.HTMLWorkerModelUtils;
import com.example.gibson.androidhtmlscheduler.model.PlainHTMLWorkerModel;
import com.example.gibson.androidhtmlscheduler.model.TweetHTMLWorkerModel;
import com.example.gibson.androidhtmlscheduler.utils.HTMLWorkerUtils;
import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Gibson on 8/31/2016.
 */
public class HTMLWorkerService extends Service {
  // Constants
  private PowerManager.WakeLock wakeLock;

  // Variables
  public HashMap<String, AbstractHTMLWorker> HTMLWorkersHashMap = new HashMap<>();
  public String appDirectory;

  @Override
  public void onCreate() {
    super.onCreate();

    // set wakelock
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNjfdhotDimScreen");
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startid) {
    // set root directory
    this.appDirectory = intent.getStringExtra("appDirectory");

    // create directory (if does not exist)
    File appDirectoryFile = new File(appDirectory);
    if(!appDirectoryFile.exists()){
      try {
        FileUtils.forceMkdir(appDirectoryFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    init();

    return 0;
  }

  public void init() {
    // Get all config files
    List<AbstractHTMLWorkerModel> abstractHTMLWorkerModelList = getWorkerModels(appDirectory);

    // start services accordingly
    for (AbstractHTMLWorkerModel abstractHTMLWorkerModel : abstractHTMLWorkerModelList) {
      startWorker(abstractHTMLWorkerModel);
    }

    // make worker json files neat
    HTMLWorkerModelUtils.MakeAllJsonWorkerModelsPretty(appDirectory);
  }

  public List<AbstractHTMLWorkerModel> getWorkerModels(String rootDirectory) {
    // match only .json files
    List<String> listFileMatcher = new ArrayList<>();
    listFileMatcher.add("json");

    // get paths of all workers
    List<File> files = (List<File>) FileUtils.listFiles(
        new File(rootDirectory), listFileMatcher.toArray(new String[0]), true
    );

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

      // adds worker to list
      htmlWorkerModelArrayList.add(abstractHTMLWorkerModel);
    }

    // return list of workers
    return htmlWorkerModelArrayList;
  }

  public void startWorker(final AbstractHTMLWorkerModel abstractHTMLWorkerModel) {
    Log.d("UNIQUE_TAG", abstractHTMLWorkerModel.workerName);

    // create worker object, add to hashmap
    switch (abstractHTMLWorkerModel.htmlWorkerType) {
      case PLAIN: {
        HTMLWorkersHashMap.put(
            abstractHTMLWorkerModel.workerName,
            new PlainHTMLWorker((PlainHTMLWorkerModel) abstractHTMLWorkerModel,
                appDirectory));
        break;
      }
      case TWEET: {
        HTMLWorkersHashMap.put(
            abstractHTMLWorkerModel.workerName,
            new TweetHTMLWorker((TweetHTMLWorkerModel) abstractHTMLWorkerModel,
                appDirectory));
        break;
      }
      default: {
        // unknown type
        return;
      }
    }

    // start worker
    AbstractHTMLWorker abstractHTMLWorker = HTMLWorkersHashMap.get(abstractHTMLWorkerModel.workerName);
    abstractHTMLWorker.startWorker();
  }

  /**
   * Returns the appropriate storage directory path
   *
   * @return
   */

}
