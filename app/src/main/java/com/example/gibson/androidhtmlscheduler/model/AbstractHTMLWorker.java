package com.example.gibson.androidhtmlscheduler.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gibson on 8/29/2016.
 */
public abstract class AbstractHTMLWorker {
  public HTMLWorkerType htmlWorkerType;
  public String workerName;
  public List<HTMLSubWorker> subWorkers;

  // determines if tasks are to be done sequentially + batch
  public boolean isBatch;

  public AbstractHTMLWorker(HTMLWorkerType workerType, String workerName) {
    this.htmlWorkerType = workerType;
    this.workerName = workerName;
    subWorkers = new ArrayList<>();
    isBatch = false;
  }
}
