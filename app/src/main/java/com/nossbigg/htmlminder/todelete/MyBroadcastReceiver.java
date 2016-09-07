package com.nossbigg.htmlminder.todelete;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Gibson on 8/12/2016.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {
  public void onReceive(Context context, Intent intent) {
    Log.d("UNIQUE_TAG", "inside MyBroadcastReceiver");
    String response = intent.getStringExtra("response");
    Toast.makeText(context, "received the Intent's message: " + response, Toast.LENGTH_LONG).show();
    Log.d("UNIQUE_TAG", response);
  }
}
