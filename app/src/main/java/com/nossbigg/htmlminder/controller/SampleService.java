package com.nossbigg.htmlminder.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.nossbigg.htmlminder.R;
import com.nossbigg.htmlminder.utils.FileUtilsCustom;
import com.nossbigg.htmlminder.view.MainActivity;
import com.google.common.base.Strings;
import com.google.common.primitives.Ints;

import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Gibson on 8/12/2016.
 */
public class SampleService extends Service {
  // FIXME
  private final String ROOT_DIR = "";
  private boolean isInitialized = false;
  private PowerManager.WakeLock wakeLock;

  @Override
  public void onCreate() {
    super.onCreate();

    //wakelock
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNjfdhotDimScreen");
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private long tweet_last_max_id = 0L;

  @Override
  public int onStartCommand(Intent intent, int flags, int startid) {
    String key = intent.getStringExtra("key");
//    Log.d("UNIQUE_TAG", key);

    // get filepath
    String filePath = ROOT_DIR + "a.txt";

    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
//        String twitterData = getTwitterData();
//
//        // append to file
//        String path = ROOT_DIR + "responses.txt";
//        try {
//          saveToFile(path, twitterData);
//        } catch (IOException e) {
//          e.printStackTrace();
//        }

//        getTwitterStreamingData();

        return null;
      }
    }.execute();

    // prevents more than 1 twitter harvester from initializing
    if (isInitialized) return 0;
    isInitialized = true;

    // start as foreground service
    Intent notificationIntent = new Intent(this, MainActivity.class);
    final int notificationID = 1337;
    PendingIntent pendingIntent =
        PendingIntent.getActivity(this, 0, notificationIntent, 0);
    final NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("HTML Minder")
            .setContentText("Running")
            .setContentIntent(pendingIntent);
    Notification notification = mBuilder.build();
    startForeground(notificationID, notification);

