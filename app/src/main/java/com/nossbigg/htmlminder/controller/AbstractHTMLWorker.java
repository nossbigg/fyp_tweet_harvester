package com.nossbigg.htmlminder.controller;

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

  public AbstractHTMLWorker(AbstractHTMLWorkerModel abstractHTMLWorkerModel) {
    this.abstractHTMLWorkerModel = abstractHTMLWorkerModel;
    notificationID = HTMLWorkerUtils.WorkerNameToNotificationID(abstractHTMLWorkerModel.workerName);
  }

  public abstract void startWorker();

  public abstract void stopWorker();

  // TODO implement notification builder
}
