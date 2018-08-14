package com.example.eperez.androidexam.Modelos;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.example.eperez.androidexam.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class DrawPolilyne {

    GoogleMap mMap = null;
    Context context = null;
    String url ="";


    public DrawPolilyne(Context context,GoogleMap mMap,String url){
        this.context = context;
        this.mMap = mMap;
        this.url = url;

    }

    public void DrawnPolyline(){
        JSONObject response = getJSON(url);
        Log.i("TAG url: ",url);
        JSONArray arr=null,another = null,another4=null;
        JSONObject another3 = null,legs= null;
        JSONArray arr2 = null;
        try {
            arr = response.getJSONArray("routes");
            legs = arr.getJSONObject(0);
            another = legs.getJSONArray("legs");
            another3 = another.getJSONObject(0);
            another4 = another3.getJSONArray("steps");


            arr2 = new JSONArray();
            for (int i = 0; i <another4.length(); i++) {
                JSONObject ob = another4.getJSONObject(i);
                JSONObject obj2 = new JSONObject();

                obj2.accumulate("lat", ob.getJSONObject("end_location").getDouble("lat"));
                obj2.accumulate("lng", ob.getJSONObject("end_location").getDouble("lng"));
                arr2.put(obj2);

            }

        } catch (JSONException e) {
            e.getMessage();
        }

        PolylineOptions polylineOptions = new PolylineOptions();
        for (int i = 0; i < arr2.length(); i++)
        {
            JSONObject obj = null;
            String latd="";
            String lng="";
            try {
                obj = arr2.getJSONObject(i);
                latd = obj.getString("lat");
                lng = obj.getString("lng");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            polylineOptions.color(context.getResources().getColor(R.color.colorAccent));

            polylineOptions.width(10);

            Double lat = Double.parseDouble(latd);
            Double Longitude = Double.parseDouble(lng);

            polylineOptions.add(new LatLng(lat, Longitude));
        }
        mMap.addPolyline(polylineOptions);
    }


    public JSONObject getJSON(String mUrl){

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = stringToURL(mUrl);

            urlConnection = (HttpURLConnection) url
                    .openConnection();

            InputStream in = urlConnection.getInputStream();

            InputStreamReader isw = new InputStreamReader(in);

            StringBuilder builder = new StringBuilder();

            int data = isw.read();
            while (data != -1) {
                char current = (char) data;
                data = isw.read();
                //System.out.print(current);
                builder.append(current);
            }
            return new JSONObject(String.valueOf(builder));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private URL stringToURL(String urlString){
        try{
            URL url = new URL(urlString);
            return url;
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
        return null;
    }
}
