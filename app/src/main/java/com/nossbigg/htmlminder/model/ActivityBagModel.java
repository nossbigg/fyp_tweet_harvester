package com.nossbigg.htmlminder.model;

import com.nossbigg.htmlminder.controller.ConfigService;
import com.nossbigg.htmlminder.controller.LocalFileService;

import java.io.Serializable;

/**
 * Created by Gibson on 9/9/2016.
 */
public class ActivityBagModel implements Serializable{
  public LocalFileService localFileService;
  public ConfigService configService;
}
