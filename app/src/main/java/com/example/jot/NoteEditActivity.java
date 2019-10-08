package com.example.jot;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

public class NoteEditActivity extends AppCompatActivity
        implements GestureDetector.OnDoubleTapListener {
    public static final int AUTO_SAVE_INTERVAL_MS = 10000;

    private GestureDetector gestureDetector;

    private TextView EmptyMessage;

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

        //toggle the empty message off if note is populated
        EmptyMessage = findViewById(R.id.EmptyLinesMessage);

        if(note.getLineCount() > 0){
            EmptyMessage.setVisibility(View.GONE);
        }

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

                //show empty message if note is empty
                if(note.getLineCount() == 0){
                    EmptyMessage.setVisibility(View.VISIBLE);
                }

                return deletedLine.getContent();
            }

            @Override
            public void undoDelete() {
                note.addLine(deletedLine.getContent());
                EmptyMessage.setVisibility(View.GONE);
                LineAdapter.notifyDataSetChanged();
            }
        });

        ItemTouchHelper itHelper = new ItemTouchHelper(itCallback);
        itHelper.attachToRecyclerView(LineRecycler);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override public boolean onDoubleTap(MotionEvent e){
                //add a line when the NewLine button is clicked
                EmptyMessage.setVisibility(View.GONE);

                //add an entry and update the adapter
                note.newLine();

                LineAdapter.notifyDataSetChanged();
                LineRecycler.scrollToPosition(LineAdapter.getItemCount()-1);

                return true;
            }

            @Override public void onLongPress(MotionEvent e){ super.onLongPress(e); }
            @Override public boolean onDoubleTapEvent(MotionEvent e){ return true; }
            @Override public boolean onDown(MotionEvent e){ return true; }
        });
        LineRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                return gestureDetector.onTouchEvent(e);
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

    @Override public boolean onSingleTapConfirmed(MotionEvent e){ return true; }
    @Override public boolean onDoubleTapEvent(MotionEvent e){ return true; }

    @Override
    public boolean onDoubleTap(MotionEvent e){
        //add a line when the NewLine button is clicked
        EmptyMessage.setVisibility(View.GONE);

        //add an entry and update the adapter
        note.newLine();

        LineAdapter.notifyDataSetChanged();
        LineRecycler.scrollToPosition(LineAdapter.getItemCount()-1);

        return true;
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
