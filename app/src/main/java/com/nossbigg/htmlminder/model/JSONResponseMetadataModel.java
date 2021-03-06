package com.nossbigg.htmlminder.model;

import java.util.HashMap;

/**
 * Created by Gibson on 9/10/2016.
 */
public class JSONResponseMetadataModel {
  public long timestamp_received_epoch = 0L;
  public String query_url = "";
  public String query_method = "";
  public HashMap<String, String> parameters = new HashMap<>();

  public JSONResponseMetadataModel(long timestamp_received_epoch,
                                   HTMLSubWorkerModel subWorker) {
    this.timestamp_received_epoch = timestamp_received_epoch;
    this.query_url = subWorker.url;
    this.query_method = subWorker.method;
    this.parameters = subWorker.parameters;
  }
}
