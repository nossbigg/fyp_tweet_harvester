package com.nossbigg.htmlminder.model;

/**
 * Created by Gibson on 8/29/2016.
 */
public class TweetHTMLWorker extends AbstractHTMLWorker{
  public String CONSUMER_KEY = "";
  public String CONSUMER_SECRET = "";
  public String OAUTH_TOKEN = "";

  public TweetHTMLWorker(String CONSUMER_KEY, String CONSUMER_SECRET, String workerName) {
    super(HTMLWorkerType.TWEET, workerName);
    this.CONSUMER_KEY = CONSUMER_KEY;
    this.CONSUMER_SECRET = CONSUMER_SECRET;
  }
}
