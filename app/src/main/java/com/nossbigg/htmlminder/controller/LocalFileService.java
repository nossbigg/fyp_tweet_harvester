package com.nossbigg.htmlminder.controller;

import com.nossbigg.htmlminder.model.AbstractHTMLWorkerModel;
import com.nossbigg.htmlminder.model.HTMLSubWorkerModel;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages local storage structure
 * <p/>
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

  public LocalFileService(ConfigService configService) {
    // update app init dir and app dir
    appInitDir = configService.appInitConfig.HERE_DIR;
    appDir = configService.appConfig.HERE_DIR;

    // create folder structure
    createAppFolderStructure(appDir);
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

  // COMPRESSION TASK
  // TODO compress files that are older than set threshold
  public void compressFilesOlderThanThreshold(String rootDir, List<String> matchPatterns, long thresholdMs) {

  }
}
