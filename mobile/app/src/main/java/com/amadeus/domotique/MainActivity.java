package com.amadeus.domotique;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Notifyable, View.OnClickListener {

    public static int screenWidth;

    private String serverIP = "";
    private int serverPort = 8080;
    private HttpListener listener;

    private Map<String, Integer> imageIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.fillMap();
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

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        MainActivity.screenWidth = displayMetrics.widthPixels;
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

        URI uri = null;
        try {
            uri = new URI("HTTP", null, this.serverIP, this.serverPort, "/info", null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        HttpRequestTask requestTask = new HttpRequestTask(this.listener, MainActivity.this, true, "", 5);
        requestTask.execute(uri);
    }

    private void fillMap(){
        this.imageIds = new HashMap<>();
        this.imageIds.put("4a418a", R.drawable.blue);
        this.imageIds.put("6c83ba", R.drawable.light_blue);
        this.imageIds.put("8f2686", R.drawable.saturated_purple);
        this.imageIds.put("a9d62b", R.drawable.lime);
        this.imageIds.put("c984bb", R.drawable.light_purple);
        this.imageIds.put("d6e44b", R.drawable.yellow);
        this.imageIds.put("d9337c", R.drawable.saturated_pink);
        this.imageIds.put("da5d41", R.drawable.dark_peach);
        this.imageIds.put("dc4b31", R.drawable.saturated_red);
        this.imageIds.put("dcf0f8", R.drawable.cold_sky);
        this.imageIds.put("e491af", R.drawable.pink);
        this.imageIds.put("e57345", R.drawable.peach);
        this.imageIds.put("e78834", R.drawable.warm_amber);
        this.imageIds.put("e8bedd", R.drawable.light_pink);
        this.imageIds.put("eaf6fb", R.drawable.cool_daylight);
        this.imageIds.put("ebb63e", R.drawable.candlelight);
        this.imageIds.put("efd275", R.drawable.warm_glow);
        this.imageIds.put("f1e0b5", R.drawable.warm_white);
        this.imageIds.put("f2eccf", R.drawable.sunrise);
        this.imageIds.put("f5faf6", R.drawable.cool_white);
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
                    this.serverPort = data.getIntExtra("PORT", 8080);
                    SharedPreferences settings = getSharedPreferences("UserInfo", 0);
                    settings.edit()
                            .putString("IP", this.serverIP)
                            .putInt("PORT", this.serverPort)
                            .apply();
                    try {
                        this.setSources(new JSONObject(data.getStringExtra("INFOS")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == 0 && this.serverIP == "") {
                    Toast.makeText(this, "Aucune donnée utilisateur trouvée.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, ConnectionActivity.class);
                    intent.putExtra("IP", this.serverIP);
                    intent.putExtra("PORT", this.serverPort);
                    startActivityForResult(intent, 7);
                }
                break;
            }
            case 10:{
                if (resultCode==-1){
                    System.out.println("[AMADEUS] Couleur changée");
                    String colorHex = data.getStringExtra("HEX");
                    boolean isLight = data.getBooleanExtra("ISLIGHT", true);
                    String name = data.getStringExtra("NAME");
                    int viewId = data.getIntExtra("VIEWID", -1);
                    de.hdodenhof.circleimageview.CircleImageView imgView = findViewById(viewId);
                    imgView.setImageResource(this.imageIds.get(colorHex));
                    String charge = "";
                    if(isLight){
                        charge = "{\"command\":\"\",\"group\":\"\",\"light\":"+Integer.parseInt(name)+",\"color\":\""+colorHex+"\",\"dimmer\":null}";
                    }
                    else{
                        charge = "{\"command\":\"\",\"group\":\""+name+"\",\"light\":null,\"color\":\""+colorHex+"\",\"dimmer\":null}";
                    }
                    try{
                        URI uri = new URI("HTTP", null, this.serverIP, this.serverPort, "/", null, null);
                        HttpRequestTask requestTask = new HttpRequestTask(this.listener, MainActivity.this, false,
                                charge, 1);
                        requestTask.execute(uri);
                    }
                    catch (Exception e){
                        System.out.println("[AMADEUS] "+e.getMessage());
                    }
                }
                break;
            }
        }
    }



    @Override
    public void getNotification(JSONObject obj, int returnCode) {
        switch (returnCode){
            case 0:{
                System.out.println("[AMADEUS] Mise à jour "+obj);
                this.setSources(obj);
                break;
            }
            case -1:{
                try{
                    System.out.println("[AMADEUS] Envoie des infos: "+obj);
                    URI uri = new URI("HTTP", null, this.serverIP, this.serverPort, "/", null, null);
                    HttpRequestTask requestTask = new HttpRequestTask(this.listener, MainActivity.this, false,
                            obj.toString(), 1);
                    requestTask.execute(uri);
                }
                catch (Exception e){
                    System.out.println("[AMADEUS] "+e.getMessage());
                }
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {

    }

    public void askForColor(View view, boolean isLight, String name){
        Intent intent = new Intent(MainActivity.this, ColorPickActivity.class);
        intent.putExtra("VIEWID", view.getId());
        intent.putExtra("ISLIGHT", isLight);
        intent.putExtra("NAME", name);
        startActivityForResult(intent, 10);
    }

    public void setSources(JSONObject obj){
        System.out.println(obj);
        LinearLayout layout = (LinearLayout) findViewById(R.id.main_layout);
        layout.removeAllViewsInLayout();
        JSONArray groups = null;
        try {
            groups = obj.getJSONArray("groups");
            for(int i = 0 ; i < groups.length() ; i++){
                SourceManagement group = new SourceManagement(this, MainActivity.this, groups.getJSONObject(0).getString("id"), 50, true, this.imageIds.get("efd275"));
                JSONArray lights = groups.getJSONObject(i).getJSONArray("lights");
                for(int j = 0; j < lights.length(); j++){
                    JSONObject jsonObj = lights.getJSONObject(0);
                    group.addItem(jsonObj.getString("id"), jsonObj.getInt("dimmer"), this.imageIds.get(jsonObj.getString("color")));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
