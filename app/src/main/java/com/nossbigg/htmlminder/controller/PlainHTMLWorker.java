package com.nossbigg.htmlminder.controller;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.nossbigg.htmlminder.exception.HTMLWorkerCallFailedException;
import com.nossbigg.htmlminder.exception.HTMLWorkerException;
import com.nossbigg.htmlminder.exception.HTMLWorkerNotOKException;
import com.nossbigg.htmlminder.model.HTMLSubWorkerModel;
import com.nossbigg.htmlminder.model.JSONResponseMetadataModel;
import com.nossbigg.htmlminder.model.PlainHTMLWorkerModel;
import com.nossbigg.htmlminder.utils.FileUtilsCustom;
import com.nossbigg.htmlminder.utils.GsonUtils;
import com.nossbigg.htmlminder.utils.HTMLWorkerUtils;

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

  public PlainHTMLWorker(PlainHTMLWorkerModel plainHTMLWorkerModel) {
    super(plainHTMLWorkerModel);
    workerModel = plainHTMLWorkerModel;
    initWorkers();
  }

  private void initWorkers() {
    for (HTMLSubWorkerModel htmlSubWorkerModel : workerModel.subWorkers) {
      // make new runnable
      Handler handler = new Handler();
      Runnable runnable = initSubWorkerRunnable(handler,
          htmlSubWorkerModel);

      // create new htmlsubworker
      HTMLSubWorker htmlSubWorker =
          new HTMLSubWorker(htmlSubWorkerModel, handler, runnable);

      // skip subworker if essential fields are missing
      if (HTMLWorkerUtils.isAnyFieldsEmpty(
          htmlSubWorkerModel.subWorkerName, htmlSubWorkerModel.url, htmlSubWorkerModel.method
      )) continue;

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

          // get timestamp received
          String timestamp = Long.toString(System.currentTimeMillis());

          // check if json response
          JSONObject jo = new JSONObject();
          boolean isValidJson = true;
          try {
            jo = new JSONObject(response);
          } catch (JSONException e) {
            isValidJson = false;
          }

          // JSON-related operations
          if (isValidJson) {
            // check fields not null (if specified)
            if (!StringUtils.isEmpty(htmlSubWorkerModel.checkFieldsNotEmpty)
                && HTMLWorkerUtils.isJsonFieldsEmpty(jo, htmlSubWorkerModel.checkFieldsNotEmpty)) {
              return null;
            }

            // add extra metadata
            try {
              JSONResponseMetadataModel meta
                  = new JSONResponseMetadataModel(timestamp, htmlSubWorkerModel);
              jo.put("htmlminder_custom_metadata",
                  new JSONObject(GsonUtils.gsonStandard.toJson(meta, meta.getClass())));
            } catch (JSONException e) {
              // there's no reason for it to be here...
            }

            // update response with new json
            response = jo.toString();
          }

          // add line as delimiter
          response += "\n";

          // create path to save (based on date)
          String fullPath = htmlSubWorkerModel.dataSaveDir + "/" +
              htmlSubWorkerModel.subWorkerName + "-" +
              HTMLWorkerUtils.getCurrentDateInFormat("yyyy-MM-dd") + ".text";

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

    try {
      // build url connection object
      HttpsURLConnection con = HTMLWorkerUtils.prepareBasicConnection(htmlSubWorkerModel);

      // get response code
      int responseCode = con.getResponseCode();
      if (responseCode != HttpsURLConnection.HTTP_OK) throw new HTMLWorkerNotOKException("");

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
}
