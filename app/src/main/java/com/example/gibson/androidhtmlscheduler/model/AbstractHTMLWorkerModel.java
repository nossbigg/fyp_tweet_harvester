package com.example.gibson.androidhtmlscheduler.model;

import com.example.gibson.androidhtmlscheduler.controller.AbstractHTMLWorker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gibson on 8/29/2016.
 */
public abstract class AbstractHTMLWorkerModel {
  public HTMLWorkerType htmlWorkerType = null;
  public String workerName = "";
  public List<HTMLSubWorkerModel> subWorkers = new ArrayList<>();
  public String description = "";

  // determines if tasks are to be done sequentially + batch
  public boolean isBatch;

  public AbstractHTMLWorkerModel(HTMLWorkerType workerType, String workerName) {
    this.htmlWorkerType = workerType;
    this.workerName = (workerName != null) ? workerName : "";
    isBatch = false;
  }

  public AbstractHTMLWorkerModel(AbstractHTMLWorkerModel a) {
    this.htmlWorkerType = a.htmlWorkerType;
    this.workerName = (a.workerName != null) ? a.workerName : workerName;

    // deep copy subworkers
    if(a.subWorkers != null){
      this.subWorkers = new ArrayList<>();
      for(HTMLSubWorkerModel subWorkerModel : a.subWorkers){
        this.subWorkers.add(new HTMLSubWorkerModel(subWorkerModel));
      }
    }
    this.description = (a.description != null) ? a.description : description;
    this.isBatch = a.isBatch;
  }
}
