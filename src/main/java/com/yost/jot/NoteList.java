package com.yost.jot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoteList implements Serializable {
    private List<Note> notes;

    public NoteList() {
        notes = new ArrayList<>();
    }

    public Note newNote() {
        Note note = new Note();
        notes.add(note);
        return note;
    }

    public void addNote(Note note){ notes.add(note); }

    public void removeNote(int i){ notes.remove(i); }

    public void moveNote(int fromPos, int toPos){
        if (fromPos < toPos) {
            for (int i = fromPos; i < toPos; i++) {
                Collections.swap(notes, i, i + 1);
            }
        } else {
            for (int i = fromPos; i > toPos; i--) {
                Collections.swap(notes, i, i - 1);
            }
        }
    }

    public int getNoteCount(){ return notes.size(); }

    public Note getNote(int i){ return notes.get(i); }

    public int getSize(){
        return notes.size();
    }

    public Note getLast(){
        return getNoteCount() > 0 ? getNote(getNoteCount() - 1) : newNote();
    }

    public String[] getTitles(){
        String[] names = new String[notes.size()];

        for(int i = 0; i < notes.size(); i++){
            names[i] = notes.get(i).getTitle();
        }

        return names;
    }

    public void print(){
        for(int i = 0; i < getNoteCount(); i++){
            getNote(i).print();
        }
    }
}
