package com.example.jot;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NoteSelectActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback{
    private static final int REQUEST_BACKUP_EXPORT = 101;
    private static final int REQUEST_BACKUP_IMPORT = 102;

    private NoteSelectAdapter NotesAdapter;

    private NoteList noteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_select);

        //start by loading in all the notes
        NoteIO.setActivity(this);
        noteList = NoteIO.load();

        //create notes recycler
        RecyclerView NotesRecycler = findViewById(R.id.NotesRecycler);

        //attach a generic linear layout manager and item animator
        NotesRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        NotesRecycler.setItemAnimator(new DefaultItemAnimator());

        //create a click event for each recycler note via an adapter
        NotesAdapter = new NoteSelectAdapter(
            new NoteSelectAdapter.NoteBindInterface() {
                @Override
                public void onBindNote(NoteSelectAdapter.ViewHolder holder, final int pos) {
                    //fetch note from list; set the holder's title and lines
                    Note note = noteList.getNote(pos);
                    String line0 = note.getLength() > 0 ? note.getLine(0) : "";

                    holder.title.setText(note.getTitle());
                    holder.first_line.setText(line0);

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //select this note
                            noteList.selectNote(pos);

                            //switch to the note edit activity
                            Intent intent = new Intent(v.getContext(), NoteEditActivity.class);
                            v.getContext().startActivity(intent);
                        }
                    });
                }
            },
            new NoteSelectAdapter.NoteLengthInterface() {
                @Override
                public int getLength() {
                    return noteList.getLength();
                }
            });
        NotesRecycler.setAdapter(NotesAdapter);

        //dragging items up/down rearranges them
        ItemTouchHelper itHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recycler,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        //getNote to and from positions and do the move
                        int fromPos = viewHolder.getAdapterPosition();
                        int toPos = target.getAdapterPosition();

                        noteList.moveNote(fromPos, toPos);
                        NoteIO.save(noteList);

                        NotesAdapter.notifyDataSetChanged();
                        return true;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                        final int pos = viewHolder.getAdapterPosition();

                        //prompt user, double-check if they want to delete
                        AlertDialog deleteAsk = new AlertDialog.Builder(NoteSelectActivity.this)
                                .setTitle("Confirm Delete")
                                .setMessage("Do you want to delete '" + noteList.getNote(pos).getTitle() + "'?")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //remove the item and notify the adapter
                                        noteList.removeNote(pos);

                                        NoteIO.save(noteList);

                                        NotesAdapter.notifyItemRemoved(pos);
                                        dialogInterface.dismiss();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                                        NotesAdapter.notifyDataSetChanged();
                                        dialogInterface.dismiss();
                                    }
                                })
                                .create();
                        deleteAsk.show();
                    }
                });
        itHelper.attachToRecyclerView(NotesRecycler);

        //button to create a new note
        AppCompatButton NewNote = findViewById(R.id.NewNote);
        NewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                noteList.newNote();
                noteList.selectNote(noteList.getLength()-1);

                NoteIO.save(noteList);

                //switch to the main activity (edit note)
                Intent intent = new Intent(NoteSelectActivity.this,
                        NoteEditActivity.class);

                NoteSelectActivity.this.startActivity(intent);
            }
        });

        //button to back up all note data
        AppCompatButton BackupNotes = findViewById(R.id.BackupNotes);

        final String[] options = {"Backup all notes", "Recover notes from backup"};

        BackupNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog backupAsk = new AlertDialog.Builder(NoteSelectActivity.this).setTitle("Backup Options")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0){
                                    //request external write permissions; later, back up the notes
                                    String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                    requestPermissions(perms, REQUEST_BACKUP_EXPORT);
                                } else if (i == 1){
                                    //request external read permissions; later, recover from backup
                                    String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
                                    requestPermissions(perms, REQUEST_BACKUP_IMPORT);
                                }
                            }
                        }).create();
                backupAsk.show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] p, @NonNull int[] res){
        if(requestCode == REQUEST_BACKUP_EXPORT &&
           res[0] == PackageManager.PERMISSION_GRANTED){

            //back up the notes if you got permission for external file saving
            NoteIO.exportBackup(noteList);

        } else if(requestCode == REQUEST_BACKUP_IMPORT &&
                res[0] == PackageManager.PERMISSION_GRANTED){

            //fetch all backup names and ask user to pick one
            final String[] options = NoteIO.getBackupNames();

            AlertDialog backupAsk = new AlertDialog.Builder(NoteSelectActivity.this)
                    .setTitle("Choose a Backup File")
                    .setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            noteList = NoteIO.importBackup(options[i]);

                            NoteIO.save(noteList);
                            noteList = NoteIO.load();

                            NotesAdapter.notifyDataSetChanged();
                        }
                    }).create();
            backupAsk.show();
        }
    }

    @Override
    public void onDestroy(){
        NoteIO.save(noteList);

        super.onDestroy();

        finish();
        System.exit(0);
    }
}
