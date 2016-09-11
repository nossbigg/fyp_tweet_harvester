package com.nossbigg.htmlminder.controller;

import android.os.Handler;

import com.nossbigg.htmlminder.model.HTMLSubWorkerModel;
import com.nossbigg.htmlminder.model.HTMLWorkerNotificationModel;

/**
 * Created by Gibson on 9/6/2016.
 */
public class HTMLSubWorker {
  public HTMLSubWorkerModel htmlSubWorkerModel;

  public Handler handler;
  public Runnable runnable;

  // Stores notification object
  public HTMLWorkerNotificationModel notificationModel
      = new HTMLWorkerNotificationModel();
  // TODO move dataSaveDir from model to here (don't wanna pollute the Model)

  public HTMLSubWorker(HTMLSubWorkerModel htmlSubWorkerModel){
    this.htmlSubWorkerModel = htmlSubWorkerModel;
  }

  public HTMLSubWorker(HTMLSubWorkerModel htmlSubWorkerModel, Handler handler){
    this.htmlSubWorkerModel = htmlSubWorkerModel;
    this.handler = handler;
  }

  public void assignRunnable(Runnable runnable){
    this.runnable = runnable;
  }
}
