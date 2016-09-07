package com.example.gibson.androidhtmlscheduler.controller;

import com.example.gibson.androidhtmlscheduler.model.TweetHTMLWorkerModel;

/**
 * Created by Gibson on 9/6/2016.
 */
public class TweetHTMLWorker extends AbstractHTMLWorker {
  public TweetHTMLWorkerModel workerModel;

  public TweetHTMLWorker(TweetHTMLWorkerModel tweetHTMLWorkerModel, String appDirectory) {
    super(tweetHTMLWorkerModel, appDirectory);
    workerModel = tweetHTMLWorkerModel;
  }

  @Override
  public void startWorker() {

  }

  @Override
  public void stopWorker() {

  }

//  private String getAppOAuth() throws IOException {
//    String OAuthTokenFilePath = HTMLWorkerService.ROOT_DIR + "OAuthToken.txt";
//    File file = new File(OAuthTokenFilePath);
//
//    // check if file exists
//    if (!file.exists()) {
//      initAppOAuth(OAuthTokenFilePath);
//    }
//
//    return FileUtilsCustom.readfile(OAuthTokenFilePath);
//  }
//
//  private void initAppOAuth(String path) {
//    Log.d("UNIQUE_TAG", "initAppOAuth");
//    // token information
//    final String CONSUMER_KEY = "2LqkZ3BA9MUV2ZvFHMpESVnQn";
//    final String CONSUMER_SECRET = "syU7IvNeuonSD6p77uEkySXWS6NVJimvWIbJxrYGBF3l2YAVAD";
//    String ENCODED_CREDENTIALS = "";
//    try {
//      ENCODED_CREDENTIALS = "Basic " + Base64.encodeToString(
//          (CONSUMER_KEY + ":" + CONSUMER_SECRET).getBytes("UTF-8")
//          , Base64.NO_WRAP);
//    } catch (UnsupportedEncodingException e) {
//      e.printStackTrace();
//    }
//
//    // get token
//    String bearerToken = "";
//
//    try {
//      URL obj = new URL("https://api.twitter.com/oauth2/token");
//      HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
//      con.setRequestMethod("POST");
//      con.setRequestProperty("Authorization", ENCODED_CREDENTIALS);
//      con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
//      con.setRequestProperty("Host", "api.twitter.com");
//      con.setRequestProperty("User-Agent", "GibFYP");
//      //con.setRequestProperty("Accept-Encoding", "gzip");
//      con.setDoOutput(true);
//      con.setDoInput(true);
//      con.setRequestProperty("Content-Length", "29");
//
//      // write to body of connection
//      BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
//      wr.write("grant_type=client_credentials");
//      wr.flush();
//      wr.close();
//
//      // get response code
//      int responseCode = con.getResponseCode();
//
//      // read response from connection
//      StringBuilder str = new StringBuilder();
//      BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
//      String inputLine = "";
//      while ((inputLine = br.readLine()) != null) {
//        str.append(inputLine + System.getProperty("line.separator"));
//      }
//      inputLine = str.toString();
//
//      // get token from json object
//      JSONObject json = new JSONObject(inputLine);
//      bearerToken = json.getString("access_token");
//    } catch (Exception e) {
//      Log.d("UNIQUE_TAG", e.toString());
//    }
//
//
//    // save oauth information to file
//    if (!Strings.isNullOrEmpty(bearerToken)) {
//      Log.d("UNIQUE_TAG", bearerToken);
//      try {
//        FileUtilsCustom.saveToFile(path, bearerToken);
//      } catch (Exception e) {
//      }
//    }
//
//  }
}
