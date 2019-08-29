package com.example.jot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class NoteEditActivity extends AppCompatActivity {
    TextInputEditText TitleInput;

    RecyclerView LineRecycler;
    NoteEditAdapter LineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        //title input label and note entry recycler
        TitleInput = findViewById(R.id.TitleInput);
        LineRecycler = findViewById(R.id.LineRecycler);

        //load a note into the view if one was sent by NoteSelectActivity
        Note note = (Note) getIntent().getSerializableExtra("NoteOpened");

        if(note != null) {
            TitleInput.setText(note.title);
        } else {
            note = new Note();
        }

        //configure adapter and misc for lines recycler
        LineAdapter = new NoteEditAdapter(this, note);

        //dragging items up/down rearranges them, left/right deletes them
        ItemTouchHelper itHelper = new ItemTouchHelper(
            new ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recycler,
                                      @NonNull RecyclerView.ViewHolder viewHolder,
                                      @NonNull RecyclerView.ViewHolder target) {
                    int fromPos = viewHolder.getAdapterPosition();
                    int toPos = target.getAdapterPosition();

                    List<String> lines = LineAdapter.note.lines;

                    //remove the variable at fromPos, saving it
                    String fromLine = lines.get(fromPos);
                    lines.remove(fromPos);

                    int size = lines.size();

                    //duplicate the item at the end
                    String last = String.copyValueOf(lines.get(size-1).toCharArray());
                    lines.add(last);

                    //move each item after toPos down 1 in the array
                    for(int i = size-1; i > toPos; i--){
                        String next = lines.get(i-1);
                        lines.set(i, next);
                    }

                    //there are now two instances of variable at toPos;
                    //insert the variable from fromPos at toPos
                    lines.set(toPos, fromLine);

                    //notify the adapter, return true for item successfully moved
                    LineAdapter.notifyDataSetChanged();
                    return true;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                    //remove the item and notify the adapter
                    int pos = viewHolder.getAdapterPosition();
                    LineAdapter.note.lines.remove(pos);
                    LineAdapter.notifyItemRemoved(pos);
                }
            });

        //attach the essentials, including itemTouchHelper and the adapter
        LineRecycler.setLayoutManager( new LinearLayoutManager(getApplicationContext()));
        LineRecycler.setItemAnimator(new DefaultItemAnimator());

        itHelper.attachToRecyclerView(LineRecycler);
        LineRecycler.setAdapter(LineAdapter);

        //button to save file
        AppCompatButton SaveNote = findViewById(R.id.SaveNote);
        SaveNote.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view){ save(); }
        });

        //button to return to notes list
        AppCompatButton ShowNoteSelect = findViewById(R.id.ShowNoteSelect);
        ShowNoteSelect.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view){
                save();

                //switch to the notes list
                startActivity(new Intent(getApplicationContext(), NoteSelectActivity.class));
            }
        });

        //add a line when the NewLine button is clicked
        FloatingActionButton NewLine = findViewById((R.id.NewLine));
        NewLine.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                //add an entry and update the adapter
                LineAdapter.note.lines.add("");
                System.out.println(LineAdapter.note.lines.size());
                LineAdapter.notifyDataSetChanged();
            }
        });
    }

    void save(){
        //update the note title based on text entry and save with NoteIO
        LineAdapter.note.title = TitleInput.getText().toString();
        NoteIO.save(this, LineAdapter.note);
    }
}
