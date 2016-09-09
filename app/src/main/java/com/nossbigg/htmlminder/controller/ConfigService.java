package com.nossbigg.htmlminder.controller;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.google.gson.JsonSyntaxException;
import com.nossbigg.htmlminder.model.AppConfigModel;
import com.nossbigg.htmlminder.model.AppInitConfigModel;
import com.nossbigg.htmlminder.utils.FileUtilsCustom;
import com.nossbigg.htmlminder.utils.GsonUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages configuration files
 * <p/>
 * Created by Gibson on 9/6/2016.
 */
public class ConfigService implements Serializable{
  // Stores other additional app config
  public AppConfigModel appConfig;
  // Stores initial app config (eg. where is the
  public AppInitConfigModel appInitConfig;

  // stores copy of all possible directories avaliable to app
  HashSet<String> allPossibleDirs = new HashSet<>();

  /**
   * Sets appConfig and appInitConfig
   *
   * @throws IOException
   */
  public ConfigService(Context appContext) throws IOException {
    // get all possible directories
    allPossibleDirs = getAllPossibleAppDirs(appContext);

    // get app init dir
    String appInitDir = getBestAppDirectory(allPossibleDirs,
        APP_DIRECTORY_PREFERENCE.INT_SD, APP_DIRECTORY_PREFERENCE.EXT_SD,
        APP_DIRECTORY_PREFERENCE.OTHERS);

    // app init config
    appInitConfig = initAppInitConfig(appInitDir);

    // init config
    appConfig = initAppConfig(appInitConfig.APP_DIR);
  }

