package com.nossbigg.htmlminder.controller;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nossbigg.htmlminder.exception.HTMLWorkerCallFailedException;
import com.nossbigg.htmlminder.exception.HTMLWorkerException;
import com.nossbigg.htmlminder.exception.HTMLWorkerNotOKException;
import com.nossbigg.htmlminder.model.HTMLSubWorkerModel;
import com.nossbigg.htmlminder.model.HTMLWorkerNotificationModel;
import com.nossbigg.htmlminder.model.JSONResponseMetadataModel;
import com.nossbigg.htmlminder.model.TweetHTMLWorkerModel;
import com.nossbigg.htmlminder.utils.FileUtilsCustom;
import com.nossbigg.htmlminder.utils.GsonUtils;
import com.nossbigg.htmlminder.utils.HTMLWorkerModelUtils;
import com.nossbigg.htmlminder.utils.HTMLWorkerUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

/**
 * Tweet HTML Worker
 * Note: Tweets are collected sequentially by worker order, and not in parallel.
 * <p>
 * Created by Gibson on 9/6/2016.
 */
public class TweetHTMLWorker extends AbstractHTMLWorker implements AsyncCallbackHandler {
  public TweetHTMLWorkerModel workerModel;

  public Boolean isProperConfig = true;

  public long requestInterval = 0;
  public long requestIntervalRandomVariance = 0;

  // LAST TWEET ID Persistence
  // stores last max id to each worker
  public HashMap<String, Long> workerNameToLastMaxIdTweetMap = new HashMap<>();
  // Handler and runnable for persistence
  public Handler lastTweetIdPersistenceHandler = new Handler();
  public Runnable lastTweetIdPersistenceRunnable;
  public long lastTweetIdPersistenceInterval = 5000;

  // LOOPING THREAD VARIABLES
  private Iterator<Map.Entry<String,HTMLSubWorker>> namesToWorkersMapIterator;

  // Handler and runnable for master loop
  public Handler masterWorkerHandler = new Handler();
  public Runnable masterWorkerRunnable;

  public TweetHTMLWorker(TweetHTMLWorkerModel tweetHTMLWorkerModel) {
    super(tweetHTMLWorkerModel);
    workerModel = tweetHTMLWorkerModel;
    initWorkers();
    masterWorkerRunnable = initMasterWorkerRunnable(masterWorkerHandler, namesToWorkersMap);
    namesToWorkersMapIterator = namesToWorkersMap.entrySet().iterator();

    lastTweetIdPersistenceRunnable = initLastTweetIdPersistenceRunnable(
        lastTweetIdPersistenceHandler, workerNameToLastMaxIdTweetMap,
        LocalFileService.getLastMaxTweetJsonPathFromWorkerDataSaveDir(workerModel.dataSaveDir)
    );
  }

  @Override
  public void startWorker() {
    // try load oauth
    try {
      HTMLWorkerUtils.initTwitterOAuth(this, workerModel);
    } catch (Exception e) {
      isProperConfig = false;
    }
  }

  public void startWorkerAfterCallback() {
    if (!isProperConfig) return;

    // start worker
    masterWorkerHandler.post(masterWorkerRunnable);

    // start last tweet id map persistence
    lastTweetIdPersistenceHandler.post(lastTweetIdPersistenceRunnable);
  }

