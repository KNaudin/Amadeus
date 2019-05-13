package com.amadeus.domotique;

import android.view.View;

import java.lang.reflect.Constructor;

public class Identity {
    private View v;
    private Class c;
    private String t, n;

    public Identity(View v, Class c, String type, String name){
        this.v = v;
        this.c = c;
        this.t = type;
        this.n = name;
    }

    public View getView(){
        return this.v;
    }

    public Class getClss(){
        return this.c;
    }

    public String getType(){
        return this.t;
    }

    public String getName(){
        return this.n;
    }
}
