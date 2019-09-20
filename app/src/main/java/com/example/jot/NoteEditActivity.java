package com.example.jot;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

public class NoteEditActivity extends AppCompatActivity {
    TextInputEditText TitleInput;

    RecyclerView LineRecycler;
    NoteEditAdapter LineAdapter;

    NoteList noteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        //start by loading in all the notes
        NoteIO.setActivity(this);
        noteList = NoteIO.load();

        noteList.print();

        //create title label, set it to the note's title, add a listener for updates
        TitleInput = findViewById(R.id.TitleInput);
        TitleInput.setText(noteList.getSelected().getTitle());

        TitleInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int j, int k) {}
            @Override public void onTextChanged(CharSequence s, int i, int j, int k) {}

            @Override
            public void afterTextChanged(Editable editable) {
                noteList.getSelected().setTitle(TitleInput.getText().toString());
                NoteIO.softSave(noteList);
            }
        });

        //find line recycler
        LineRecycler = findViewById(R.id.LineRecycler);

        //create/attach an adapter for the recycler
        LineAdapter = new NoteEditAdapter(
            new NoteEditAdapter.NoteBindInterface() {
                @Override
                public void onBindNote(NoteEditAdapter.ViewHolder holder, int pos) {
                    //fetch note from list; set the holder's LineText entry
                    String content_str = noteList.getSelected().getLine(pos);
                    holder.LineText.setText(content_str);
                }
            },
            new NoteEditAdapter.NoteUpdateInterface() {
                @Override
                public void onLineUpdate(NoteEditAdapter.ViewHolder holder, Editable edit) {
                    //update the note when the text is changed
                    noteList.getSelected().setLine(holder.getAdapterPosition(), edit.toString());
                    NoteIO.softSave(noteList);
                }
            },
            new NoteEditAdapter.NoteLengthInterface(){
                @Override
                public int getLength() {
                    return noteList.getSelected().getLength();
                }
            });
        LineRecycler.setAdapter(LineAdapter);

        //attach a generic linear layout manager and item animator
        LineRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        LineRecycler.setItemAnimator(new DefaultItemAnimator());

        //dragging recycler items up/down rearranges them, left/right deletes them
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

                    noteList.getSelected().moveLine(fromPos, toPos);
                    NoteIO.softSave(noteList);

                    LineAdapter.notifyDataSetChanged();
                    return true;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                    //removeNote the item and notify the adapter
                    int pos = viewHolder.getAdapterPosition();

                    noteList.getSelected().removeLine(pos);
                    NoteIO.softSave(noteList);

                    LineAdapter.notifyItemRemoved(pos);
                }
            });
        itHelper.attachToRecyclerView(LineRecycler);

        //button to return to notes list
        AppCompatButton ShowNoteSelect = findViewById(R.id.ShowNoteSelect);
        ShowNoteSelect.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view){
                //switch to the notes list
                startActivity(new Intent(getApplicationContext(), NoteSelectActivity.class));
            }
        });

        //add a line when the NewLine button is clicked
        AppCompatButton NewLine = findViewById((R.id.NewLine));
        NewLine.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                //add an entry and update the adapter
                noteList.getSelected().newLine();
                NoteIO.softSave(noteList);

                LineAdapter.notifyDataSetChanged();
                LineRecycler.scrollToPosition(LineAdapter.getItemCount()-1);
            }
        });
    }

    @Override
    public void onDestroy(){
        NoteIO.save(noteList);
        super.onDestroy();
    }
}
