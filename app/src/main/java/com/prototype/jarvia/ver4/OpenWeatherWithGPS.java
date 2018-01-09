package com.prototype.jarvia.ver4;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.os.AsyncTaskCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.Context.LOCATION_SERVICE;


/**
 * Created by Joon.Y.K on 2017-03-27.
 */

public class OpenWeatherWithGPS {
    LocationManager locationManager;
    //현재 GPS 사용유무
    boolean isGPSEnabled = false;
    //네트워크 사용유무
    boolean isNetworkEnabled = false;
    //GPS 상태값
    boolean isGetLocation = false;
    Location location;

    double lat; // 위도
    double lng; // 경도

    //최소 GPS 정보 업데이트 거리 1000미터
    private static final long MIN_DISTANCE_CHANGE_FORUPDATES = 1000;

    //최소 업데이트 시간 1분
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    Context mContext;

    private String response;
    private String dustGrade;

    int corePoolSize = 60;
    int maximumPoolSize = 80;
    int keepAliveTime = 10;




    public OpenWeatherWithGPS(Context mContext) {
        this.mContext = mContext;
    }

    public String getResponse(){
        return response;
    }

    public Location getLocation(){
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            //GPS 정보 가져오기
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            //current network state
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            //For some reason, it detects my network is disabled, even if wifi is turned on
            if(!isGPSEnabled && !isNetworkEnabled){
                //if GPS and Network cannot be used, Default for Atlanta for now
                location.setLatitude(33.7489954);
                location.setLongitude(-84.3879824);
                lat = location.getLatitude();
                lng = location.getLongitude();
            } else {
                this.isGetLocation = true;
                //get location from network
                if(isNetworkEnabled){
                    try{
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FORUPDATES, locationListener);
                    // Security Exception Required
                } catch (SecurityException e)
                {
                    e.printStackTrace();
                }


                    if(locationManager != null){
                        try {
                       location =  locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        // Security Exception Required
                    } catch (SecurityException e)
                    {
                        e.printStackTrace();
                    }

                        if(location != null){
                            //save lat and long
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                        }
                    }
                }
                if(isGPSEnabled){
                    if(location == null){

                        try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FORUPDATES, locationListener);
                        // Security Exception Required
                    } catch (SecurityException e)
                    {
                        e.printStackTrace();
                    }

                        if(locationManager != null){
                            try{
                           location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                // Security Exception Required
                            } catch (SecurityException e)
                            {
                                e.printStackTrace();
                            }

                                if(location != null){
                                lat = location.getLatitude();
                                lng = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }
        return location;
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location){
            //Toast("onLocationChanged");
            getWeatherData(location.getLatitude(), location.getLongitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras){
            //Toast("onStatusChanged");
        }

        @Override
        public void onProviderEnabled(String provider){
            //Toast("onProviderEnabled");
        }

        @Override
        public void onProviderDisabled(String provider){
            //Toast("onProviderDisabled");
        }
    };

    public void getWeatherData(double lat, double lng){
        //Get Celsius Temp Weather
        String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lng + "&units=metric&appid=3376cfd36d634389d9fac1bedde3308c";
        String skDustUrl = "http://apis.skplanetx.com/weather/dust?version=1&lat=" + lat + "&lon=" + lng + "&appKey=5ed9ffbe-9483-39dd-923b-318c0eb5b799";

        //Execute DustTask. Weather task is executed in postExecute of this execution. So in sequence.
        ReceiveDustTask receiveDustTask = new ReceiveDustTask();
    //    ReceiveWeatherTask receiveWeatherTask = new ReceiveWeatherTask();


        AsyncTaskCompat.executeParallel(receiveDustTask, skDustUrl);
      //  AsyncTaskCompat.executeParallel(receiveWeatherTask, url);

    }

    private String transferKorWeather(String weather){
        weather = weather.toLowerCase();

        if(weather.equals("haze")){
            return "안개";
        }
        else if(weather.equals("fog")){
            return "안개";
        }
        else if(weather.equals("clouds")){
            return "구름";
        }
        else if(weather.equals("few clouds")){
            return "구름 조금";
        }
        else if(weather.equals("scattered clouds")){
            return "구름 낌";
        }
        else if(weather.equals("broken clouds")){
            return "구름 많음";
        }
        else if(weather.equals("overcast clouds")){
            return "구름 많음";
        }
        else if(weather.equals("clear sky")){
            return "맑음";
        }
        return "";
    }



    private class ReceiveWeatherTask extends AsyncTask<String, Void, JSONObject>{
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
                String iconName = "";
                String nowTemp = "";
                String maxTemp = "";
                String minTemp = "";

                String humidity = "";
                String speed = "";
                String main = "";
                String description = "";
                String cityName = "";

                try{
                    iconName = result.getJSONArray("weather").getJSONObject(0).getString("icon");
                    nowTemp = result.getJSONObject("main").getString("temp");
                    humidity = result.getJSONObject("main").getString("humidity");
                    minTemp = result.getJSONObject("main").getString("temp_min");
                    maxTemp = result.getJSONObject("main").getString("temp_max");
                    //speed = result.getJSONObject("main").getString("speed");
                    main = result.getJSONArray("weather").getJSONObject(0).getString("main");
                    cityName = result.getString("name");

                    description = result.getJSONArray("weather").getJSONObject(0).getString("description");
                } catch (JSONException e){
                    e.printStackTrace();
                }
                description = transferKorWeather(description);
                Float nowTempF = Float.parseFloat(nowTemp);
                nowTemp = String.format("%.1f", nowTempF);


                final String msg = description + " 습도" + humidity +"%, 풍속 " + speed + "m/s" + " 온도 현재:" + nowTemp + " / 최저:" + minTemp + " / 최고:" + maxTemp;
                response = "현재 계신 " + cityName + "의 날씨는 " + description + "이며 온도는 약 " + nowTemp + "도에 미세먼지 등급은 " + dustGrade + "입니다. ";
            }
        }



