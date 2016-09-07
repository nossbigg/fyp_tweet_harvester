package com.nossbigg.htmlminder.utils;

import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

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
}
