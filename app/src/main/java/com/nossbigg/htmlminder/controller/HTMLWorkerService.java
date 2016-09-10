package com.nossbigg.htmlminder.controller;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nossbigg.htmlminder.model.AbstractHTMLWorkerModel;
import com.nossbigg.htmlminder.model.ActivityBagModel;
import com.nossbigg.htmlminder.model.HTMLSubWorkerModel;
import com.nossbigg.htmlminder.model.PlainHTMLWorkerModel;
import com.nossbigg.htmlminder.model.TweetHTMLWorkerModel;
import com.nossbigg.htmlminder.utils.HTMLWorkerModelUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
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
  public String appDirectory = "";
  public ActivityBagModel activityBagModel;

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
    // load reference to activityBagModel
    activityBagModel = (ActivityBagModel) intent.getSerializableExtra("activityBagModel");

    // load appDirectory
    appDirectory = activityBagModel.localFileService.appDir;

    // create directory (if does not exist)
    File appDirectoryFile = new File(appDirectory);
    if (!appDirectoryFile.exists()) {
      try {
        FileUtils.forceMkdir(appDirectoryFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    init();

    return START_STICKY;
  }

  public void init() {
    // Get all worker objects from json files
    List<AbstractHTMLWorkerModel> abstractHTMLWorkerModelList
        = getWorkerModels(appDirectory);

    // start services accordingly
    for (AbstractHTMLWorkerModel abstractHTMLWorkerModel : abstractHTMLWorkerModelList) {
      startWorker(abstractHTMLWorkerModel);
    }

    // make worker json files neat + repair
    HTMLWorkerModelUtils.MakeAllJsonWorkerModelsPretty(
        activityBagModel.localFileService.getWorkerConfigsDir());
  }

  public void startWorker(final AbstractHTMLWorkerModel abstractHTMLWorkerModel) {
    Log.d("UNIQUE_TAG", abstractHTMLWorkerModel.workerName);

    // create worker object, add to hashmap
    switch (abstractHTMLWorkerModel.htmlWorkerType) {
      case PLAIN: {
        HTMLWorkersHashMap.put(
            abstractHTMLWorkerModel.workerName,
            new PlainHTMLWorker((PlainHTMLWorkerModel) abstractHTMLWorkerModel));
        break;
      }
      case TWEET: {
        HTMLWorkersHashMap.put(
            abstractHTMLWorkerModel.workerName,
            new TweetHTMLWorker((TweetHTMLWorkerModel) abstractHTMLWorkerModel));
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

  public List<AbstractHTMLWorkerModel> getWorkerModels(String appDirectory) {
    String workersDirectory = activityBagModel.localFileService.getWorkerConfigsDir();
    ;

    List<AbstractHTMLWorkerModel> workerModels = HTMLWorkerModelUtils.getWorkerModelsFromDirectory(workersDirectory);

    // ADDITIONAL VARIABLES
    // saves data save directories to each worker and subworker
    for (AbstractHTMLWorkerModel abstractHTMLWorkerModel : workerModels) {
      // saves worker data save dir
      abstractHTMLWorkerModel.dataSaveDir =
          activityBagModel.localFileService.getWorkerDataSaveDir(abstractHTMLWorkerModel);

      // save subworkers data save directories
      for (HTMLSubWorkerModel htmlSubWorkerModel : abstractHTMLWorkerModel.subWorkers) {
        htmlSubWorkerModel.dataSaveDir =
            activityBagModel.localFileService.getSubWorkerDataSaveDir(
                abstractHTMLWorkerModel, htmlSubWorkerModel);
      }
    }

    // return list of workers
    return workerModels;
  }

}
