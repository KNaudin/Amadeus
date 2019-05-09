package com.amadeus.domotique;

import android.support.v7.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.ArrayList;

public class HttpListener {
    private ArrayList<Notifyable> subscribers;

    public HttpListener(){
        subscribers = new ArrayList<>();
    }

    public void subscribe(Notifyable app){
        this.subscribers.add(app);
    }

    public void notifySubs(JSONObject obj, boolean correctReturn){
        for(Notifyable app : this.subscribers){
            app.getNotification(obj, correctReturn);
        }
    }
}
