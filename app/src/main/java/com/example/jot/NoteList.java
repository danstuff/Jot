package com.example.jot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NoteList implements Serializable {
    public List<Note> data;

    public NoteList() {
        data  = new ArrayList<>();
    }

    public Note addNew() {
        Note note = new Note();
        data.add(note);

        return note;
    }

    public Note get(int i){
        return data.get(i);
    }

    public Note remove(int i){
        Note note = data.get(i);
        data.remove(i);

        return note;
    }
}
