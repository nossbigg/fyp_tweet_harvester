package com.example.gibson.androidhtmlscheduler.model;

/**
 * Created by Gibson on 8/29/2016.
 */
public class TweetHTMLWorkerModel extends AbstractHTMLWorkerModel {
  public String CONSUMER_KEY = "";
  public String CONSUMER_SECRET = "";
  public String OAUTH_TOKEN = "";

  public TweetHTMLWorkerModel(String CONSUMER_KEY, String CONSUMER_SECRET, String workerName) {
    super(HTMLWorkerType.TWEET, workerName);
    this.CONSUMER_KEY = CONSUMER_KEY;
    this.CONSUMER_SECRET = CONSUMER_SECRET;
  }

  public TweetHTMLWorkerModel(TweetHTMLWorkerModel t) {
    super(t);
    CONSUMER_KEY = (t.CONSUMER_KEY != null) ? t.CONSUMER_KEY : CONSUMER_KEY;
    CONSUMER_SECRET = (t.CONSUMER_SECRET != null) ? t.CONSUMER_SECRET : CONSUMER_SECRET;
    OAUTH_TOKEN = (t.OAUTH_TOKEN != null) ? t.OAUTH_TOKEN : OAUTH_TOKEN;
  }
}
