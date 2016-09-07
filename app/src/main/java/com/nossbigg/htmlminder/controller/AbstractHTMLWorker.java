package com.nossbigg.htmlminder.controller;

import com.nossbigg.htmlminder.model.AbstractHTMLWorkerModel;
import com.nossbigg.htmlminder.utils.HTMLWorkerUtils;

import java.util.HashMap;

/**
 * Created by Gibson on 9/6/2016.
 */
public abstract class AbstractHTMLWorker {
  public AbstractHTMLWorkerModel abstractHTMLWorkerModel;
  public Runnable masterWorker;
  public HashMap<String, HTMLSubWorker> namesToWorkersMap = new HashMap<>();
  public int notificationID;
  public String workerDirectory = "";

  public AbstractHTMLWorker(AbstractHTMLWorkerModel abstractHTMLWorkerModel,
                            String appDirectory) {
    this.abstractHTMLWorkerModel = abstractHTMLWorkerModel;
    notificationID = HTMLWorkerUtils.WorkerNameToNotificationID(abstractHTMLWorkerModel.workerName);
    workerDirectory = appDirectory + abstractHTMLWorkerModel.workerName + "/";
  }

  public abstract void startWorker();

  public abstract void stopWorker();
}
