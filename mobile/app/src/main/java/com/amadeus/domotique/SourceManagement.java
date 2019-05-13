package com.amadeus.domotique;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SourceManagement extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private Context context;
    private AppCompatActivity parent;
    private String name = "";
    private String showName = "";
    private String groupPrint = "Lumière";
    private boolean isGroup = false;
    private int lastValue;
    private boolean sliding = false;
    private Map<Integer, Identity> ids;
    private Map<SeekBar, Boolean> slidingSliders;
    private Map<SeekBar, Integer> slidersValue;

    final private LinearLayout itemsLayout;

    public SourceManagement(AppCompatActivity parent, Context mainContext, String name, int defaultValue, boolean isGroup){
        this.ids = new HashMap<>();
        this.slidingSliders = new HashMap<>();
        this.slidersValue = new HashMap<>();
        this.context = mainContext;
        this.parent = parent;
        this.name = name;
        this.isGroup = isGroup;
        this.showName = name;
        if(isGroup)
            this.groupPrint = "Groupe";
        else{
            showName = "Lampe "+Integer.parseInt(name);
        }
        this.lastValue = defaultValue;

        LinearLayout layout = parent.findViewById(R.id.main_layout);

        View itemView = LayoutInflater.from(this.context).inflate(R.layout.activity_item,null, false);
        View itemMainLayout = ((ViewGroup)itemView).getChildAt(0);
        View itemChildren = ((ViewGroup)itemMainLayout).getChildAt(1);
        View firstItemLayout = ((ViewGroup)itemChildren).getChildAt(0);

        ((LinearLayout)firstItemLayout).setGravity(Gravity.CENTER);

        this.itemsLayout = (LinearLayout) itemChildren;
        this.itemsLayout.setVisibility(View.GONE);

        layout.addView(itemView);


        Button mainSrcButton = (Button) ((ViewGroup)itemMainLayout).getChildAt(0);
        mainSrcButton.setId(View.generateViewId());
        SeekBar mainSeekBar = (SeekBar) ((ViewGroup)firstItemLayout).getChildAt(0);
        mainSeekBar.setId(View.generateViewId());
        ImageView mainColor = (ImageView)  ((ViewGroup)firstItemLayout).getChildAt(1);
        mainColor.setId(View.generateViewId());

        mainSrcButton.setText(this.showName);
        mainSeekBar.setProgress(defaultValue);
        mainSeekBar.setMax(254);
        mainSeekBar.setOnSeekBarChangeListener(this);

        mainSrcButton.setOnClickListener(this);
        mainColor.setOnClickListener(this);

        this.addId(mainSrcButton, Button.class, this.groupPrint, this.name);
        this.addId(mainSeekBar, SeekBar.class, this.groupPrint, this.name);
        this.addId(mainColor, ImageView.class, this.groupPrint, this.name);
        this.setSliding(mainSeekBar, false);
        this.setSliderValue(mainSeekBar, defaultValue);
    }

    public void addItem(String name, int defaultValue){
        LinearLayout hLayout = new LinearLayout(this.context);
        hLayout.setOrientation(LinearLayout.HORIZONTAL);
        TextView label = new TextView(this.context);
        label.setText("Lampe "+name);
        SeekBar seekBar = new SeekBar(this.context);
        seekBar.setProgress(defaultValue);
        seekBar.setMax(254);
        ImageView colorBox = new ImageView(this.context);
        colorBox.setImageResource(android.R.color.holo_blue_dark);
        hLayout.addView(label);
        hLayout.addView(seekBar);
        hLayout.addView(colorBox);

        this.itemsLayout.addView(hLayout);

        seekBar.getLayoutParams().width = (int) (MainActivity.screenWidth * 0.6);
        colorBox.getLayoutParams().width = (int) this.context.getResources().getDimension(android.R.dimen.notification_large_icon_width);
        colorBox.getLayoutParams().height = (int) this.context.getResources().getDimension(android.R.dimen.notification_large_icon_height);
        hLayout.setGravity(Gravity.CENTER);
        hLayout.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
        hLayout.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;

        seekBar.setId(View.generateViewId());
        colorBox.setId(View.generateViewId());

        this.addId(seekBar, SeekBar.class, "Lumière", name);
        this.addId(colorBox, ImageView.class, "Lumière", name);
        this.setSliding(seekBar, false);
        this.setSliderValue(seekBar, defaultValue);

        colorBox.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);

    }

    public void addId(View v, Class c, String linkedTo, String name){
        this.ids.put(v.getId(), new Identity(v, c, linkedTo, name));
    }

    public Identity getIdentityByID(int id){
        return this.ids.get(id);
    }

    public void setSliding(SeekBar sb, boolean sliding){
        this.slidingSliders.put(sb, sliding);
    }

    public boolean getSliding(SeekBar sb){
        return this.slidingSliders.get(sb);
    }

    public void setSliderValue(SeekBar sb, int value){
        this.slidersValue.put(sb, value);
    }

    public int getSliderLastValue(SeekBar sb){
        return this.slidersValue.get(sb);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(Math.abs(this.getSliderLastValue(seekBar) - progress) >= 10){
            Identity objectSlide = this.getIdentityByID(seekBar.getId());
            this.setSliding(seekBar, true);
            this.setSliderValue(seekBar, progress);
            System.out.println("[AMADEUS] "+objectSlide.getType()+" "+objectSlide.getName()+" paramétré à "+progress);
            String data = "";
            if(objectSlide.getType().equals("Groupe")){
                data = "{\"command\":\"\",\"group\":\""+objectSlide.getName()+"\",\"light\":\"\",\"color\":\"\",\"dimmer\":"+progress+"}";
            }
            else{
                data = "{\"command\":\"\",\"group\":\"\",\"light\":"+Integer.parseInt(objectSlide.getName())+",\"color\":\"\",\"dimmer\":"+progress+"}";
            }
            try {
                ((Notifyable)(this.parent))
                        .getNotification(new JSONObject(data), -1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        this.setSliding(seekBar, false);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Identity objectSlide = this.getIdentityByID(seekBar.getId());
        System.out.println("[AMADEUS] Finished setting value for "+objectSlide.getType()+" "+objectSlide.getName());
//        if(!this.getSliding(seekBar)) {
            System.out.println("[AMADEUS] " + objectSlide.getType() + " " + objectSlide.getName() + " paramétré à " + seekBar.getProgress());
            String data = "";
            if (objectSlide.getType().equals("Groupe")) {
                data = "{\"command\":\"\",\"group\":\"" + objectSlide.getName() + "\",\"light\":\"\",\"color\":\"\",\"dimmer\":" + seekBar.getProgress() + "}";
            } else {
                data = "{\"command\":\"\",\"group\":\"\",\"light\":" + Integer.parseInt(objectSlide.getName()) + ",\"color\":\"\",\"dimmer\":" + seekBar.getProgress() + "}";
            }
            try {
                ((Notifyable) (this.parent))
                        .getNotification(new JSONObject(data), -1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
//        }
    }

    @Override
    public void onClick(View v) {
        Identity objectClicked = this.getIdentityByID(v.getId());
        System.out.println("[AMADEUS] "+objectClicked.getType()+" "+objectClicked.getName()+" cliqué");
        switch (objectClicked.getClss().getName()){
            case "android.widget.Button":{
                Button b = (Button) v;
                if(b.getText().toString() == this.showName){
                    if(this.itemsLayout.getVisibility() == View.GONE){
                        this.expand();
                    }
                    else{
                        this.collapse();
                    }
                }
                break;
            }
            case "android.widget.ImageView":{
                break;
            }
        }
//
    }

    private void expand() {
        //set Visible
        itemsLayout.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        itemsLayout.measure(widthSpec, heightSpec);

        ValueAnimator mAnimator = slideAnimator(0, itemsLayout.getMeasuredHeight());

        mAnimator.start();
    }

    private void collapse() {
        int finalHeight = itemsLayout.getHeight();

        ValueAnimator mAnimator = slideAnimator(finalHeight, 0);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                //Height=0, but it set visibility to GONE
                itemsLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        mAnimator.start();
    }

    private ValueAnimator slideAnimator(int start, int end) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);


        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();

                ViewGroup.LayoutParams layoutParams = itemsLayout.getLayoutParams();
                layoutParams.height = value;
                itemsLayout.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }
}
