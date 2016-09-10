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

    if (file.exists()) {
      FileUtils.write(file, content, isAppend);
    } else {
      FileUtils.write(file, content);
    }
  }

  public static boolean tryDeleteFile(String path){
    File f = new File(path);
    try{
      FileUtils.forceDelete(f);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public static boolean makeGzipFromFile(String sourcePath, String zipFilePath) {
    // check if file exists
    File sourceFile = new File(sourcePath);
    if (!sourceFile.exists()) return false;

    // write to file (using direct input from file, no need save response)
    // ref: https://www.mkyong.com/java/how-to-compress-a-file-in-gzip-format/
    byte[] buffer = new byte[1024];
    GZIPOutputStream gzos = null;
    FileInputStream in = null;
    try {
      // open streams
      gzos = new GZIPOutputStream(new FileOutputStream(zipFilePath));
      in = new FileInputStream(sourcePath);

      // read/write files
      int len;
      while ((len = in.read(buffer)) > 0) {
        gzos.write(buffer, 0, len);
      }
    } catch (IOException ex) {
      // problems with process, make file failed
      return false;
    } finally {
      try {
        // close all streams
        in.close();
        gzos.finish();
        gzos.close();
      } catch (IOException e) {
      }
    }

    return true;
  }
}