    // Twitter harvesting
    final Handler handler = new Handler();
    final int taskInterval = Ints.checkedCast(TimeUnit.MINUTES.toMillis(15) / 450);
    Runnable getTwitterDataHardcodeRunnable = new Runnable() {
      @Override
      public void run() {
        new AsyncTask<Void, Void, Void>() {
          @Override
          protected Void doInBackground(Void... params) {
            // get results
            String response = getTwitterDataHARDCODE(tweet_last_max_id);

            // don't save if it's empty
            if (StringUtils.isEmpty(response)) return null;

            // serialize to jsonobject
            JSONObject jo;
            JSONObject search_metadata;
            try {
              jo = new JSONObject(response);
              search_metadata = jo.getJSONObject("search_metadata");
            } catch (JSONException e) {
              // failed parsing
              e.printStackTrace();
              return null;
            }

            // update notifications
            updateNotification(notificationID, mBuilder);

            // save tweet since_id (to prevent grabbing of old tweets)
            try {
              tweet_last_max_id = search_metadata.getLong("max_id");
            } catch (JSONException e) {
              // failed parsing
              e.printStackTrace();
              return null;
            }

            // check number of tweets received
            int tweetsReceived = 0;
            try {
              tweetsReceived = jo.getJSONArray("statuses").length();
            } catch (JSONException e) {
              // failed parsing
              e.printStackTrace();
              return null;
            }
            // don't save if no new tweets
            if (tweetsReceived == 0) {
              Log.d("UNIQUE_TAG", "no new tweets");
              return null;
            }

            // add timestamp
            try {
              jo.put("timestamp", Long.toString(System.currentTimeMillis()));
            } catch (JSONException e) {
              // failed parsing
              e.printStackTrace();
              return null;
            }
            response = jo.toString();

            // add newline
            response += "\n";

            // save results
            String path = ROOT_DIR + "getTwitterDataHARDCODE.txt";
            try {
              FileUtils.write(new File(path), response, true);
              Log.d("UNIQUE_TAG", "saved: " + response);
            } catch (IOException e) {
              e.printStackTrace();
            }

            return null;
          }
        }.execute();

        // repeat task
        handler.postDelayed(this, taskInterval);
      }
    };
    handler.postDelayed(getTwitterDataHardcodeRunnable, taskInterval);

    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    startService(new Intent(this, SampleService.class));
  }

  // REST hardcoded
  private String getTwitterDataHARDCODE(long tweet_last_max_id) {
    // ref: http://www.coderslexicon.com/demo-of-twitter-application-only-oauth-authentication-using-java/

    // GET OAUTH
    String OAuthToken = "";
    try {
      OAuthToken = getAppOAuth();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    // encode credentials
    String ENCODED_CREDENTIALS = "Bearer " + OAuthToken;

    // GET TWEET
    String response = "";
    String q_term = "#rumor";

    // url encode q
    String q_urlencoded = "";
    try {
      q_urlencoded = "?q=" + URLEncoder.encode(q_term, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }

    // add tweet max limiter (if available)
    if (tweet_last_max_id != 0L) {
      q_urlencoded += "&since_id=" + tweet_last_max_id;
    }

    String tweetEndpoint = "https://api.twitter.com/1.1/search/tweets.json";
    String completeUrl = tweetEndpoint + q_urlencoded;

    try {
      URL obj = new URL(completeUrl);
      HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("Authorization", ENCODED_CREDENTIALS);
      con.setRequestProperty("Host", "api.twitter.com");
      con.setRequestProperty("User-Agent", "GibFYP");
      con.setRequestProperty("Accept-Encoding", "gzip");

      // get response code
      int responseCode = con.getResponseCode();
      if (responseCode != 200) return "";

      // read response from connection
      StringBuilder str = new StringBuilder();
      BufferedReader br = new BufferedReader(
          new InputStreamReader(
              new GZIPInputStream(con.getInputStream())));
      String inputLine = "";
      while ((inputLine = br.readLine()) != null) {
        str.append(inputLine + System.getProperty("line.separator"));
      }
      inputLine = str.toString();

      // save response
      response = inputLine;
    } catch (Exception e) {
      Log.d("UNIQUE_TAG", e.toString());
    }

    return response;
  }

  // REST
  private String getTwitterData() {
    // ref: http://www.coderslexicon.com/demo-of-twitter-application-only-oauth-authentication-using-java/

    // GET OAUTH
    String OAuthToken = "";
    try {
      OAuthToken = getAppOAuth();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // encode credentials
    String ENCODED_CREDENTIALS = "Bearer " + OAuthToken;

    // GET TWEET
    String response = "";
    String q = "@twitterapi";
    String tweetEndpoint = "https://api.twitter.com/1.1/search/tweets.json?q=%23rekt";

    try {
      URL obj = new URL(tweetEndpoint);
      HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("Authorization", ENCODED_CREDENTIALS);
      con.setRequestProperty("Host", "api.twitter.com");
      con.setRequestProperty("User-Agent", "GibFYP");
      con.setRequestProperty("Accept-Encoding", "gzip");

      // get response code
      int responseCode = con.getResponseCode();

      // read response from connection
      StringBuilder str = new StringBuilder();
      BufferedReader br = new BufferedReader(
          new InputStreamReader(
              new GZIPInputStream(con.getInputStream())));
      String inputLine = "";
      while ((inputLine = br.readLine()) != null) {
        str.append(inputLine + System.getProperty("line.separator"));
      }
      inputLine = str.toString();

      // save response
      response = inputLine;
      Log.d("UNIQUE_TAG", response);
    } catch (Exception e) {
      Log.d("UNIQUE_TAG", e.toString());
    }

    return response;
  }

  // Streaming
  private void getTwitterStreamingData() {
    // query information
    String method = "POST";
    String tweetEndpoint = "https://stream.twitter.com/1.1/statuses/filter.json";
    String q = "track=rekt";

    tweetEndpoint = "https://stream.twitter.com/1.1/statuses/sample.json";

    // Build AUTHORIZATION
    String oauth_consumer_key = "2LqkZ3BA9MUV2ZvFHMpESVnQn";
    String oauth_nonce = RandomStringUtils.randomAlphanumeric(40);
    String oauth_signature = "";
    String oauth_signature_method = "HMAC-SHA1";
    String oauth_timestamp = Long.toString(System.currentTimeMillis());
    String oauth_token = "768834133514526720-2fghzECfpZxLBUzKnQC8O4ciwK8dOMe";
    String oauth_version = "1.0";

    // generate signature
    {
      // build signature base string
      String parameter_string = "";
      Map<String, String> map = new HashMap<String, String>();
      map.put("track", "rekt");
      map.put("oauth_consumer_key", oauth_consumer_key);
      map.put("oauth_nonce", oauth_nonce);
      map.put("oauth_signature_method", oauth_signature_method);
      map.put("oauth_timestamp", oauth_timestamp);
      map.put("oauth_token", oauth_token);
      map.put("oauth_version", oauth_version);

      // get elements in alphabetical order
      SortedSet<String> keys = new TreeSet<String>(map.keySet());
      for (String key : keys) {
        try {
          parameter_string += URLEncoder.encode(key, "UTF-8")
              + "=" + URLEncoder.encode(map.get(key), "UTF-8");
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      }

      String signature_base = null;
      try {
        signature_base = method + "&" + URLEncoder.encode(tweetEndpoint, "UTF-8")
            + "&" + URLEncoder.encode(parameter_string, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }

      // build signing key
      final String CONSUMER_SECRET = "syU7IvNeuonSD6p77uEkySXWS6NVJimvWIbJxrYGBF3l2YAVAD";
      final String OAUTH_TOKEN_SECRET = "FTIVM7yg2EVNQzkSaylJIkxINdd4tR7YdgikLQ54PAwEu";
      String signing_key = CONSUMER_SECRET + "&" + OAUTH_TOKEN_SECRET;

      // generate signature
      oauth_signature = Base64.encode(HmacUtils.hmacSha1(signing_key, signature_base), Base64.NO_WRAP).toString();
    }

    // encode credentials
    String ENCODED_CREDENTIALS = "OAuth "
        + "oauth_consumer_key=\"" + oauth_consumer_key + "\", "
        + "oauth_nonce=\"" + oauth_nonce + "\", "
        + "oauth_signature=\"" + oauth_signature + "\", "
        + "oauth_signature_method=\"" + oauth_signature_method + "\", "
        + "oauth_timestamp=\"" + oauth_timestamp + "\", "
        + "oauth_token=\"" + oauth_token + "\", "
        + "oauth_version=\"" + oauth_version + "\"";

    // GET TWEET
    String response = "";

    try {
      URL obj = new URL(tweetEndpoint);
      HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
      con.setRequestMethod("POST");
      con.setRequestProperty("Authorization", ENCODED_CREDENTIALS);
      con.setRequestProperty("Host", "api.twitter.com");
      con.setRequestProperty("User-Agent", "GibFYP");
      con.setRequestProperty("Accept-Encoding", "gzip");
      con.setDoOutput(true);
      con.setDoInput(true);

      // write to body of connection
      BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
      wr.write(q);
      wr.flush();
      wr.close();

      // get response code
      int responseCode = con.getResponseCode();

      // read response from connection
      StringBuilder str = new StringBuilder();
      BufferedReader br = new BufferedReader(
          new InputStreamReader(
              new GZIPInputStream(con.getInputStream())));
      String inputLine = "";
      while (true) {
        inputLine = br.readLine();
        Log.d("UNIQUE_TAG", inputLine);
      }
//      inputLine = str.toString();

      // save response
//      response = inputLine;
//      Log.d("UNIQUE_TAG", response);
    } catch (Exception e) {
      Log.d("UNIQUE_TAG", e.toString());
    }
  }

  private void updateNotification(int NOTIFICATION_ID, NotificationCompat.Builder mNotifyBuilder) {
    NotificationManager mNotificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    // generate formatted date
    SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy H:mm:ss a");
    String date = sdf.format(new Date());

    mNotifyBuilder.setContentText("Last pulled: " + date);
    mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
  }


  /**
   * @return
   * @throws IOException
   */

  private String getAppOAuth() throws IOException {
    String OAuthTokenFilePath = ROOT_DIR + "OAuthToken.txt";
    File file = new File(OAuthTokenFilePath);

    // check if file exists
    if (!file.exists()) {
      initAppOAuth(OAuthTokenFilePath);
    }

    return FileUtils.readFileToString(new File(OAuthTokenFilePath));
  }

  private void initAppOAuth(String path) {
    Log.d("UNIQUE_TAG", "initAppOAuth");
    // token information
    final String CONSUMER_KEY = "2LqkZ3BA9MUV2ZvFHMpESVnQn";
    final String CONSUMER_SECRET = "syU7IvNeuonSD6p77uEkySXWS6NVJimvWIbJxrYGBF3l2YAVAD";
    String ENCODED_CREDENTIALS = "";
    try {
      ENCODED_CREDENTIALS = "Basic " + Base64.encodeToString(
          (CONSUMER_KEY + ":" + CONSUMER_SECRET).getBytes("UTF-8")
          , Base64.NO_WRAP);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    // get token
    String bearerToken = "";

    try {
      URL obj = new URL("https://api.twitter.com/oauth2/token");
      HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
      con.setRequestMethod("POST");
      con.setRequestProperty("Authorization", ENCODED_CREDENTIALS);
      con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
      con.setRequestProperty("Host", "api.twitter.com");
      con.setRequestProperty("User-Agent", "GibFYP");
      //con.setRequestProperty("Accept-Encoding", "gzip");
      con.setDoOutput(true);
      con.setDoInput(true);
      con.setRequestProperty("Content-Length", "29");

      // write to body of connection
      BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
      wr.write("grant_type=client_credentials");
      wr.flush();
      wr.close();

      // get response code
      int responseCode = con.getResponseCode();

      // read response from connection
      StringBuilder str = new StringBuilder();
      BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine = "";
      while ((inputLine = br.readLine()) != null) {
        str.append(inputLine + System.getProperty("line.separator"));
      }
      inputLine = str.toString();

      // get token from json object
      JSONObject json = new JSONObject(inputLine);
      bearerToken = json.getString("access_token");
    } catch (Exception e) {
      Log.d("UNIQUE_TAG", e.toString());
    }


    // save oauth information to file
    if (!Strings.isNullOrEmpty(bearerToken)) {
      Log.d("UNIQUE_TAG", bearerToken);
      try {
        FileUtilsCustom.saveToFile(path, bearerToken, false);
      } catch (Exception e) {
      }
    }

  }
}
