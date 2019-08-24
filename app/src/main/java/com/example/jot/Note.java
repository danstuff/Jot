package com.example.jot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Note implements Serializable {
    public static int last_id = 0;
    public int id;

    public String title;
    public List<String> lines;

    public Note() {
        last_id++;

        this.id = last_id;
        title  = "";
        lines = new ArrayList<>();
    }
}
