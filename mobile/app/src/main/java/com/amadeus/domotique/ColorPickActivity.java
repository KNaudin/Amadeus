package com.amadeus.domotique;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ColorPickActivity extends AppCompatActivity {

    private int viewId;
    private boolean isLight;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_pick);
        this.viewId = getIntent().getIntExtra("VIEWID", -1);
        this.isLight = getIntent().getBooleanExtra("ISLIGHT", true);
        this.name = getIntent().getStringExtra("NAME");
    }

    public void getColor(View v){
        Intent data = new Intent();
        de.hdodenhof.circleimageview.CircleImageView img = (de.hdodenhof.circleimageview.CircleImageView) v;
        data.putExtra("HEX", v.getTag().toString());
        data.putExtra("VIEWID", this.viewId);
        data.putExtra("ISLIGHT", this.isLight);
        data.putExtra("NAME", this.name);
        setResult(-1, data);
        finish();
    }
}
