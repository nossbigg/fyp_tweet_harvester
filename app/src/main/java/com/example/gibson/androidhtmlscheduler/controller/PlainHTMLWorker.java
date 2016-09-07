package com.example.gibson.androidhtmlscheduler.controller;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.example.gibson.androidhtmlscheduler.exception.HTMLWorkerCallFailedException;
import com.example.gibson.androidhtmlscheduler.exception.HTMLWorkerException;
import com.example.gibson.androidhtmlscheduler.exception.HTMLWorkerNotOKException;
import com.example.gibson.androidhtmlscheduler.model.HTMLSubWorkerModel;
import com.example.gibson.androidhtmlscheduler.model.PlainHTMLWorkerModel;
import com.example.gibson.androidhtmlscheduler.utils.FileUtilsCustom;
import com.example.gibson.androidhtmlscheduler.utils.HTMLWorkerUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Gibson on 9/6/2016.
 */
public class PlainHTMLWorker extends AbstractHTMLWorker {
  public PlainHTMLWorkerModel workerModel;

  public PlainHTMLWorker(PlainHTMLWorkerModel plainHTMLWorkerModel,
                         String appDirectory) {
    super(plainHTMLWorkerModel, appDirectory);
    workerModel = plainHTMLWorkerModel;
    initWorkers();
  }

  private void initWorkers() {
    for (HTMLSubWorkerModel htmlSubWorkerModel : workerModel.subWorkers) {
      // make new runnable
      Handler handler = new Handler();
      Runnable runnable = initSubWorkerRunnable(handler,
          htmlSubWorkerModel);

      // skip subworker if essential fields are missing
      if (HTMLWorkerUtils.isAnyFieldsEmpty(
          htmlSubWorkerModel.subWorkerName, htmlSubWorkerModel.url, htmlSubWorkerModel.method
      )) continue;

      // create new htmlsubworker
      HTMLSubWorker htmlSubWorker =
          new HTMLSubWorker(htmlSubWorkerModel, handler, runnable);

      // add to list of workers
      namesToWorkersMap.put(htmlSubWorkerModel.subWorkerName, htmlSubWorker);
    }
  }

  @Override
  public void startWorker() {
    for (Map.Entry<String, HTMLSubWorker> entry : namesToWorkersMap.entrySet()) {
      HTMLSubWorker htmlSubWorker = entry.getValue();
      htmlSubWorker.handler.post(htmlSubWorker.runnable);
    }
  }

  @Override
  public void stopWorker() {
    for (Map.Entry<String, HTMLSubWorker> entry : namesToWorkersMap.entrySet()) {
      HTMLSubWorker htmlSubWorker = entry.getValue();
      htmlSubWorker.handler.removeCallbacks(htmlSubWorker.runnable);
    }
  }

  private Runnable initSubWorkerRunnable(final Handler handler,
                                         final HTMLSubWorkerModel htmlSubWorkerModel) {
    return new Runnable() {
      @Override
      public void run() {
        // do html task
        initSubWorkerHTMLTask(htmlSubWorkerModel).execute();

        // repeat task
        handler.postDelayed(this, htmlSubWorkerModel.interval);
      }
    };
  }

  private AsyncTask<Void, Void, Void> initSubWorkerHTMLTask(final HTMLSubWorkerModel htmlSubWorkerModel) {
    return new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        try {
          // get response
          String response = getResponseFromHTMLCall(htmlSubWorkerModel);

          // check fields not null (if specified)
          if(!StringUtils.isEmpty(htmlSubWorkerModel.checkFieldsNotEmpty)){
            try {
              JSONObject jo = new JSONObject(response);
              if (HTMLWorkerUtils.isJsonFieldsEmpty(jo, htmlSubWorkerModel.checkFieldsNotEmpty)){
                return null;
              }
            } catch (JSONException e) {
              // it's not JSON, no worries
            }
          }

          // add line as delimiter
          response += "\n";

          // create path to save (based on date)
          String subWorkerDirectory = workerDirectory + htmlSubWorkerModel.subWorkerName + "/";
          String fullPath = subWorkerDirectory +
              htmlSubWorkerModel.subWorkerName + "-" +
              getCurrentDateInFormat("yyyy-MM-dd") + ".text";

          // save response
          FileUtilsCustom.saveToFile(fullPath, response, true);

          Log.d("UNIQUE_TAG", "response: " + response);
        } catch (HTMLWorkerException e) {
//          e.printStackTrace();
        } catch (IOException e) {
          Log.d("UNIQUE_TAG", e.toString());
          //
        }

        return null;
      }
    };
  }

  private String getResponseFromHTMLCall(HTMLSubWorkerModel htmlSubWorkerModel) throws HTMLWorkerException {
    String response = "";

    // get url
    // parses params if GET
    String urlString = htmlSubWorkerModel.url;
    if (StringUtils.equals("GET", htmlSubWorkerModel.method)) {
      urlString = HTMLWorkerUtils.AddParamsToURL(urlString, htmlSubWorkerModel.parameters);
    }

    try {
      // build url object
      URL url = new URL(urlString);

      // build url connection object
      HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
      con.setRequestMethod(htmlSubWorkerModel.method);

      // if POST, send params
      if (StringUtils.equals("POST", htmlSubWorkerModel.method)) {
        // set form urlencoded
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        // enable output stream
        con.setDoOutput(true);

        // open connection
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));

        // send params
        for (Map.Entry<String, String> entry : htmlSubWorkerModel.parameters.entrySet()) {
          StringBuilder sb = new StringBuilder();
          sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
          sb.append("=");
          sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
          wr.write(sb.toString());
        }

        // close connection
        wr.flush();
        wr.close();
      }

      // get response code
      int responseCode = con.getResponseCode();
      if (responseCode != 200) throw new HTMLWorkerNotOKException("");

      // read response from connection
      StringBuilder str = new StringBuilder();
      BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine = "";
      while ((inputLine = br.readLine()) != null) {
        str.append(inputLine + System.getProperty("line.separator"));
      }

      // store response
      response = str.toString();
    } catch (IOException e) {
      throw new HTMLWorkerCallFailedException("");
    }

    return response;
  }

  private String getCurrentDateInFormat(String format) {
    SimpleDateFormat sdfDate = new SimpleDateFormat(format);//dd/MM/yyyy
    Date now = new Date();
    String strDate = sdfDate.format(now);
    return strDate;
  }
}
