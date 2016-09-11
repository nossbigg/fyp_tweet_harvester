package com.nossbigg.htmlminder.controller;

import android.support.v4.app.NotificationCompat;

import com.nossbigg.htmlminder.model.AbstractHTMLWorkerModel;
import com.nossbigg.htmlminder.utils.HTMLWorkerUtils;

import java.util.HashMap;

/**
 * Created by Gibson on 9/6/2016.
 */
public abstract class AbstractHTMLWorker {
  public AbstractHTMLWorkerModel abstractHTMLWorkerModel;
  public HashMap<String, HTMLSubWorker> namesToWorkersMap = new HashMap<>();
  public int notificationID;

  // Store notification builder reference
  public NotificationCompat.Builder notificationBuilder;

  public AbstractHTMLWorker(AbstractHTMLWorkerModel abstractHTMLWorkerModel) {
    this.abstractHTMLWorkerModel = abstractHTMLWorkerModel;
    notificationID = HTMLWorkerUtils.generateWorkerNameToNotificationID(abstractHTMLWorkerModel.workerName);
  }

  public abstract void startWorker();

  public abstract void stopWorker();

  public abstract String getNotificationInfo();
}
