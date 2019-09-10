package com.example.jot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NoteList implements Serializable {
    private List<Note> notes;
    private static int selected_id = 0;

    public NoteList() {
        notes = new ArrayList<>();
    }

    public Note getNote(int i){
        return notes.get(i);
    }
    public void removeNote(int i){ notes.remove(i); }

    public void moveNote(int fromPos, int toPos){
        //removeNote the variable at fromPos, saving it
        Note fromNote = getNote(fromPos);
        removeNote(fromPos);

        int size = getLength();

        //duplicate the item at the end
        Note last = new Note(getNote(size-1));
        notes.add(last);

        //move each item after toPos down 1 in the array
        for(int i = size-1; i > toPos; i--){
            Note next = getNote(i-1);
            notes.set(i, next);
        }

        //there are now two instances of variable at toPos;
        //insert the variable from fromPos at toPos
        notes.set(toPos, fromNote);
    }

    public void newNote() { notes.add(new Note()); }

    public int getLength(){ return notes.size(); }

    public void selectNote(int id){ selected_id = id; }

    public Note getSelected(){ return getNote(selected_id); }

    public Note getLast(){ return getNote(getLength() - 1); }

    public void print(){
        for(int i = 0; i < getLength(); i++){
            System.out.println(getNote(i).getTitle() + " lines: " +  getNote(i).getLength());
        }
    }
}
