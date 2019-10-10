package com.example.jot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Note implements Serializable {
    private int file_index;
    private String title;
    private List<NoteLine> lines;

    public Note(int file_index) {
        this.file_index = file_index;

        title  = "";
        lines = new ArrayList<>();
    }

    private int flip(int line_id){
        return lines.size() - line_id - 1;
    }

    public String getTitle(){ return title; }
    public void setTitle(String new_title){ title = new_title; }

    public int getFileIndex(){ return file_index; }
    public void setFileIndex(int new_index){ file_index = new_index; }

    public NoteLine getLine(int line_id){ return lines.get(flip(line_id)); }

    public int getLineCount(){ return lines.size(); }

    public void newLine(){ lines.add(new NoteLine()); }

    public void addLine(String content){ lines.add(new NoteLine(content)); }

    public void removeLine(int line_id){ lines.remove(flip(line_id)); }

    public void moveLine(int fromPos, int toPos){
        Collections.swap(lines, flip(fromPos), flip(toPos));
    }

    public void print(){
        System.out.println(file_index + " - " + title);

        for(int i = 0; i < lines.size(); i++){
            System.out.println(" - " + lines.get(i).getContent());
        }
    }
}
