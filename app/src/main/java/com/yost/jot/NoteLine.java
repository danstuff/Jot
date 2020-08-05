package com.yost.jot;

import java.io.Serializable;
import java.util.Date;

public class NoteLine implements Serializable {
    private String content;

    public NoteLine(){
        content = "";

    }

    public NoteLine(String content){
        this.content = content;
    }

    public void setContent(String new_content){ content = new_content; }

    public String getContent(){ return content; }
}
