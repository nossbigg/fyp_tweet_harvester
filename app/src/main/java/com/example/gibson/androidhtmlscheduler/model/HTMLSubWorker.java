package com.example.gibson.androidhtmlscheduler.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gibson on 8/29/2016.
 */
public class HTMLSubWorker {
  public String subWorkerName = "";
  public String url = "";
  public String method = "";
  public int interval = 0;
  public int intervalRandomVariance = 0;
  public Map<String, String> parameters;

  public HTMLSubWorker(String subWorkerName, String url, String method, int interval, int intervalRandomVariance) {
    this.subWorkerName = subWorkerName;
    this.url = url;
    this.method = method;
    this.interval = interval;
    this.intervalRandomVariance = intervalRandomVariance;
    parameters = new HashMap<>();
  }
}
