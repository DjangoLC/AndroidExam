package com.example.eperez.androidexam;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Task extends AsyncTask<Void,Void,Boolean> {

    private List<LatLng> latLng;
    private listener listener;
    private StringBuilder builder;
    private final String TAG = getClass().getSimpleName();


    public Task(List<LatLng> latLng, listener listener){
        this.latLng = latLng;
        this.listener = listener;
    }

    interface listener{
        void onFinish(StringBuilder builder);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        JSONObject object = new JSONObject();
        builder = new StringBuilder();
        for (LatLng l : latLng){
            try {
                object.put("latitude", l.latitude);
                object.put("longitud",l.longitude);
                Log.i(TAG,object.toString());
                builder.append(object.toString());


            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }

        }
        Log.i(TAG,object.toString());


        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean){
            Log.i(TAG,builder.toString());
            listener.onFinish(builder);
        } else{

        }
    }
}
