package com.nossbigg.htmlminder.utils;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Gibson on 9/8/2016.
 */
public class TwitterAPIUtils {
  public static String getOAuthBearerToken(String CONSUMER_KEY,
                                           String CONSUMER_SECRET) throws IOException, JSONException {
    // build credentials
    String ENCODED_CREDENTIALS = "Basic " +
        Base64.encodeToString(
            (CONSUMER_KEY + ":" + CONSUMER_SECRET).getBytes("UTF-8")
            , Base64.NO_WRAP);

    // make url call
    URL obj = new URL("https://api.twitter.com/oauth2/token");
    HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
    con.setRequestMethod("POST");
    con.setRequestProperty("Authorization", ENCODED_CREDENTIALS);
    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
    con.setRequestProperty("Host", "api.twitter.com");
    con.setRequestProperty("User-Agent", "GibFYP");
    con.setRequestProperty("Accept-Encoding", "gzip");
    con.setDoOutput(true);
    con.setRequestProperty("Content-Length", "29");

    // write to body of connection
    BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
    wr.write("grant_type=client_credentials");
    wr.flush();
    wr.close();

    // get response code
    int responseCode = con.getResponseCode();
    if (responseCode != HttpsURLConnection.HTTP_OK) throw new IOException("");

    // read response from connection (using gzip)
    StringBuilder response = new StringBuilder();
    BufferedReader br = new BufferedReader(
        new InputStreamReader(
            new GZIPInputStream(con.getInputStream())));
    String inputLine = "";
    while ((inputLine = br.readLine()) != null) {
      response.append(inputLine + System.getProperty("line.separator"));
    }

    // get token from json object
    JSONObject jsonResponse = new JSONObject(response.toString());
    return jsonResponse.getString("access_token");
  }

}
