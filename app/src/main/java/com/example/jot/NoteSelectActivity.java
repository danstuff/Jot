package com.example.jot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NoteSelectActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    private List<Note> notesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_select);

        //button to create a new note
        AppCompatButton NewNote = findViewById(R.id.NewNote);
        NewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                //switch to the main activity (edit note)
                Intent intent = new Intent(NoteSelectActivity.this,
                        NoteEditActivity.class);

                NoteSelectActivity.this.startActivity(intent);
            }
        });

        //button to back up all note data
        AppCompatButton BackupNotes = findViewById(R.id.BackupNotes);
        BackupNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //request external write permissions; later, back up the notes
                String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(perms, 101);
            }
        });

        //get recycler
        RecyclerView NotesRecycler = findViewById(R.id.NotesRecycler);

        //create a click event for each recycler note via an adapter
        final NoteSelectAdapter NotesAdapter = new NoteSelectAdapter(notesList,
                new NoteSelectAdapter.OnNoteClickListener() {
                    @Override
                    public void onNoteClick(Note note) {
                        //intent to switch to main activity, and transmit the selected note
                        Intent intent = new Intent(NoteSelectActivity.this,
                                NoteEditActivity.class);

                        intent.putExtra("NoteOpened", note);
                        NoteSelectActivity.this.startActivity(intent);
                    }
                });

        //attach essentials to the recycler, including the notes adapter
        NotesRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        NotesRecycler.setItemAnimator(new DefaultItemAnimator());
        NotesRecycler.setAdapter(NotesAdapter);

        loadNotes();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults){
        //back up the notes if you got permission for external file saving
        if(requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            NoteIO.backup(NoteSelectActivity.this, notesList);
        }
    }

    private void loadNotes() {
        //fetch all files in main directory
        File directory = getFilesDir();
        File[] files = directory.listFiles();

        //load each file into the list
        for(int f = 0; f < files.length; f++){
            Note note = NoteIO.load(this, files[f].getName());

            if(note != null){ notesList.add(note); }
        }
    }
}
