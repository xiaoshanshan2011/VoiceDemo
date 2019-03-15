package com.itchenqi.voicelib.http;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * http 工具类
 */
public class HttpGetUtil {
    private static final String TAG = "HttpGetUtil";

    public void post(String url){
        new AsyncTask<String, Void, Integer>() {
            @Override
            protected Integer doInBackground(String... params) {
                InputStream inputStream = null;

                HttpURLConnection urlConnection = null;

                Integer result = 0;
                try {
                /* forming th java.net.URL object */
                    URL url = new URL(params[0]);

                    urlConnection = (HttpURLConnection) url.openConnection();

                 /* optional request header */
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                /* optional request header */

                    urlConnection.setRequestProperty("header_key", "alicloud_url_header");
                    urlConnection.setRequestProperty("Authorization", "APPCODE cd83d6d9b85449768f76fd23877a91aa");

                /* for Get request */
                    urlConnection.setRequestMethod("GET");

                    int statusCode = urlConnection.getResponseCode();

                /* 200 represents HTTP OK */
                    if (statusCode == 200) {

                        inputStream = new BufferedInputStream(urlConnection.getInputStream());

                        String response = convertInputStreamToString(inputStream);

                        parseResult(response);
                        result = 1; // Successful

                    } else {
                        result = 0; //"Failed to fetch data!";
                    }

                } catch (Exception e) {
                    Log.d(TAG, e.getLocalizedMessage());
                }

                return result; //"Failed to fetch data!";
            }

            @Override
            protected void onPostExecute(Integer result) {
                super.onPostExecute(result);
                /* Download complete. Lets update UI */
                if (result == 1 && statusCode.equals("0")) {
                    onSuccess(content);
                } else {
                    onError();
                }
            }
        }.execute(url);
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String line = "";
        String result = "";

        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

            /* Close Stream */
        if (null != inputStream) {
            inputStream.close();
        }

        return result;
    }

    private String statusCode;
    private String content;
    public void parseResult(String result) {
        statusCode = "1";
        try {
            JSONObject response = new JSONObject(result);
            statusCode = response.getString("status");
            JSONObject data = response.getJSONObject("result");
            content = data.getString("content");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onSuccess(String result){

    }
    public void onError(){

    }
}
