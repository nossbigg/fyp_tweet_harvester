package com.nossbigg.htmlminder.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nossbigg.htmlminder.R;
import com.nossbigg.htmlminder.utils.HTMLWorkerSampleGenerator;
import com.nossbigg.htmlminder.controller.HTMLWorkerService;
import com.nossbigg.htmlminder.todelete.SampleService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // launches activity
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // request write permissions
    verifyStoragePermissions(this);

    // starts hardcoded service
//    startService();

    // starts AbstractHTMLWorker service
    startHTMLWorkerService();

    // bind listener for button
    Button generateWorkerButton = (Button) findViewById(R.id.button);
    generateWorkerButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        generateWorkers();
      }
    });

    // close app by going to homescreen
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

  private void startHTMLWorkerService(){
    // declare intent
    Intent mServiceIntent = new Intent(this, HTMLWorkerService.class);

    // add root directory to intent
    mServiceIntent.putExtra("appDirectory", getAppDirectory());
    // FIXME
    mServiceIntent.putExtra("appDirectory",
        ContextCompat.getExternalFilesDirs(this,null)[1] + "/HTMLMinder/");

    // start service
    this.startService(mServiceIntent);

    // notify user
    Toast.makeText(getApplicationContext(), "Starting HTMLWorker service...", Toast.LENGTH_SHORT).show();
  }

  private void generateWorkers(){
    HTMLWorkerSampleGenerator htmlWorkerSampleGenerator = new HTMLWorkerSampleGenerator();
    htmlWorkerSampleGenerator.generateSampleWorkers();
//    htmlWorkerSampleGenerator.readWorkers();
  }

  /**
   * Redirects user to homescreen
   */
  private void goToHome(){
    Intent startMain = new Intent(Intent.ACTION_MAIN);
    startMain.addCategory(Intent.CATEGORY_HOME);
    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(startMain);
  }

  private static String getAppDirectory() {
    // TODO add detection for suitable places to store files
    // try save into base folders, if not then just use full paths suggested by android
    return Environment.getExternalStorageDirectory().getAbsolutePath()
        + "/HTMLMinder/";
  }

  // Storage Permissions
  private static final int REQUEST_EXTERNAL_STORAGE = 1;
  private static String[] PERMISSIONS_STORAGE = {
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE
  };
  private void verifyStoragePermissions(Activity activity) {
    // Check if we have write permission
    int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    if (permission != PackageManager.PERMISSION_GRANTED) {
      // We don't have permission so prompt the user
      ActivityCompat.requestPermissions(
          activity,
          PERMISSIONS_STORAGE,
          REQUEST_EXTERNAL_STORAGE
      );
    }

    // check if marshmallow (permissions)
    if(Build.VERSION.SDK_INT> Build.VERSION_CODES.LOLLIPOP_MR1){
      String[] perms = {"android.permission. WRITE_EXTERNAL_STORAGE"};
      int permsRequestCode = 200;
      requestPermissions(perms, permsRequestCode);
    }
  }

  @Override
  public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
    switch(permsRequestCode){
      case 200:
        boolean writeAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
        break;
    }
  }
}