  private AppInitConfigModel initAppInitConfig(String appInitDir) throws IOException {
    // load initial config file exists
    String appInitConfigPath =
        LocalFileService.getAppInitConfigPathFromAppInitDir(appInitDir);
    File appInitConfig = new File(appInitConfigPath);
    if (appInitConfig.exists()) {
      try {
        // successful loading returns object
        String json = FileUtils.readFileToString(appInitConfig);
        AppInitConfigModel appInitConfigModel
            = GsonUtils.gsonStandard.fromJson(json, AppInitConfigModel.class);
        return appInitConfigModel;
      } catch (Exception e) {
        // bad file format, try delete file
        if (e instanceof JsonSyntaxException) {
          FileUtilsCustom.tryDeleteFile(appInitConfigPath);
        }
      }
    }

    // file does not exist, build and persist
    AppInitConfigModel appInitConfigModel = new AppInitConfigModel();
    // get external sd location as preferred
    appInitConfigModel.APP_DIR = getBestAppDirectory(allPossibleDirs,
        APP_DIRECTORY_PREFERENCE.EXT_SD, APP_DIRECTORY_PREFERENCE.INT_SD,
        APP_DIRECTORY_PREFERENCE.OTHERS);
    appInitConfigModel.HERE_DIR = appInitDir;
    String json = GsonUtils.gsonPretty.toJson(appInitConfigModel, appInitConfigModel.getClass());
    try {
      FileUtilsCustom.saveToFile(
          LocalFileService.getAppInitConfigPathFromAppInitDir(appInitDir)
          , json, false);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return appInitConfigModel;
  }

  private AppConfigModel initAppConfig(String appDir) throws IOException {
    // get app config path
    String appConfigPath =
        LocalFileService.getAppConfigPathFromAppDir(appDir);
    File appConfig = new File(appConfigPath);

    // load config file exists
    if (appConfig.exists()) {
      try {
        // successful loading returns object
        String json = FileUtils.readFileToString(appConfig);
        AppConfigModel appConfigModel
            = GsonUtils.gsonStandard.fromJson(json, AppConfigModel.class);
        return appConfigModel;
      } catch (Exception e) {
        // bad file format, try delete file
        if (e instanceof JsonSyntaxException) {
          FileUtilsCustom.tryDeleteFile(appConfigPath);
        }
      }
    }

    // file does not exist, build and persist
    // create model
    AppConfigModel appConfigModel = new AppConfigModel();
    appConfigModel.APP_DIR = appDir;
    appConfigModel.HERE_DIR = appDir;
    // save to json
    String json = GsonUtils.gsonPretty.toJson(appConfigModel, appConfigModel.getClass());
    try {
      FileUtilsCustom.saveToFile(appConfigPath, json, false);
    } catch (IOException e) {
      throw new IOException("Cannot save appConfigModel.");
    }

    return appConfigModel;
  }

  private HashSet<String> getAllPossibleAppDirs(Context appContext) {
    HashSet<String> possibleAppDirs = new HashSet<>();
    for (File f : ContextCompat.getExternalFilesDirs(appContext, null)) {
      possibleAppDirs.add(f.getAbsolutePath());
    }
    possibleAppDirs.add(appContext.getFilesDir().getAbsolutePath());
    return possibleAppDirs;
  }

  enum APP_DIRECTORY_PREFERENCE {
    EXT_SD, INT_SD, OTHERS
  }

  private String getBestAppDirectory(HashSet<String> allPossibleDirs
      , APP_DIRECTORY_PREFERENCE... preferenceList) {
    // make temp file arrays
    List<String> tempDirs = new ArrayList<>();

    // Filtering process
    for (String dir : allPossibleDirs) {
      File f = new File(dir);

      // checks
      if (!f.exists()) continue;
      if (!f.isDirectory()) continue;
      if (!f.canWrite()) continue;

      // add dir
      tempDirs.add(dir);
    }

    // Choosing the best
    // preference for sd, then emulated, then else
    List<String> sdDirs = new ArrayList<>();
    List<String> emulatedDirs = new ArrayList<>();
    List<String> otherDirs = new ArrayList<>();

    Pattern sdDirPattern = Pattern.compile(".*/sdcard[0-9]+.*");
    Pattern emulatedPattern = Pattern.compile(".*/emulated.*");

    for (String dirPath : tempDirs) {
      if (dirPath.matches(sdDirPattern.pattern())) {
        sdDirs.add(dirPath);
      } else if (dirPath.matches(emulatedPattern.pattern())) {
        emulatedDirs.add(dirPath);
      } else {
        otherDirs.add(dirPath);
      }
    }

    String chosenDir = "";
    for (APP_DIRECTORY_PREFERENCE pref : preferenceList) {
      switch (pref) {
        case EXT_SD: {
          // take best
          if (sdDirs.size() > 0) {
            // break if only one entry
            if (sdDirs.size() == 1) {
              chosenDir = sdDirs.get(0);
            } else {
              Matcher matcher;

              // take sdcard with biggest number at the back
              TreeMap<String, String> snippetToFullDir = new TreeMap<>();
              for (String sdDir : sdDirs) {
                matcher = sdDirPattern.matcher(sdDir);
                if (matcher.matches()) {
                  // take only first match
                  snippetToFullDir.put(matcher.group(), sdDir);
                }
              }

              // choose best
              chosenDir = snippetToFullDir.get(snippetToFullDir.lastKey());
            }
          }
          break;
        }
        case INT_SD: {
          if (emulatedDirs.size() > 0) {
            chosenDir = emulatedDirs.get(0);
          }
          break;
        }
        case OTHERS: {
          chosenDir = otherDirs.get(0);
          break;
        }
      }

      // break if chosen dir is assigned
      if (!StringUtils.isEmpty(chosenDir)) break;
    }

    return chosenDir;
  }

  // TODO delete if not needed
  private List<String> getExistingAppInitConfigs() throws IOException {
    List<String> dirsWithAppInitConfig = new ArrayList<>();

    // find and use if there are existing locations
    // take first one found
    for (String appDir : allPossibleDirs) {
      String appConfigPath
          = LocalFileService.getAppConfigPathFromAppDir(appDir);
      File appConfigFile = new File(appConfigPath);
      if (appConfigFile.exists()) {
        try {
          // try deserialize
          String json = FileUtils.readFileToString(appConfigFile);
          GsonUtils.gsonStandard.fromJson(json, AppConfigModel.class);

          dirsWithAppInitConfig.add(appConfigPath);
        } catch (Exception e) {
          // bad file format, try delete file
          if (e instanceof JsonSyntaxException) {
            FileUtilsCustom.tryDeleteFile(appConfigPath);
          }
        }
      }
    }

    return dirsWithAppInitConfig;
  }


}
