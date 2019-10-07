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

import com.google.android.material.snackbar.Snackbar;

import java.util.Timer;
import java.util.TimerTask;

public class NoteSelectActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    public static final int AUTO_LOAD_INTERVAL_MS = 15000;

    public static final String SELECTED_NOTE_DATA = "SelNoteData";

    private static final int REQUEST_BACKUP_EXPORT = 101;
    private static final int REQUEST_BACKUP_IMPORT = 102;

    private NoteSelectAdapter NotesAdapter;

    private NoteIO noteIO;
    private AlertUtil alertUtil;

    private NoteList noteList;
    private Note deletedNote;

    private AutoInterval interval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_select);

        noteIO = new NoteIO(this);
        alertUtil = new AlertUtil(this);

        //start by loading in all the notes
        noteList = noteIO.loadList();

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
                        //fetch note from list
                        Note note = noteList.getNote(pos);

                        //get the note's title and first line
                        String title = note.getTitle();
                        String line0 = note.getLineCount() > 0 ?
                                note.getLine(0).getContent() : "";

                        //set the title and first line elements of the holder
                        holder.title.setText(title);
                        holder.first_line.setText(line0);

                        //when clicked, open note to edit
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                noteList = noteIO.loadList();
                                editNote(v, noteList.getNote(pos));
                            }
                        });
                    }
                },
                new NoteSelectAdapter.NoteLengthInterface() {
                    @Override
                    public int getLength() {
                        return noteList.getNoteCount();
                    }
                });
        NotesRecycler.setAdapter(NotesAdapter);

        //dragging items up/down rearranges them
        ItemTouchHelper.SimpleCallback itCallback = ItemTouchUtil.make(this,
                new ItemTouchUtil.Actions() {
            @Override public void move(int fromPos, int toPos) {
                noteList.moveNote(fromPos, toPos);
                noteList = noteIO.cycleList(noteList);

                NotesAdapter.notifyDataSetChanged();
            }

            @Override public String delete(int pos) {
                deletedNote = noteList.getNote(pos);
                noteList.removeNote(pos);

                noteIO.delete(deletedNote.getFileIndex()+".dat");
                noteList = noteIO.cycleList(noteList);

                //notify the adapter
                NotesAdapter.notifyItemRemoved(pos);

                return deletedNote.getTitle();
            }

            @Override
            public void undoDelete() {
                noteList.addNote(deletedNote);
                noteList = noteIO.cycleList(noteList);

                NotesAdapter.notifyDataSetChanged();
            }
        });

        ItemTouchHelper itHelper = new ItemTouchHelper(itCallback);
        itHelper.attachToRecyclerView(NotesRecycler);

        //button to create and edit a new note
        AppCompatButton NewNote = findViewById(R.id.NewNote);
        NewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteList = noteIO.loadList();
                editNote(view, noteList.newNote());
            }
        });

        //button to back up all note data
        AppCompatButton BackupNotes = findViewById(R.id.BackupNotes);

        final String[] options = {"Backup all notes", "Recover notes from backup"};

        BackupNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog backupAsk = alertUtil.make("Backup Options", options,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0) {
                                    //request external write permissions; later, back up the notes
                                    String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                    requestPermissions(perms, REQUEST_BACKUP_EXPORT);
                                } else if (i == 1) {
                                    //request external read permissions; later, recover from backup
                                    String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
                                    requestPermissions(perms, REQUEST_BACKUP_IMPORT);
                                }
                            }
                        });
                backupAsk.show();
            }
        });

        //start a timer that reloads the notes every 15 seconds
        interval = new AutoInterval(new AutoInterval.Task() {
            @Override public void run() {
                noteList = noteIO.loadList();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        NotesAdapter.notifyDataSetChanged();
                    }
                });
            }
        }, AUTO_LOAD_INTERVAL_MS);
        interval.start();

        //request external write permissions; later, back up the notes
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestPermissions(perms, REQUEST_BACKUP_EXPORT);
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, @NonNull String[] p, @NonNull int[] res) {
        if (reqCode == REQUEST_BACKUP_EXPORT &&
                res[0] == PackageManager.PERMISSION_GRANTED) {

            //back up the notes if you got permission for external file saving
            noteIO.exportBackup(noteList);

        } else if (reqCode == REQUEST_BACKUP_IMPORT &&
                res[0] == PackageManager.PERMISSION_GRANTED) {

            //fetch all backup names and ask user to pick one
            final String[] options = noteIO.getBackupNames();

            AlertDialog backupAsk = alertUtil.make("Choose a Backup File", options,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //import the selected file
                            noteList = noteIO.importBackup(options[i]);

                            //reload everything
                            noteIO.saveList(noteList);
                            noteList = noteIO.loadList();

                            NotesAdapter.notifyDataSetChanged();
                        }
                    });
            backupAsk.show();
        }
    }

    public void editNote(View v, Note note) {
        noteIO.save(note);

        //pass the selected note, switch to the note edit activity, and exit
        Intent intent = new Intent(v.getContext(), NoteEditActivity.class);
        intent.putExtra(SELECTED_NOTE_DATA, note);

        v.getContext().startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        interval.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        interval.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        interval.stop();

        System.exit(0);
    }
}
