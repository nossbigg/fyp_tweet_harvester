package com.nossbigg.htmlminder.controller;

import android.os.Handler;

import com.nossbigg.htmlminder.model.HTMLSubWorkerModel;

/**
 * Created by Gibson on 9/6/2016.
 */
public class HTMLSubWorker {
  public HTMLSubWorkerModel htmlSubWorkerModel;

  public Handler handler;
  public Runnable runnable;

  public HTMLSubWorker(HTMLSubWorkerModel htmlSubWorkerModel, Handler handler, Runnable runnable){
    this.htmlSubWorkerModel = htmlSubWorkerModel;
    this.handler = handler;
    this.runnable = runnable;
  }
}
