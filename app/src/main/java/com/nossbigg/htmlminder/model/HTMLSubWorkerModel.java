package com.nossbigg.htmlminder.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gibson on 8/29/2016.
 */
public class HTMLSubWorkerModel {
  public String subWorkerName = "";
  public String description = "";
  public String url = "";
  public String method = "";
  public long interval = 0;
  public long intervalRandomVariance = 0;
  public HashMap<String, String> parameters = new HashMap<>();
  public String checkFieldsNotEmpty = "";

  // paths data save dir
  // for runtime only, will not be persisted
  public transient String dataSaveDir = "";

  public HTMLSubWorkerModel(String subWorkerName, String url, String method, long interval, long intervalRandomVariance) {
    this.subWorkerName = subWorkerName;
    this.url = url;
    this.method = method;
    this.interval = interval;
    this.intervalRandomVariance = intervalRandomVariance;
  }

  public HTMLSubWorkerModel(HTMLSubWorkerModel h){
    subWorkerName = (h.subWorkerName != null) ? h.subWorkerName : subWorkerName ;
    description = (h.description != null) ? h.description : description;
    url = (h.url != null) ? h.url : url;
    method = (h.method != null) ? h.method : method;
    interval = h.interval;
    intervalRandomVariance = h.intervalRandomVariance;
    parameters = (h.parameters != null) ? h.parameters : parameters;
    checkFieldsNotEmpty = (h.checkFieldsNotEmpty != null) ? h.checkFieldsNotEmpty : checkFieldsNotEmpty;
  }
}