        private String transferKorWeather(String weather){
            weather = weather.toLowerCase();


            if(weather.equals("haze")){
                return "안개";
            }
            else if(weather.equals("fog")){
                return "안개";
            }
            else if(weather.equals("clouds")){
                return "구름";
            }
            else if(weather.equals("few clouds")){
                return "구름 조금";
            }
            else if(weather.equals("scattered clouds")){
                return "구름 낌";
            }
            else if(weather.equals("broken clouds")){
                return "구름 많음";
            }
            else if(weather.equals("overcast clouds")){
                return "구름 많음";
            }
            else if(weather.equals("clear sky") || weather.equals("clearsky")){
                return "맑음";
            }
            else if(weather.equals("thunderstorm") || weather.equals("thunderstorm with light rain") || weather.equals("thunderstorm with rain")){
                return "천둥번개";
           }
            return "";
        }

    } // end of class ReceiveWeather Task

    private class ReceiveDustTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... datas) {
            try {

                // for debug worker thread
                if (android.os.Debug.isDebuggerConnected())
                    android.os.Debug.waitForDebugger();

                // create the file with the given file path
                File file = new File(datas[0]);
                HttpURLConnection conn = (HttpURLConnection) new URL(datas[0]).openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    InputStreamReader reader = new InputStreamReader(is);
                    BufferedReader in = new BufferedReader(reader);

                    String readed;
                    while ((readed = in.readLine()) != null) {
                        JSONObject jObject = new JSONObject(readed);
                        //jObject.getJSONArray("weather").getJSONObject(0).getString("icon");

                        return jObject;
                    }
                } else {
                    return null;
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (result != null) {
                String cityName = "";
                String grade = "";

                try {
                    grade = result.getJSONObject("weather").getJSONArray("dust").getJSONObject(0).getJSONObject("pm10").getString("grade");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                dustGrade = grade;
                String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lng + "&units=metric&appid=3376cfd36d634389d9fac1bedde3308c";
               // String skDustUrl = "http://apis.skplanetx.com/weather/dust?version=1&lat=" + 37.5714100000 + "&lon=" + 126.9657900000 + "&appKey=5ed9ffbe-9483-39dd-923b-318c0eb5b799";


                //Execute weather task.
             //   ReceiveDustTask receiveDustTask = new ReceiveDustTask();
                ReceiveWeatherTask receiveWeatherTask = new ReceiveWeatherTask();
                //receiveWeatherTask.execute(url);

              //  AsyncTaskCompat.executeParallel(receiveDustTask, skDustUrl);
                AsyncTaskCompat.executeParallel(receiveWeatherTask, url);
            }
        }
    }

} // end of class OpenWeatherWithGPS.java
