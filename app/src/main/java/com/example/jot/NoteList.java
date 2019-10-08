package com.example.jot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoteList {
    private List<Note> notes;

    public NoteList() {
        notes = new ArrayList<>();
    }

    public Note newNote() {
        Note note = new Note(getNoteCount());
        notes.add(note);
        return note;
    }

    public void addNote(Note note){
        notes.add(note);
    }

    public int getNoteCount(){ return notes.size(); }

    public Note getNote(int i){
        return notes.get(i);
    }

    public Note getLast(){ return getNote(getNoteCount() - 1); }

    public String[] getTitles(){
        String[] names = new String[notes.size()];

        for(int i = 0; i < notes.size(); i++){
            names[i] = notes.get(i).getTitle();
        }

        return names;
    }

    public int getSize(){
        return notes.size();
    }

    public void moveNote(int fromPos, int toPos){
        Collections.swap(notes, fromPos, toPos);
    }

    public void removeNote(int i){ notes.remove(i); }

    public void print(){
        for(int i = 0; i < getNoteCount(); i++){
            getNote(i).print();
        }
    }
}
