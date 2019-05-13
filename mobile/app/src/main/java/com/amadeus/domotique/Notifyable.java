package com.amadeus.domotique;

import org.json.JSONObject;

public interface Notifyable {
    public void getNotification(JSONObject obj, int returnCode);
}
