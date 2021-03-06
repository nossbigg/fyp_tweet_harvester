package com.nossbigg.htmlminder.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nossbigg.htmlminder.R;
import com.nossbigg.htmlminder.controller.ConfigService;
import com.nossbigg.htmlminder.controller.HTMLWorkerService;
import com.nossbigg.htmlminder.controller.LocalFileService;
import com.nossbigg.htmlminder.model.ActivityBagModel;
import com.nossbigg.htmlminder.utils.AppSampleJsonConfigGenerator;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

  Intent HTMLWorkerServiceIntent;

  static boolean isStartedActivity = false;
  ActivityBagModel activityBagModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // launches activity
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // if activity has started, do not re-init the program
    if (!isStartedActivity) {
      // create ActivityBagModel
      activityBagModel = new ActivityBagModel();

      // init program
      initProgram(activityBagModel);

      // update started activity flag
      isStartedActivity = true;
    }

    // bind listener for button
    Button generateWorkerButton = (Button) findViewById(R.id.restartServiceButton);
    generateWorkerButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        restartHTMLWorkerService();
      }
    });

    Button stopWorkerButton = (Button) findViewById(R.id.stopServiceButton);
    stopWorkerButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        stopHTMLWorkerService();
      }
    });

    // FIXME nullpointerexception when create again
    // update appDir label
    setDirDetailsLabel(activityBagModel.localFileService.appDir,
        activityBagModel.localFileService.appInitDir);

    // TODO add label for detailed listing of current htmlworkers

    // close app by going to homescreen
//    goToHome();
  }

  private void initProgram(ActivityBagModel activityBagModel) {
    try {
      activityBagModel.configService = new ConfigService(this);
    } catch (IOException e) {
      // something seriously wrong happened here! :(
    }
    activityBagModel.localFileService
        = new LocalFileService(activityBagModel.configService);

    // update appDir label
    setDirDetailsLabel(activityBagModel.localFileService.appDir,
        activityBagModel.localFileService.appInitDir);

    // starts AbstractHTMLWorker service
    startHTMLWorkerService(activityBagModel);
  }

  private void startHTMLWorkerService(ActivityBagModel activityBagModel) {
    // declare intent
    HTMLWorkerServiceIntent = new Intent(this, HTMLWorkerService.class);

    // add root directory to intent
    HTMLWorkerServiceIntent.putExtra("activityBagModel", activityBagModel);

    // start service
    this.startService(HTMLWorkerServiceIntent);

    // notify user
    Toast.makeText(getApplicationContext(), "Starting HTMLWorker service...", Toast.LENGTH_SHORT).show();
  }

  private void stopHTMLWorkerService() {
    this.stopService(HTMLWorkerServiceIntent);
  }

  private void restartHTMLWorkerService() {
    stopHTMLWorkerService();
    startHTMLWorkerService(activityBagModel);
  }

  private void generateWorkers() {
    AppSampleJsonConfigGenerator gen
        = new AppSampleJsonConfigGenerator(activityBagModel.localFileService.appDir);
    gen.generateSampleWorkers();
//    gen.readWorkers();
  }

  private void setDirDetailsLabel(String appDir, String appInitDir) {
    String info = "appDir: " + appDir + "\n" + "appInitDir: " + appInitDir;
    TextView t = (TextView) findViewById(R.id.dirDetails);
    t.setText(info);
  }

  /**
   * Redirects user to homescreen
   */
  private void goToHome() {
    Intent startMain = new Intent(Intent.ACTION_MAIN);
    startMain.addCategory(Intent.CATEGORY_HOME);
    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(startMain);
  }
}
