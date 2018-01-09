package com.prototype.jarvia.ver4;

import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Joon.Y.K on 2017-04-17.
 */

public class WikiResponse {

    private String retStr = "검색 결과가 없습니다. ";

    public String getRetStr(){
        //reset returning string each time, to prevent recurrance
        String ret = retStr;
        retStr = "검색 결과가 없습니다. ";
        return ret;
    }


    public void wikiResponseExec(String searchKeyword){

        String url = "https://ko.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exsentences=2&exintro=&explaintext=&redirects&titles=" + searchKeyword;

        ReceiveWikiTask wiki = new ReceiveWikiTask();
        AsyncTaskCompat.executeParallel(wiki, url);

    }

    private class ReceiveWikiTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... datas){
            try{

                // for debug worker thread
                if(android.os.Debug.isDebuggerConnected())
                    android.os.Debug.waitForDebugger();

                // create the file with the given file path
                File file = new File(datas[0]);
                HttpURLConnection conn = (HttpURLConnection) new URL(datas[0]).openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.connect();

                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    InputStream is = conn.getInputStream();
                    InputStreamReader reader = new InputStreamReader(is);
                    BufferedReader in = new BufferedReader(reader);

                    String readed;
                    while((readed = in.readLine()) != null){
                        JSONObject jObject = new JSONObject(readed);
                        //jObject.getJSONArray("weather").getJSONObject(0).getString("icon");

                        return jObject;
                    }
                } else{
                    return null;
                }
                return null;
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result){
            if(result != null){

                String extract = "";

                try{
              //iconName = result.getJSONArray("weather").getJSONObject(0).getString("icon");
               //   sentence = result.getJSONObject("query").getJSONObject("pages").getJSONObject("21721040").getString("extract");
                  JSONArray pages = result.getJSONObject("query").getJSONObject("pages").names();
                    extract = result.getJSONObject("query").getJSONObject("pages").getJSONObject(pages.get(0).toString()).getString("extract");
                    retStr = extract;
                    if(retStr.equals("")){
                        retStr = "검색 결과가 없습니다.";
                    }

                } catch (JSONException e){
                    e.printStackTrace();
                }



            }
        }


    } // end of class ReceiveWeather Task

}
