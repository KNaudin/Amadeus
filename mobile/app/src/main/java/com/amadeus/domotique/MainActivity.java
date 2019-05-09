package com.amadeus.domotique;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URI;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Notifyable, View.OnClickListener {

    private String serverIP = "";
    private int serverPort = 8080;
    private HttpListener listener;

    private Button button_light;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        if(settings.getAll().isEmpty()){
            Toast.makeText(this, "Aucune donnée utilisateur trouvée.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this, ConnectionActivity.class);
            intent.putExtra("IP", this.serverIP);
            intent.putExtra("PORT", this.serverPort);
            startActivityForResult(intent, 7);
        }
        else{
            this.serverIP = settings.getString("IP", null);
            this.serverPort = settings.getInt("PORT", 8080);
        }
        this.listener = new HttpListener();
        listener.subscribe(this);

//        button_light = findViewById(R.id.button_light);
//        button_light.setOnClickListener(this);

        LinearLayout mainLayout = findViewById(R.id.main_layout);
        SourceManagement src_man = new SourceManagement(MainActivity.this, mainLayout, "Salon", 200);
    }

    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.button_light: {
//                try{
//                    URI uri = new URI("HTTP", null, this.serverIP, this.serverPort, "/", null, null);
//                    HttpRequestTask requestTask = new HttpRequestTask(this.listener, MainActivity.this, false,
//                            "{\"command\":\"\",\"group\":\"salon\",\"light\":\"\",\"color\":\"\",\"dimmer\":10}");
//                    requestTask.execute(uri);
//                }
//                catch (Exception e){
//                    System.out.println("[AMADEUS] "+e.getMessage());
//                }
//                break;
//            }
//        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.options) {
            Intent intent = new Intent(MainActivity.this, ConnectionActivity.class);
            intent.putExtra("IP", this.serverIP);
            intent.putExtra("PORT", this.serverPort);
            startActivityForResult(intent, 7);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id){
            case R.id.gestion_lumiere: {
                Toast.makeText(this, "Vous êtes déjà sur la bonne page.", Toast.LENGTH_LONG).show();
                break;
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        System.out.println("[AMADEUS] "+requestCode+" "+resultCode);
        switch(requestCode){
            case 7: {
                if (resultCode == -1) {
                    System.out.println("[AMADEUS] Nouvelle IP sélectionnée");
                    this.serverIP = data.getStringExtra("IP");
                    SharedPreferences settings = getSharedPreferences("UserInfo", 0);
                    settings.edit()
                            .putString("IP", this.serverIP)
                            .putInt("PORT", this.serverPort)
                            .apply();
                } else if (resultCode == 0 && this.serverIP == "") {
                    Toast.makeText(this, "Aucune donnée utilisateur trouvée.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, ConnectionActivity.class);
                    intent.putExtra("IP", this.serverIP);
                    intent.putExtra("PORT", this.serverPort);
                    startActivityForResult(intent, 7);
                }
                break;
            }
        }
    }



    @Override
    public void getNotification(JSONObject obj, boolean correctReturn) {
        System.out.println(obj);
    }
}
