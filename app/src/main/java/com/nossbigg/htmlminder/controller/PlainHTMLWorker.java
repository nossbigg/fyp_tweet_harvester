package com.nossbigg.htmlminder.controller;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.nossbigg.htmlminder.exception.HTMLWorkerCallFailedException;
import com.nossbigg.htmlminder.exception.HTMLWorkerException;
import com.nossbigg.htmlminder.exception.HTMLWorkerNotOKException;
import com.nossbigg.htmlminder.model.HTMLSubWorkerModel;
import com.nossbigg.htmlminder.model.HTMLWorkerNotificationModel;
import com.nossbigg.htmlminder.model.JSONResponseMetadataModel;
import com.nossbigg.htmlminder.model.PlainHTMLWorkerModel;
import com.nossbigg.htmlminder.utils.FileUtilsCustom;
import com.nossbigg.htmlminder.utils.GsonUtils;
import com.nossbigg.htmlminder.utils.HTMLWorkerUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
      // init htmlsubworker object
      // handler
      Handler handler = new Handler();
      // htmlsubworker
      HTMLSubWorker htmlSubWorker =
          new HTMLSubWorker(htmlSubWorkerModel, handler);
      // runnable
      Runnable runnable = initSubWorkerRunnable(handler,
          htmlSubWorker);
      // assign runnable to htmlsubworker
      htmlSubWorker.assignRunnable(runnable);

      // skip subworker if essential fields are missing
      if (HTMLWorkerUtils.isAnyFieldsEmpty(
          htmlSubWorkerModel.subWorkerName, htmlSubWorkerModel.url
          , htmlSubWorkerModel.method
      )) continue;

      // add to list of workers
      namesToWorkersMap.put(htmlSubWorkerModel.subWorkerName, htmlSubWorker);
    }
  }

  @Override
  public void startWorker() {
    // start all workers
    for (Map.Entry<String, HTMLSubWorker> entry : namesToWorkersMap.entrySet()) {
      HTMLSubWorker htmlSubWorker = entry.getValue();
      htmlSubWorker.handler.post(htmlSubWorker.runnable);
    }
  }

  @Override
  public void stopWorker() {
    // update destroy flag
    isWorkerDestroy = true;

    // stop all workers
    for (Map.Entry<String, HTMLSubWorker> entry : namesToWorkersMap.entrySet()) {
      HTMLSubWorker htmlSubWorker = entry.getValue();
      htmlSubWorker.handler.removeCallbacks(htmlSubWorker.runnable);
    }
  }

  @Override
  public String getNotificationInfo() {
    StringBuilder note = new StringBuilder();

    for (Map.Entry<String, HTMLSubWorker> set : namesToWorkersMap.entrySet()) {
      HTMLSubWorker subWorker = set.getValue();
      HTMLWorkerNotificationModel notificationModel = subWorker.notificationModel;
      note.append(subWorker.htmlSubWorkerModel.subWorkerName + ": ");
      note.append(
          // display none if null
          (notificationModel.lastPulled_epoch != 0L) ?
              HTMLWorkerUtils.convertEpochToDateFormat(
                  notificationModel.lastPulled_epoch, "d/M/yyyy hh:mm:ss a") :
              "None"
      );
      note.append("\n");
    }

    return note.toString();
  }

  private Runnable initSubWorkerRunnable(final Handler handler,
                                         final HTMLSubWorker htmlSubWorker) {
    return new Runnable() {
      @Override
      public void run() {
        // do html task
        initSubWorkerHTMLTask(htmlSubWorker).execute();

        // repeat task
        handler.postDelayed(this, htmlSubWorker.htmlSubWorkerModel.interval);
      }
    };
  }

  private AsyncTask<Void, Void, Void> initSubWorkerHTMLTask(final HTMLSubWorker htmlSubWorker) {
    return new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        try {
          HTMLSubWorkerModel htmlSubWorkerModel = htmlSubWorker.htmlSubWorkerModel;

          // get response
          String response = getResponseFromHTMLCall(htmlSubWorker);

          // TERMINATE IF DESTROY FLAG IS TRUE
          if (isWorkerDestroy) return null;

          // get timestamp received
          Long timestamp = System.currentTimeMillis();

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

          // save timestamp
          htmlSubWorker.notificationModel.lastPulled_epoch = timestamp;

          // add line as delimiter
          response += "\n";

          // create path to save (based on date)
          String fullPath = htmlSubWorkerModel.dataSaveDir + "/" +
              htmlSubWorkerModel.subWorkerName + "-" +
              HTMLWorkerUtils.getDateInFormat(new Date(), "yyyy-MM-dd") + ".text";

          // save response
          FileUtilsCustom.saveToFile(fullPath, response, true);

          Log.d("UNIQUE_TAG",
              workerModel.workerName + "|" + htmlSubWorkerModel.subWorkerName
                  + "|" + "response: " + response);
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

  private String getResponseFromHTMLCall(HTMLSubWorker htmlSubWorker) throws HTMLWorkerException {
    HTMLSubWorkerModel htmlSubWorkerModel = htmlSubWorker.htmlSubWorkerModel;

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