  @Override
  public void stopWorker() {
    masterWorkerHandler.removeCallbacks(masterWorkerRunnable);
    lastTweetIdPersistenceHandler.removeCallbacks(lastTweetIdPersistenceRunnable);
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

  private void initWorkers() {
    // check if has workers
    if (workerModel.subWorkers.size() == 0) {
      isProperConfig = false;
      return;
    }

    for (HTMLSubWorkerModel htmlSubWorkerModel : workerModel.subWorkers) {
      // init htmlsubworker object
      // htmlsubworker
      HTMLSubWorker htmlSubWorker =
          new HTMLSubWorker(htmlSubWorkerModel);

      // skip subworker if essential fields are missing
      if (HTMLWorkerUtils.isAnyFieldsEmpty(
          htmlSubWorkerModel.subWorkerName, htmlSubWorkerModel.url
          , htmlSubWorkerModel.method
      )) continue;

      // add to list of workers
      namesToWorkersMap.put(htmlSubWorkerModel.subWorkerName, htmlSubWorker);
    }

    // get and set frequency and random interval from first worker
    HTMLSubWorkerModel firstModel = workerModel.subWorkers.get(0);
    requestInterval = firstModel.interval;
    requestIntervalRandomVariance = firstModel.intervalRandomVariance;

    // try load saved last max id tweets json file
    workerNameToLastMaxIdTweetMap =
        HTMLWorkerUtils.getWorkerNameToLastMaxIdTweetJson(
            LocalFileService.getLastMaxTweetJsonPathFromWorkerDataSaveDir(
                workerModel.dataSaveDir
            ));
    // if returned nothing, make new list
    if (workerNameToLastMaxIdTweetMap.size() == 0) {
      for(String subWorkerName : namesToWorkersMap.keySet()){
        workerNameToLastMaxIdTweetMap.put(subWorkerName, 0L);
      }
    }
  }

  /**
   * Maintains launching of subworkers
   *
   * @param
   * @param
   * @param handler
   * @param namesToWorkersMap
   * @return
   */
  private Runnable initMasterWorkerRunnable(final Handler handler,
                                            final HashMap<String, HTMLSubWorker> namesToWorkersMap) {
    final TweetHTMLWorker that = this;
    return new Runnable() {
      @Override
      public void run() {
        // reset iterator if at end
        if(!namesToWorkersMapIterator.hasNext()){
          namesToWorkersMapIterator = namesToWorkersMap.entrySet().iterator();
        }

        // get current HTMLWorker
        HTMLSubWorker subWorker = namesToWorkersMapIterator.next().getValue();

        // do html task
        initSubWorkerHTMLTask(subWorker).execute();
        // repeat task
        handler.postDelayed(this, requestInterval);
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
          String response = getResponseFromHTMLCall(htmlSubWorkerModel);

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

            // get and save tweetmaxid
            try {
              JSONObject search_metadata = jo.getJSONObject("search_metadata");
              long last_max_id = search_metadata.getLong("max_id");
              workerNameToLastMaxIdTweetMap.put(htmlSubWorkerModel.subWorkerName, last_max_id);
            } catch (JSONException e) {
              // cannot find search_metadata, just don't save lo
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
              HTMLWorkerUtils.getDateInFormat(new Date(), "yyyy-MM-dd")  + ".text";

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
      // add maxtweet to subworkermodel params
      boolean hasAddedLastMaxId = false;
      long last_max_id = workerNameToLastMaxIdTweetMap.get(htmlSubWorkerModel.subWorkerName);
      if (last_max_id != 0 && last_max_id > 0L) {
        htmlSubWorkerModel.parameters.put("since_id", "" + last_max_id);
        hasAddedLastMaxId = true;
      }

      // build url connection object
      HttpsURLConnection con = HTMLWorkerUtils.prepareBasicConnection(htmlSubWorkerModel);
      String ENCODED_CREDENTIALS = "Bearer " + workerModel.OAUTH_TOKEN;
      con.setRequestProperty("Authorization", ENCODED_CREDENTIALS);
      con.setRequestProperty("Host", "api.twitter.com");
      con.setRequestProperty("User-Agent", "GibFYP");
      con.setRequestProperty("Accept-Encoding", "gzip");

      // remove maxtweet from subworkermodel params (if added)
      if (hasAddedLastMaxId) {
        htmlSubWorkerModel.parameters.remove("since_id");
      }

      // get response code
      int responseCode = con.getResponseCode();
      if (responseCode != HttpsURLConnection.HTTP_OK) throw new HTMLWorkerNotOKException("");

      // read response from connection (using gzip)
      StringBuilder str = new StringBuilder();
      BufferedReader br = new BufferedReader(
          new InputStreamReader(
              new GZIPInputStream(con.getInputStream())));
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

  @Override
  public void callbackOAuthRequest(Boolean result) {
    isProperConfig = result;

    startWorkerAfterCallback();

    if (isProperConfig) {
      // persist OAUTH_TOKEN by saving to json
      String workerJsonString = HTMLWorkerModelUtils.HTMLWorkerModelToJson(workerModel);
      try {
        FileUtilsCustom.saveToFile(workerModel.jsonConfigFilePath,
            workerJsonString, false);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  // TWEET ID PERSISTENCE
  public Runnable initLastTweetIdPersistenceRunnable(final Handler handler,
                                                     final HashMap<String, Long> workerNameToLastMaxIdTweetMap,
                                                     final String savePath) {
    return new Runnable() {
      @Override
      public void run() {
        // serialize object to json
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(workerNameToLastMaxIdTweetMap, workerNameToLastMaxIdTweetMap.getClass());

        // persist last tweet id map to disk
        try {
          FileUtilsCustom.saveToFile(savePath, jsonString, false);
        } catch (IOException e) {
          e.printStackTrace();
        }

        // repeat task
        handler.postDelayed(this, lastTweetIdPersistenceInterval);
      }
    };
  }
}
