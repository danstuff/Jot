package com.example.jot;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

public class NoteEditActivity extends AppCompatActivity {
    public static final int AUTO_SAVE_INTERVAL_MS = 10000;

    private TextInputEditText TitleInput;

    private RecyclerView LineRecycler;
    private NoteEditAdapter LineAdapter;

    private AutoInterval interval;

    private NoteIO noteIO;

    private Note note;
    private NoteLine deletedLine;

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
        ItemTouchHelper.SimpleCallback itCallback = ItemTouchUtil.make(this,
                new ItemTouchUtil.Actions() {
            @Override public void move(int fromPos, int toPos) {
                note.moveLine(fromPos, toPos);
                LineAdapter.notifyDataSetChanged();
            }

            @Override public String delete(int pos) {
                deletedLine = note.getLine(pos);
                note.removeLine(pos);

                LineAdapter.notifyItemRemoved(pos);

                return deletedLine.getContent();
            }

            @Override
            public void undoDelete() {
                note.addLine(deletedLine.getContent());
                LineAdapter.notifyDataSetChanged();
            }
        });

        ItemTouchHelper itHelper = new ItemTouchHelper(itCallback);
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

        //start a timer that saves the note every 5 seconds
        interval = new AutoInterval(new AutoInterval.Task() {
            @Override
            public void run() {
                noteIO.save(note);
            }
        }, AUTO_SAVE_INTERVAL_MS);
        interval.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        interval.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        noteIO.save(note);
        interval.stop();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        noteIO.save(note);
        interval.stop();
    }
}
