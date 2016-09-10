package com.nossbigg.htmlminder.utils;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.nossbigg.htmlminder.controller.AsyncCallbackHandler;
import com.nossbigg.htmlminder.controller.TweetHTMLWorker;
import com.nossbigg.htmlminder.model.HTMLSubWorkerModel;
import com.nossbigg.htmlminder.model.TweetHTMLWorkerModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Gibson on 8/30/2016.
 */
public class HTMLWorkerUtils {
  public static String AddParamsToURL(String url, Map<String, String> params) {
    // if empty parameters, return
    if (params.size() == 0) {
      return url;
    }

    StringBuilder sb = new StringBuilder(url);
    sb.append("?");

    int paramsCount = params.size();
    int paramIndex = 0;
    for (Map.Entry<String, String> entry : params.entrySet()) {
      try {
        // add param to url
        sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
        sb.append("=");
        sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));

        // increments index
        paramIndex++;

        // add ampersand if not last element
        if (paramIndex != paramsCount) {
          sb.append("&");
        }
      } catch (UnsupportedEncodingException e) {
        // skip entry
      }
    }

    return sb.toString();
  }

  public static int WorkerNameToNotificationID(String workerName) {
    int result = 0;
    for (char c : workerName.toCharArray()) {
      result += Character.getNumericValue(c);
    }

    return Integer.parseInt(result + "" + ((int) Math.random() * 100000));
  }

  public static boolean isAnyFieldsEmpty(String... fields) {
    for (String s : fields) {
      if (StringUtils.isEmpty(s)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Assumption: If an array exists, it can only exist at the last element
   * Note: Can consider future jsonarray traversal
   *
   * @param jo
   * @param fieldsCommaDelimited
   * @return
   */
  public static boolean isJsonFieldsEmpty(JSONObject jo, String fieldsCommaDelimited) {
    fieldsCommaDelimited = fieldsCommaDelimited.replaceAll(" ", "");

    HashSet<String> fieldList = new HashSet<>(Arrays.asList(fieldsCommaDelimited.split(",")));

    for (String field : fieldList) {
      JSONObject lastObject = jo;

      ArrayList<String> terms = new ArrayList<>(Arrays.asList(field.split("\\.")));
      int size = terms.size();
      int indexLastElement = size - 1;
      for (int index = 0; index < size; index++) {
        String term = terms.get(index);
        //boolean isArrayAccessor = term.matches("^(?:[\\w]+)((\\[[\\d]+\\])+)$");

        Object o = new Object();

        try {
          o = lastObject.get(term);
        } catch (JSONException e) {
          // no such term, failed
          return false;
        }

        // check if last object
        if (index == indexLastElement) {
          if (o instanceof JSONArray) {
            return ((JSONArray) o).length() == 0;
          } else if (o instanceof String) {
            return (StringUtils.isEmpty((String) o));
          } else {
            // not quite sure what to do with this edge case...
          }
        } else {
          // not supposed to end so quickly
          if (!(o instanceof JSONObject)) return false;

          // move to next object
          lastObject = (JSONObject) o;
        }
      }
    }

    return true;
  }

  public static String getCurrentDateInFormat(String format) {
    SimpleDateFormat sdfDate = new SimpleDateFormat(format);
    Date now = new Date();
    return sdfDate.format(now);
  }

  public static HttpsURLConnection prepareBasicConnection(HTMLSubWorkerModel htmlSubWorkerModel) throws IOException {
    // parses params if GET
    String urlString = htmlSubWorkerModel.url;
    if (StringUtils.equals("GET", htmlSubWorkerModel.method)) {
      urlString = HTMLWorkerUtils.AddParamsToURL(urlString, htmlSubWorkerModel.parameters);
    }

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
        wr.write(URLEncoder.encode(entry.getKey(), "UTF-8")
            + "="
            + URLEncoder.encode(entry.getValue(), "UTF-8")
        );
      }

      // close connection
      wr.flush();
      wr.close();
    }

    return con;
  }

  // TWITTER WORKERS
  // OAUTH
  public static void initTwitterOAuth(final TweetHTMLWorker tweetHTMLWorker
      , final TweetHTMLWorkerModel workerModel) throws IOException, JSONException {
    if (!StringUtils.isEmpty(workerModel.OAUTH_TOKEN)) {
      tweetHTMLWorker.callbackOAuthRequest(true);
      return;
    }

    new AsyncTask<TweetHTMLWorkerModel, Void, Boolean>() {
      @Override
      protected Boolean doInBackground(TweetHTMLWorkerModel... params) {
        String OAUTH_TOKEN = "";
        try {
          OAUTH_TOKEN = TwitterAPIUtils.getOAuthBearerToken(workerModel.CONSUMER_KEY, workerModel.CONSUMER_SECRET);
          workerModel.OAUTH_TOKEN = OAUTH_TOKEN;
        } catch (Exception e) {
          e.printStackTrace();
          return false;
        }
        return true;
      }

      @Override
      protected void onPostExecute(Boolean result) {
        tweetHTMLWorker.callbackOAuthRequest(result);
      }
    }.execute(workerModel);
  }

  public static HashMap<String, Long> getWorkerNameToLastMaxIdTweetJson(String path) {
    // build new empty list
    HashMap<String, Long> newList = new HashMap<>();

    // if file doesn't exist, just build new empty list based off worker names
    File file = new File(path);
    if (!file.exists()) {
      return newList;
    }

    // read file content
    String json = "";
    try {
      json = FileUtils.readFileToString(file);
    } catch (IOException e) {
      // failed to read file
      return newList;
    }

    // deserialize map
    Gson gson = new Gson();
    HashMap<String, Long> importedList =
        gson.fromJson(json, newList.getClass());

    // return entries
    return newList;
  }
}
