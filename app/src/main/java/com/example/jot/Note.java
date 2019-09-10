package com.example.jot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Note implements Serializable {
    private String title;
    private List<String> lines;

    public Note() {
        title  = "";
        lines = new ArrayList<>();
    }

    public Note(Note to_copy){
        title =  to_copy.title;
        lines = new ArrayList<>(to_copy.lines);
    }

    public int getLength(){ return lines.size(); }

    public String getTitle(){ return title; }

    public void setTitle(String new_title){ title = new_title; }

    public String getLine(int line_id){ return lines.get(line_id); }

    public void setLine(int line_id, String content){ lines.set(line_id, content); }

    public void moveLine(int fromPos, int toPos){
        //removeNote the variable at fromPos, saving it
        String fromLine = lines.get(fromPos);
        lines.remove(fromPos);

        int size = lines.size();

        //duplicate the item at the end
        String last = String.copyValueOf(lines.get(size-1).toCharArray());
        lines.add(last);

        //move each item after toPos down 1 in the array
        for(int i = size-1; i > toPos; i--){
            String next = lines.get(i-1);
            lines.set(i, next);
        }

        //there are now two instances of variable at toPos;
        //insert the variable from fromPos at toPos
        lines.set(toPos, fromLine);
    }

    public void removeLine(int line_id){ lines.remove(line_id); }

    public void newLine(){ lines.add(""); }

    public void addLine(String line){ lines.add(line); }
}
