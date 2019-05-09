package com.amadeus.domotique;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

public class SourceManagement {

    private Context context;

    public SourceManagement(Context mainContext, LinearLayout layout, String name, int defaultValue){
        this.context = mainContext;

        View itemView = LayoutInflater.from(this.context).inflate(R.layout.activity_item,null, false);
        layout.addView(itemView);

        View itemMainLayout = ((ViewGroup)itemView).getChildAt(0);

        Button mainSrcButton = (Button) ((ViewGroup)itemMainLayout).getChildAt(0);
        SeekBar mainSeekBar = (SeekBar) ((ViewGroup)itemMainLayout).getChildAt(1);
        mainSrcButton.setText(name);
        mainSeekBar.setMax(254);
        mainSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                System.out.println("[AMADEUS] valeur seekbar "+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
}
