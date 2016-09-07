package com.nossbigg.htmlminder.utils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Gibson on 8/30/2016.
 */
public class HTMLWorkerUtils {
  public static String AddParamsToURL(String url, Map<String, String> params) {
    // if empty parameters, return
    if (params.size() == 0) {
      return url;
    }

    StringBuilder sb = new StringBuilder(url);
    sb.append("?");

    int paramsCount = params.size();
    int paramIndex = 0;
    for (Map.Entry<String, String> entry : params.entrySet()) {
      try {
        // add param to url
        sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
        sb.append("=");
        sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));

        // increments index
        paramIndex++;

        // add ampersand if not last element
        if (paramIndex != paramsCount) {
          sb.append("&");
        }
      } catch (UnsupportedEncodingException e) {
        // skip entry
      }
    }

    return sb.toString();
  }

  public static int WorkerNameToNotificationID(String workerName) {
    int result = 0;
    for (char c : workerName.toCharArray()) {
      result += Character.getNumericValue(c);
    }

    return Integer.parseInt(result + "" + ((int) Math.random() * 100000));
  }

  public static boolean isAnyFieldsEmpty(String... fields) {
    for (String s : fields) {
      if (StringUtils.isEmpty(s)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Assumption: If an array exists, it can only exist at the last element
   * Note: Can consider future jsonarray traversal
   *
   * @param jo
   * @param fieldsCommaDelimited
   * @return
   */
  public static boolean isJsonFieldsEmpty(JSONObject jo, String fieldsCommaDelimited) {
    fieldsCommaDelimited = fieldsCommaDelimited.replaceAll(" ", "");

    HashSet<String> fieldList = new HashSet<>(Arrays.asList(fieldsCommaDelimited.split(",")));

    for (String field : fieldList) {
      JSONObject lastObject = jo;

      ArrayList<String> terms = new ArrayList<>(Arrays.asList(field.split("\\.")));
      int size = terms.size();
      int indexLastElement = size - 1;
      for (int index = 0; index < size; index++) {
        String term = terms.get(index);
        //boolean isArrayAccessor = term.matches("^(?:[\\w]+)((\\[[\\d]+\\])+)$");

        Object o = new Object();

        try {
          o = lastObject.get(term);
        } catch (JSONException e) {
          // no such term, failed
          return false;
        }

        // check if last object
        if (index == indexLastElement) {
          if (o instanceof JSONArray) {
            return ((JSONArray) o).length() == 0;
          } else if (o instanceof String) {
            return (StringUtils.isEmpty((String) o));
          } else {
            // not quite sure what to do with this edge case...
          }
        } else {
          // not supposed to end so quickly
          if (!(o instanceof JSONObject)) return false;

          // move to next object
          lastObject = (JSONObject) o;
        }
      }
    }

    return true;
  }

  public static boolean isJsonObject(String jsonString) {
    try {
      JSONObject jo = new JSONObject(jsonString);
    } catch (JSONException e) {
      return false;
    }
    return true;
  }

  public static String getCurrentDateInFormat(String format) {
    SimpleDateFormat sdfDate = new SimpleDateFormat(format);
    Date now = new Date();
    return sdfDate.format(now);
  }

  // TODO design method to return basic connection
//  public static HttpsURLConnection prepareBasicConnection() {
//
//  }
}
