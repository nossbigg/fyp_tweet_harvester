package com.example.gibson.androidhtmlscheduler.model;

/**
 * Created by Gibson on 8/29/2016.
 */
public class PlainHTMLWorkerModel extends AbstractHTMLWorkerModel {
  public PlainHTMLWorkerModel(String workerName) {
    super(HTMLWorkerType.PLAIN, workerName);
  }

  public PlainHTMLWorkerModel(PlainHTMLWorkerModel p) {
    super(p);
  }

}
