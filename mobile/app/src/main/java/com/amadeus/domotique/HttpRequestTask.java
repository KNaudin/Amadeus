package com.amadeus.domotique;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

public class HttpRequestTask extends AsyncTask<URI, Void, Void> {

    private ProgressDialog progDailog;
    private boolean connectionSet = false;
    private HttpListener listener;
    private JSONObject returnedObject = null;
    private Context context;
    private boolean showLoading;
    private String charge;


    public HttpRequestTask(HttpListener listener, Context context, boolean showLoading, String charge){
        this.listener = listener;
        this.context = context;
        this.showLoading = showLoading;
        this.charge = charge;
        System.out.println("[AMADEUS] Charge: "+this.charge);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(this.showLoading) {
            progDailog = new ProgressDialog(this.context);
            progDailog.setMessage("Veuillez patienter...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(false);
            progDailog.show();
        }
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        if(this.showLoading)
            progDailog.dismiss();
        listener.notifySubs(this.returnedObject, this.connectionSet);
    }

    @Override
    protected Void doInBackground(URI... uri) {
        try{
            URL url = uri[0].toURL();
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if(charge.equals("")) {
                HttpURLConnection.setFollowRedirects(false);
                urlConnection.setConnectTimeout(5 * 1000);
                urlConnection.setRequestMethod("GET");
                System.out.println("[AMADEUS] GET Code de retour du serveur: " + urlConnection.getResponseCode());
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                Scanner s = new Scanner(in).useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";
                System.out.println("[AMADEUS] Réponse du serveur " + result);
                this.returnedObject = new JSONObject(result);
                connectionSet = true;
                urlConnection.disconnect();
            }
            else{
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                urlConnection.setRequestProperty("Accept","application/json");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                JSONObject jsonParam = new JSONObject(this.charge);
                DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();

                System.out.println("[AMADEUS] POST Code de retour du serveur: "+urlConnection.getResponseCode());
                urlConnection.disconnect();
            }
        } catch (Exception e){
            System.out.println("[AMADEUS] Error "+e.getMessage());
        } finally {
            return null;
        }
    }
}
