package com.nossbigg.htmlminder.controller;

import android.os.Handler;
import android.util.Log;

import com.nossbigg.htmlminder.model.AbstractHTMLWorkerModel;
import com.nossbigg.htmlminder.model.HTMLSubWorkerModel;
import com.nossbigg.htmlminder.utils.FileUtilsCustom;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Manages local storage structure
 * <p>
 * Created by Gibson on 9/8/2016.
 */
public class LocalFileService implements Serializable {
  // FOLDER STRUCTURE
  // app init dir: may or may not be where actual files are stored
  // ..appInitDir
  public final String appInitDir;
  // ..appInitDir/app_init_config.json
  public static String APP_INIT_CONFIG_JSON = "app_init_config.json";

  // app dir: where actual files and app config is stored
  // ..appDir
  public final String appDir;
  // ..appDir/data
  private static final String DATA_SAVE_DIR = "data";
  // ..appDir/data/<workername>/last_max_tweet.json
  private static final String LAST_MAX_TWEET_JSON = "last_max_tweet.json";
  // ..appDir/config
  private static final String CONFIG_DIR = "config";
  // ..appDir/config/app_config.json
  private static final String APP_CONFIG_JSON = "app_config.json";
  // ..appDir/config/workers
  private static final String WORKER_CONFIG_DIR = CONFIG_DIR + "/workers";

  // compress service variables
  boolean compressSvcStarted = false;
  transient Handler compressSvcHandler = new Handler();
  transient Runnable compressSvcRunnable = null;

  public LocalFileService(ConfigService configService) {
    // update app init dir and app dir
    appInitDir = configService.appInitConfig.HERE_DIR;
    appDir = configService.appConfig.HERE_DIR;

    // create folder structure
    createAppFolderStructure(appDir);

    // start compress service
    startCompressSvc();
  }

  public void startCompressSvc() {
    // don't start service if already started
    if (compressSvcStarted) return;

    // init runnable if not initialized
    if (compressSvcRunnable == null) {
      compressSvcRunnable = initCompressSvcRunnable(compressSvcHandler);
    }

    // run service
    compressSvcHandler.post(compressSvcRunnable);
  }

  public void stopCompressSvc() {
    compressSvcHandler.removeCallbacks(compressSvcRunnable);
    compressSvcStarted = false;
  }

  public Runnable initCompressSvcRunnable(final Handler handler) {
    return new Runnable() {
      @Override
      public void run() {
        Log.d("UNIQUE_TAG", "Running Compress Service..." + new Date().toString());

        // match only .text files
        List<String> listFileMatcher = new ArrayList<>();
        listFileMatcher.add("text");

        // get paths of all .text files
        HashSet<File> files = new HashSet<>();
        files.addAll(FileUtils.listFiles(new File(getDataSaveDir())
            , listFileMatcher.toArray(new String[0])
            , true
        ));

        // compresses files
        Date now = new Date();
        for (File f : files) {
          // FIXME compress files that were last modified yesterday or later
          // if file was not modified today
          if (!checkDateDMYSame(now, new Date(f.lastModified()))) {
            String filePath = f.getAbsolutePath();
            String compressedFilePath = filePath + ".gz";

            // skip files that have already been compressed
            // prevents overwrite
            if (new File(compressedFilePath).exists()) continue;

            // compress file
            FileUtilsCustom.makeGzipFromFile(compressedFilePath, filePath);

            // delete source file
            try {
              FileUtils.forceDelete(f);
            } catch (IOException e) {
            }
          }
        }

        // repeat task at around 12:01
        Calendar toStartCal = Calendar.getInstance();
        toStartCal.setTime(new Date());
        toStartCal.add(Calendar.DATE, 1);
        toStartCal.set(Calendar.HOUR_OF_DAY, 0);
        toStartCal.set(Calendar.MINUTE, 1);
        toStartCal.set(Calendar.SECOND, 0);
        toStartCal.set(Calendar.MILLISECOND, 0);
        // calculate timediff
        long interval = toStartCal.getTime().getTime() - new Date().getTime();
        // set time to run
        handler.postDelayed(this, interval);
      }
    };
  }

  private boolean checkDateDMYSame(Date a, Date b) {
    Calendar aCal = Calendar.getInstance();
    aCal.setTime(a);
    Calendar bCal = Calendar.getInstance();
    bCal.setTime(b);

    return aCal.get(Calendar.DATE) == bCal.get(Calendar.DATE)
        && aCal.get(Calendar.MONTH) == bCal.get(Calendar.MONTH)
        && aCal.get(Calendar.YEAR) == bCal.get(Calendar.YEAR);
  }

  /**
   * Creates app folder structure
   */
  public void createAppFolderStructure(String appDir) {
    List<String> foldersToCreate = new ArrayList<>();
    foldersToCreate.add("/data");
    foldersToCreate.add("/config");
    foldersToCreate.add("/config/workers");

    for (String folder : foldersToCreate) {
      try {
        FileUtils.forceMkdir(new File(appDir + folder));
      } catch (IOException e) {
        // failed to make directory
      }
    }
  }

  // PATH BUILDING METHODS
  // Static methods (used pre-initialization of this class)
  public static String getAppInitConfigPathFromAppInitDir(String appInitDir) {
    return appInitDir + "/" + APP_INIT_CONFIG_JSON;
  }

  public static String getAppConfigPathFromAppDir(String appDir) {
    return appDir + "/" + CONFIG_DIR + "/" + APP_CONFIG_JSON;
  }

  public static String getLastMaxTweetJsonPathFromWorkerDataSaveDir(String workerDataSaveDir) {
    return workerDataSaveDir + "/" + LAST_MAX_TWEET_JSON;
  }

  // Non-static methods
  public String getWorkerConfigsDir() {
    return appDir + "/" + WORKER_CONFIG_DIR;
  }

  public String getDataSaveDir() {
    return appDir + "/" + DATA_SAVE_DIR;
  }

  public String getWorkerDataSaveDir(AbstractHTMLWorkerModel abstractHTMLWorkerModel) {
    return getDataSaveDir() + "/" + abstractHTMLWorkerModel.workerName;
  }

  public String getSubWorkerDataSaveDir(
      AbstractHTMLWorkerModel abstractHTMLWorkerModel, HTMLSubWorkerModel htmlSubWorkerModel) {
    return getWorkerDataSaveDir(abstractHTMLWorkerModel)
        + "/" + htmlSubWorkerModel.subWorkerName;
  }
}
