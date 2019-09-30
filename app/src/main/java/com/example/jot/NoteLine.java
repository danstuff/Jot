package com.example.jot;

import java.io.Serializable;
import java.util.Date;

public class NoteLine implements Serializable {
    private final long COLOR_PHASE_TIME_MS = 259200000; //3 days

    private String content;
    private long create_time;

    public NoteLine(){
        content = "";

        Date d = new Date();
        create_time = d.getTime();
    }

    public NoteLine(String content){
        this.content = content;

        Date d = new Date();
        create_time = d.getTime();
    }

    public void setContent(String new_content){ content = new_content; }

    public String getContent(){ return content; }

    public String getBulletColor(){
        //TODO
        Date d = new Date();
        long cur_time = d.getTime();
        long time_dif = cur_time - create_time;

        float time_pct = time_dif / COLOR_PHASE_TIME_MS;

        if (time_pct > 2) time_pct = 2;

        //use Integer.toString(val, 16) and "#"+r+g+b

        return "";
    }
}
