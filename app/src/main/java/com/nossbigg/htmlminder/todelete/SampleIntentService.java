//package com.nossbigg.htmlminder.todelete;
//
//import android.app.IntentService;
//import android.content.Intent;
//import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
//
///**
// * Created by Gibson on 8/12/2016.
// */
//public class SampleIntentService extends IntentService {
//  SampleIntentService that = this;
//
//  public SampleIntentService() {
//    super("SampleIntentService");
//  }
//
//  @Override
//  protected void onHandleIntent(Intent intent) {
//    String key = intent.getStringExtra("key");
//    Toast.makeText(getApplicationContext(), "In service...", Toast.LENGTH_SHORT).show();
//    Log.d("UNIQUE_TAG", key);
//    getWebappString();
//  }
//
//  private String getWebappString() {
//    String result = "";
//
//    // Instantiate the RequestQueue.
//    RequestQueue queue = Volley.newRequestQueue(this);
//    String url = "http://campusbus.ntu.edu.sg/ntubus/index.php/main/getCurrentPosition/";
////    String url = "http://www.google.com";
//
//    // Request a string response from the provided URL.
//    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//        new Response.Listener<String>() {
//          @Override
//          public void onResponse(String response) {
//            // Display the first 500 characters of the response string.
////                        mTextView.setText("Response is: " + response.substring(0, 500));
//            Intent localIntent = new Intent("customIntentBroadcast");
//            localIntent.setAction("customIntentBroadcast");
//            localIntent.putExtra("response", response);
//            LocalBroadcastManager.getInstance(that).sendBroadcast(localIntent);
//            Log.d("UNIQUE_TAG", "Response: " + response);
//          }
//        }, new Response.ErrorListener() {
//      @Override
//      public void onErrorResponse(VolleyError error) {
////                mTextView.setText("That didn't work!");
//        Log.d("UNIQUE_TAG", "volley error");
//      }
//    });
//
//    // Add the request to the RequestQueue.
//    queue.add(stringRequest);
//
//    return result;
//  }
//}
