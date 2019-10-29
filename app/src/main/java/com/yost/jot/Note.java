package com.yost.jot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Note implements Serializable {
    private String title;
    private List<NoteLine> lines;

    public Note(){
        title  = "";
        lines = new ArrayList<>();
    }

    private int flip(int line_id){
        return lines.size() - line_id - 1;
    }

    public String getTitle(){ return title; }
    public void setTitle(String new_title){ title = new_title; }

    public NoteLine getLine(int line_id){ return lines.get(flip(line_id)); }

    public int getLineCount(){ return lines.size(); }

    public void newLine(){ lines.add(new NoteLine()); }

    public void addLine(String content){ lines.add(new NoteLine(content)); }

    public void removeLine(int line_id){ lines.remove(flip(line_id)); }

    public void moveLine(int fromPos, int toPos){
        int fp = flip(fromPos);
        int tp = flip(toPos);

        if (fp < tp) {
            for (int i = fp; i < tp; i++) {
                Collections.swap(lines, i, i + 1);
            }
        } else {
            for (int i = fp; i > tp; i--) {
                Collections.swap(lines, i, i - 1);
            }
        }
    }

    public void print(){
        System.out.println(title);

        for(int i = 0; i < lines.size(); i++){
            System.out.println(" - " + lines.get(i).getContent());
        }
    }
}
