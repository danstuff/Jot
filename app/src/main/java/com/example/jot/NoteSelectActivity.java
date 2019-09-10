package com.example.jot;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.view.View;
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
        implements ActivityCompat.OnRequestPermissionsResultCallback{
    private static final int REQUEST_BACKUP_EXPORT = 101;
    private static final int REQUEST_BACKUP_IMPORT = 102;

    private static final int REQUEST_CHOOSE_BACKUP_FILE = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_select);

        //create notes recycler
        RecyclerView NotesRecycler = findViewById(R.id.NotesRecycler);

        //attach a generic linear layout manager and item animator
        NotesRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        NotesRecycler.setItemAnimator(new DefaultItemAnimator());

        //create a click event for each recycler note via an adapter
        final NoteSelectAdapter NotesAdapter = new NoteSelectAdapter();
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

                        NoteIO.noteList.moveNote(fromPos, toPos);
                        NoteIO.saveAll(NoteSelectActivity.this);

                        NotesAdapter.notifyDataSetChanged();

                        NoteIO.saveAll(NoteSelectActivity.this);

                        return true;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                        final int pos = viewHolder.getAdapterPosition();

                        //prompt user, double-check if they want to delete
                        AlertDialog deleteAsk = new AlertDialog.Builder(NoteSelectActivity.this)
                                .setTitle("Confirm Delete")
                                .setMessage("Do you want to delete '" + NoteIO.noteList.getNote(pos).getTitle() + "'?")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //remove the item and notify the adapter
                                        NoteIO.noteList.removeNote(pos);
                                        NoteIO.saveAll(NoteSelectActivity.this);

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
                NoteIO.noteList.newNote();
                NoteIO.noteList.selectNote(NoteIO.noteList.getLength()-1);
                NoteIO.saveAll(NoteSelectActivity.this);

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

        //finish by loading in all the notes again
        NoteIO.loadAll(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] p, @NonNull int[] res){
        //process requests to import/export external files
        if(requestCode == REQUEST_BACKUP_EXPORT &&
           res[0] == PackageManager.PERMISSION_GRANTED){
            //back up the notes if you got permission for external file saving
            NoteIO.exportBackup(NoteSelectActivity.this);

        } else if(requestCode == REQUEST_BACKUP_IMPORT &&
                res[0] == PackageManager.PERMISSION_GRANTED){
            //ask the user to choose a file
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            try{
                startActivityForResult(
                        Intent.createChooser(intent, "Select a File to Upload"),
                        REQUEST_CHOOSE_BACKUP_FILE);
            } catch (android.content.ActivityNotFoundException e){
                Toast.makeText(this, "No file manager found; please install one",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if a backup file was selected, process it
        if(requestCode == REQUEST_CHOOSE_BACKUP_FILE && resultCode == RESULT_OK){
            Uri file_uri = data.getData();
            String file_path = file_uri.getEncodedPath();

            NoteIO.importBackup(this, file_path);
        }
    }

    @Override
    public void onDestroy(){
        NoteIO.saveAll(this);
        super.onDestroy();
    }
}
