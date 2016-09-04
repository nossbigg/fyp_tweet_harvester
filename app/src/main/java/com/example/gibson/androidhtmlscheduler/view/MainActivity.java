package com.example.gibson.androidhtmlscheduler.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.gibson.androidhtmlscheduler.R;
import com.example.gibson.androidhtmlscheduler.controller.SampleService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    startService();

    // bind listener for button
    Button send = (Button) findViewById(R.id.button);
    send.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startService();
      }
    });

//    HTMLWorkerSampleGenerator htmlWorkerSampleGenerator = new HTMLWorkerSampleGenerator();
//    htmlWorkerSampleGenerator.generateSampleWorkers();
//    htmlWorkerSampleGenerator.readWorkers();

    // add broadcast receiver
//    IntentFilter mStatusIntentFilter = new IntentFilter("customIntentBroadcast");
//    MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
//    LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcastReceiver, mStatusIntentFilter);

    // go to home
    goToHome();

  }

  private void startService() {
    // declare intent
    Intent mServiceIntent = new Intent(this, SampleService.class);

    // add parameter to intent
    DateFormat dateFormate1 = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
    String date = dateFormate1.format(Calendar.getInstance().getTime());
    mServiceIntent.putExtra("key", date);

    // start service
    this.startService(mServiceIntent);

    // notify user
    Toast.makeText(getApplicationContext(), "Starting service...", Toast.LENGTH_SHORT).show();
  }

  private void goToHome(){
    Intent startMain = new Intent(Intent.ACTION_MAIN);
    startMain.addCategory(Intent.CATEGORY_HOME);
    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(startMain);
  }
}
