package com.nossbigg.htmlminder.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by Gibson on 9/6/2016.
 */
public class FileUtilsCustom {
  /**
   * Saves to file
   *
   * @param path
   * @param content
   */
  public static void saveToFile(String path, String content, boolean isAppend) throws IOException {
    File file = new File(path);

    if(file.exists()){
      FileUtils.write(file, content, isAppend);
    } else {
      FileUtils.write(file, content);
    }
  }

  public static String readfile(String path) throws IOException {
    return FileUtils.readFileToString(new File(path));
  }
}
