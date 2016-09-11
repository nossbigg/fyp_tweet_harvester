package com.nossbigg.htmlminder.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.nossbigg.htmlminder.R;
import com.nossbigg.htmlminder.model.AbstractHTMLWorkerModel;
import com.nossbigg.htmlminder.model.ActivityBagModel;
import com.nossbigg.htmlminder.model.HTMLSubWorkerModel;
import com.nossbigg.htmlminder.model.PlainHTMLWorkerModel;
import com.nossbigg.htmlminder.model.TweetHTMLWorkerModel;
import com.nossbigg.htmlminder.utils.HTMLWorkerModelUtils;
import com.nossbigg.htmlminder.view.MainActivity;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Gibson on 8/31/2016.
 */
public class HTMLWorkerService extends Service {
  // Constants
  private PowerManager.WakeLock wakeLock;

  // Variables
  public HashMap<String, AbstractHTMLWorker> HTMLWorkersHashMap = new HashMap<>();
  public String appDirectory = "";
  public ActivityBagModel activityBagModel;

  @Override
  public void onCreate() {
    super.onCreate();

    // set wakelock
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNjfdhotDimScreen");
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startid) {
    Log.d("UNIQUE_TAG", "Starting HTMLWorkerService..");

    // load reference to activityBagModel
    activityBagModel = (ActivityBagModel) intent.getSerializableExtra("activityBagModel");

    // load appDirectory
    appDirectory = activityBagModel.localFileService.appDir;

    // create directory (if does not exist)
    File appDirectoryFile = new File(appDirectory);
    if (!appDirectoryFile.exists()) {
      try {
        FileUtils.forceMkdir(appDirectoryFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    init();

    return START_NOT_STICKY;
  }

  public void init() {
    // Get all worker objects from json files
    List<AbstractHTMLWorkerModel> abstractHTMLWorkerModelList
        = getWorkerModels(appDirectory);

    //
    for (AbstractHTMLWorkerModel abstractHTMLWorkerModel : abstractHTMLWorkerModelList) {
      try {
        // init worker
        AbstractHTMLWorker abstractHTMLWorker = initWorker(abstractHTMLWorkerModel);
        // add worker to list
        HTMLWorkersHashMap.put(abstractHTMLWorker.abstractHTMLWorkerModel.workerName, abstractHTMLWorker);
        // start worker
        abstractHTMLWorker.startWorker();
      } catch (IOException e) {
        // bad worker model
      }
    }

    // start notifications
    startNotifications(HTMLWorkersHashMap);

    // make worker json files neat + repair
    HTMLWorkerModelUtils.MakeAllJsonWorkerModelsPretty(
        activityBagModel.localFileService.getWorkerConfigsDir());
  }

  @Override
  public void onDestroy() {
    Log.d("UNIQUE_TAG", "Stopping HTMLWorkerService..");

    // stop as foregrounnd service
    stopForeground(true);

    // stop notifications
    stopNotifications();

    for (AbstractHTMLWorker worker : HTMLWorkersHashMap.values()) {
      // stop all abstractworkers
      worker.stopWorker();
    }
  }


  public AbstractHTMLWorker initWorker(AbstractHTMLWorkerModel abstractHTMLWorkerModel) throws IOException {
    AbstractHTMLWorker abstractHTMLWorker;
    Log.d("UNIQUE_TAG", abstractHTMLWorkerModel.workerName);

    // create worker object
    switch (abstractHTMLWorkerModel.htmlWorkerType) {
      case PLAIN: {
        abstractHTMLWorker = new PlainHTMLWorker((PlainHTMLWorkerModel) abstractHTMLWorkerModel);
        break;
      }
      case TWEET: {
        abstractHTMLWorker = new TweetHTMLWorker((TweetHTMLWorkerModel) abstractHTMLWorkerModel);
        break;
      }
      default: {
        // unknown type
        throw new IOException("Invalid worker type: " + abstractHTMLWorkerModel.htmlWorkerType);
      }
    }

    return abstractHTMLWorker;
  }

  public List<AbstractHTMLWorkerModel> getWorkerModels(String appDirectory) {
    String workersDirectory = activityBagModel.localFileService.getWorkerConfigsDir();
    ;

    List<AbstractHTMLWorkerModel> workerModels = HTMLWorkerModelUtils.getWorkerModelsFromDirectory(workersDirectory);

    // ADDITIONAL VARIABLES
    // saves data save directories to each worker and subworker
    for (AbstractHTMLWorkerModel abstractHTMLWorkerModel : workerModels) {
      // saves worker data save dir
      abstractHTMLWorkerModel.dataSaveDir =
          activityBagModel.localFileService.getWorkerDataSaveDir(abstractHTMLWorkerModel);

      // save subworkers data save directories
      for (HTMLSubWorkerModel htmlSubWorkerModel : abstractHTMLWorkerModel.subWorkers) {
        htmlSubWorkerModel.dataSaveDir =
            activityBagModel.localFileService.getSubWorkerDataSaveDir(
                abstractHTMLWorkerModel, htmlSubWorkerModel);
      }
    }

    // return list of workers
    return workerModels;
  }

  // NOTIFICATIONS
  Handler notificationHandler = new Handler();
  Runnable notificationRunnable = null;
  NotificationManager nMgr = null;
  HashSet<Integer> existingNotifications = new HashSet<>();

  public void startNotifications(HashMap<String, AbstractHTMLWorker> HTMLWorkersHashMap) {
    if (nMgr == null) {
      nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }
    if (notificationRunnable == null) {
      notificationRunnable = initNotificationRunnable(notificationHandler, HTMLWorkersHashMap, 2000);
    }
    notificationHandler.post(notificationRunnable);
  }

  public void stopNotifications() {
    notificationHandler.removeCallbacks(notificationRunnable);

    // remove existing notifications
    for (Integer notificationId : existingNotifications) {
      removeNotification(notificationId);
    }
  }

  public Runnable initNotificationRunnable(final Handler handler,
                                           final HashMap<String, AbstractHTMLWorker> HTMLWorkersHashMap,
                                           final long interval) {
    return new Runnable() {
      @Override
      public void run() {
        // maintain list of notifications that have been interacted with
        HashSet<Integer> notificationsInteractedList = new HashSet<>();

        for (AbstractHTMLWorker worker : HTMLWorkersHashMap.values()) {
          int notificationId = worker.notificationID;

          // add to list of notifications interacted with
          notificationsInteractedList.add(notificationId);

          // get notification content
          String content = worker.getNotificationInfo();

          // create notification if not existing
          if (!existingNotifications.contains(notificationId)) {
            NotificationCompat.Builder builder = createNotification(
                notificationId, worker);
            // save builder reference
            worker.notificationBuilder = builder;
          } else {
            // notification exists, update
            updateNotification(notificationId, content, worker.notificationBuilder);
          }
        }

        // remove dangling tasks
        HashSet<Integer> danglingNotifications = new HashSet<>(existingNotifications);
        danglingNotifications.removeAll(notificationsInteractedList);
        for (Integer notificationID : danglingNotifications) {
          removeNotification(notificationID);
        }

        // save notifications to existing
        existingNotifications = notificationsInteractedList;

        // repeat task
        handler.postDelayed(this, interval);
      }
    };
  }

  public void removeNotification(int notificationID) {
    nMgr.cancel(notificationID);
  }

  public NotificationCompat.Builder createNotification(int notificationID
      , AbstractHTMLWorker worker) {
    String content = worker.getNotificationInfo();

    Intent notificationIntent = new Intent(this, MainActivity.class);
    PendingIntent pendingIntent =
        PendingIntent.getActivity(this, 0, notificationIntent, 0);

    final NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setContentTitle("HTMLMinder: " + worker.abstractHTMLWorkerModel.workerName)
            .setContentText(content)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(content));
    Notification notification = mBuilder.build();

    startForeground(notificationID, notification);
    return mBuilder;
  }

  public void updateNotification(int notificationID, String content, NotificationCompat.Builder notificationBuilder) {
    notificationBuilder.setContentText(content);
    notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(content));
    nMgr.notify(notificationID, notificationBuilder.build());
  }
}
