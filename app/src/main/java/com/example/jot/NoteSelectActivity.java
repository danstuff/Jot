package com.example.jot;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NoteSelectActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    public static final int AUTO_LOAD_INTERVAL_MS = 15000;

    public static final String SELECTED_NOTE_DATA = "SelNoteData";

    private static final int REQUEST_BACKUP_EXPORT = 101;
    private static final int REQUEST_BACKUP_IMPORT = 102;
    private static final int REQUEST_BACKUP_CULL = 103;

    private GestureDetector gestureDetector;

    private TextView EmptyMessage;

    private NoteSelectAdapter NotesAdapter;

    private AutoInterval interval;
    private NoteIO noteIO;

    private NoteList noteList;
    private Note deletedNote;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_select);

        noteIO = new NoteIO(this);
        noteList = noteIO.loadList();

        //toggle the empty message off if notelist is populated
        EmptyMessage = findViewById(R.id.EmptyNotesMessage);

        if(noteList.getSize() > 0){
            EmptyMessage.setVisibility(View.GONE);
        }

        //if some text was sent to Jot, pick a note to add it to
        Intent intent = getIntent();

        if(intent.getAction().equals(Intent.ACTION_SEND) &&
                intent.getType().equals("text/plain")){
            final String new_line = intent.getStringExtra(Intent.EXTRA_TEXT);
            String[] note_options = noteList.getTitles();

            AlertUtil.make(this, "Pick a Note to Insert Text", note_options,
                    new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialogInterface, int i) {
                            noteList.getNote(i).addLine(new_line);
                            cycle();

                            Toast.makeText(NoteSelectActivity.this,
                                    "Text Inserted", Toast.LENGTH_LONG).show();
                        }
                    });
        }

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
                            @Override public void onClick(View v) {
                                noteList = noteIO.loadList();
                                editNote(NoteSelectActivity.this, noteList.getNote(pos));
                            }
                        });
                    }
                },
                new NoteSelectAdapter.NoteLengthInterface() {
                    @Override public int getLength() {
                        return noteList.getNoteCount();
                    }
                });
        NotesRecycler.setAdapter(NotesAdapter);

        //dragging items up/down rearranges them
        ItemTouchHelper.SimpleCallback itCallback = ItemTouchUtil.make(this,
                new ItemTouchUtil.Actions() {
            @Override public void move(int fromPos, int toPos) {
                noteList.moveNote(fromPos, toPos);
                cycle();

                NotesAdapter.notifyDataSetChanged();
            }

            @Override public String delete(int pos) {
                deletedNote = noteList.getNote(pos);
                noteList.removeNote(pos);

                cycle();

                //notify the adapter
                NotesAdapter.notifyItemRemoved(pos);

                //show empty message if notelist is empty
                if(noteList.getSize() == 0){
                    EmptyMessage.setVisibility(View.VISIBLE);
                }

                return deletedNote.getTitle();
            }

            @Override public void undoDelete() {
                noteList.addNote(deletedNote);
                cycle();

                EmptyMessage.setVisibility(View.GONE);

                NotesAdapter.notifyDataSetChanged();
            }
        });

        ItemTouchHelper itHelper = new ItemTouchHelper(itCallback);
        itHelper.attachToRecyclerView(NotesRecycler);

        //button to back up all note data
        AppCompatButton BackupNotes = findViewById(R.id.ViewOptions);

        final String[] options = {"Backup all notes", "Recover notes from backup", "Delete old backups"};

        BackupNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertUtil.make(NoteSelectActivity.this, "Options", options,
                    new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0) {
                                //request external write permissions; later, back up the notes
                                String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                requestPermissions(perms, REQUEST_BACKUP_EXPORT);
                            } else if (i == 1) {
                                //request external read permissions; later, recover from backup
                                String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
                                requestPermissions(perms, REQUEST_BACKUP_IMPORT);
                            } else if(i == 2){
                                //request external read permissions; later, recover from backup
                                String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                requestPermissions(perms, REQUEST_BACKUP_CULL);
                            }
                        }
                    });
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

        //double tap creates new notes
        GestureUtil.bindGesture(this, NotesRecycler, new GestureUtil.DoubleTap() {
            @Override public void onDoubleTap() {
                EmptyMessage.setVisibility(View.GONE);
                editNote(NoteSelectActivity.this, noteList.newNote());
            }
        });

        if(noteIO.needBackup()){
            //request external write permissions; later, back up the notes
            String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(perms, REQUEST_BACKUP_EXPORT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int c, @NonNull String[] p, @NonNull int[] r) {
        if (c == REQUEST_BACKUP_EXPORT && r[0] == PackageManager.PERMISSION_GRANTED) {
            //back up the notes if you got permission for external file saving
            AsyncTask.execute(new Runnable() {
                @Override public void run() {
                    noteIO.exportBackup(noteList);
                }
            });

        } else if (c == REQUEST_BACKUP_IMPORT && r[0] == PackageManager.PERMISSION_GRANTED) {
            //fetch all backup names and ask user to pick one
            final String[] options = noteIO.getBackupNames();

            AlertUtil.make(NoteSelectActivity.this,
                    "Choose a Backup File", options,
                    new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialogInterface, int i) {
                            final int fi = i;

                            //import the selected file
                            AsyncTask.execute(new Runnable() {
                                @Override public void run() {
                                    noteList = noteIO.importBackup(options[fi]);
                                }
                            });

                            //reload everything
                            cycle();

                            NotesAdapter.notifyDataSetChanged();
                        }
                    });
        } else if (c == REQUEST_BACKUP_CULL && r[0] == PackageManager.PERMISSION_GRANTED) {
            noteIO.cleanBackupDir();
        }
    }

    public void editNote(Context ctx, Note note) {
        noteIO.save(note);

        //pass the selected note, switch to the note edit activity, and exit
        Intent intent = new Intent(ctx, NoteEditActivity.class);
        intent.putExtra(SELECTED_NOTE_DATA, note);

        ctx.startActivity(intent);
    }

    public void cycle(){
        AsyncTask.execute(new Runnable() {
            @Override public void run() {
                noteList = noteIO.cycleList(noteList);
                noteIO.cleanLocalDir(noteList);
            }
        });
    }

    @Override public void onResume() {
        interval.start();
        super.onResume();
    }

    @Override public void onPause() {
        interval.stop();
        super.onPause();
    }

    @Override public void onDestroy() {

        interval.stop();
        super.onDestroy();
        System.exit(0);
    }
}
