package com.amadeus.domotique;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.icu.text.IDNA;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

public class ConnectionActivity extends AppCompatActivity implements Notifyable, View.OnClickListener {
    private Button button_connect;
    private TextView text_ip;
    private TextView text_port;
    private TextView text_error;
    private HttpListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String known_ip = getIntent().getStringExtra("IP");
        int known_port = getIntent().getIntExtra("PORT", 8080);

        this.listener = new HttpListener();
        this.listener.subscribe(this);

        setContentView(R.layout.activity_connection);

        this.text_ip = findViewById(R.id.text_ip);
        this.text_port = findViewById(R.id.text_port);
        System.out.println("[AMADEUS] Connexion activité");
        this.text_ip.setText(known_ip);
        this.text_port.setText(String.valueOf(known_port));


        this.button_connect = findViewById(R.id.button_connect);
        this.button_connect.setOnClickListener(this);

        this.text_error = findViewById(R.id.text_errorMessage);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_connect:{
                try{
                    int port = Integer.parseInt(this.text_port.getText().toString());
                    String address = this.text_ip.getText().toString();
                    if(address != null && address != "") {
                        URI uri = new URI("HTTP", null, address, port, "/info", null, null);
                        HttpRequestTask requestTask = new HttpRequestTask(this.listener, ConnectionActivity.this, true, "", 5);
                        requestTask.execute(uri);
                    }
                }
                catch (Exception e){
                    System.out.println("[AMADEUS] "+e.getMessage());
                }
                break;
            }
        }
    }

    @Override
    public void getNotification(JSONObject obj, int returnCode) {
        System.out.println("[AMADEUS] Valeur de retour de la connexion: "+returnCode);
        if(returnCode == 0){
            Intent data = new Intent();
            data.putExtra("IP", this.text_ip.getText().toString());
            data.putExtra("PORT", Integer.parseInt(this.text_port.getText().toString()));
            data.putExtra("INFOS", obj.toString());
            setResult(RESULT_OK, data);
            finish();
        }
        else{
            this.text_error.setText("Erreur de connexion.");
        }
    }
}
