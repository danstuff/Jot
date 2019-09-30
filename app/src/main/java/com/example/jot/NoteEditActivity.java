package com.example.jot;

import android.os.Bundle;
import android.os.Looper;
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

import java.util.Timer;
import java.util.TimerTask;

public class NoteEditActivity extends AppCompatActivity {
    public static final int AUTO_SAVE_INTERVAL_MS = 5000;

    private TextInputEditText TitleInput;

    private RecyclerView LineRecycler;
    private NoteEditAdapter LineAdapter;

    private NoteIO noteIO;

    private Note note;

    private Timer save_timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        noteIO = new NoteIO(this);

        //import the note you selected in NoteSelectActivity
        note = (Note) getIntent().getSerializableExtra(NoteSelectActivity.SELECTED_NOTE_DATA);

        //create title label, set it to the note's title, add a listener for updates
        TitleInput = findViewById(R.id.TitleInput);
        TitleInput.setText(note.getTitle());

        TitleInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int j, int k) {}
            @Override public void onTextChanged(CharSequence s, int i, int j, int k) {}

            @Override
            public void afterTextChanged(Editable editable) {
                note.setTitle(TitleInput.getText().toString());
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
                    String content_str = note.getLine(pos).getContent();
                    holder.LineText.setText(content_str);
                }
            },
            new NoteEditAdapter.NoteUpdateInterface() {
                @Override
                public void onLineUpdate(NoteEditAdapter.ViewHolder holder, Editable edit) {
                    //update the note when the text is changed
                    note.getLine(holder.getAdapterPosition()).setContent(edit.toString());
                }
            },
            new NoteEditAdapter.NoteLengthInterface(){
                @Override
                public int getLength() {
                    return note.getLineCount();
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

                    note.moveLine(fromPos, toPos);

                    LineAdapter.notifyDataSetChanged();
                    return true;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                    //removeNote the item and notify the adapter
                    int pos = viewHolder.getAdapterPosition();

                    note.removeLine(pos);

                    LineAdapter.notifyItemRemoved(pos);
                }
            });
        itHelper.attachToRecyclerView(LineRecycler);

        //button to return to notes list
        AppCompatButton ShowNoteSelect = findViewById(R.id.ShowNoteSelect);
        ShowNoteSelect.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view){
                //return to the notes list
                finish();
            }
        });

        //add a line when the NewLine button is clicked
        AppCompatButton NewLine = findViewById((R.id.NewLine));
        NewLine.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                //add an entry and update the adapter
                note.newLine();

                LineAdapter.notifyDataSetChanged();
                LineRecycler.scrollToPosition(LineAdapter.getItemCount()-1);
            }
        });

        //start a timer that saves the note every 15 seconds
        save_timer = new Timer();
        save_timer.schedule(new TimerTask() {
            @Override public void run() {
                noteIO.save(note);
            }
        }, 0, AUTO_SAVE_INTERVAL_MS);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        noteIO.save(note);

        save_timer.cancel();
    }
}
