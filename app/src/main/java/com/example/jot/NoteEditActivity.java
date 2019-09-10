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

        //load the selected note into the view
        TitleInput.setText(NoteIO.noteList.getSelected().getTitle());

        //configure adapter and misc for lines recycler
        LineAdapter = new NoteEditAdapter(this);

        //dragging items up/down rearranges them, left/right deletes them
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
                    NoteIO.noteList.getSelected().moveLine(fromPos, toPos);

                    LineAdapter.notifyDataSetChanged();
                    return true;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                    //removeNote the item and notify the adapter
                    int pos = viewHolder.getAdapterPosition();
                    NoteIO.noteList.getSelected().removeLine(pos);

                    LineAdapter.notifyItemRemoved(pos);
                }
            });

        //attach the essentials, including itemTouchHelper and the adapter
        LineRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
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
                NoteIO.noteList.getSelected().newLine();
                LineAdapter.notifyDataSetChanged();
                LineRecycler.scrollToPosition(LineAdapter.getItemCount()-1);
            }
        });
    }

    @Override
    public void onDestroy(){
        save();
        super.onDestroy();
    }

    void save(){
        //update the note title based on text entry and save with NoteIO
        NoteIO.noteList.getSelected().setTitle(TitleInput.getText().toString());
        NoteIO.saveAll(this);
    }


}
