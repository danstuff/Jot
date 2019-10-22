package com.example.jot;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Timer;
import java.util.TimerTask;

public class NoteEditActivity extends AppCompatActivity {
    public static final int AUTO_SAVE_INTERVAL_MS = 10000;

    public static final int FOCUS_LINE_DELAY_MS = 5;

    public static final int SAVE_ICON_APPEAR_MS = 500;
    public static final float SAVE_ICON_FADE_RATE = 0.95f;

    private TextView EmptyMessage;

    private TextInputEditText TitleInput;

    private RecyclerView LineRecycler;
    private NoteEditAdapter LineAdapter;

    private AutoInterval interval;

    private NoteIO noteIO;

    private NoteList noteList;

    private Note note;
    private NoteLine deletedLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        //load in the noteList
        noteIO = new NoteIO(this);

        noteList = noteIO.load(new NoteList());

        //take the noteIndex from the intent and fill the note object
        int noteIndex = (int) getIntent().getSerializableExtra(NoteSelectActivity.SELECTED_NOTE_INDEX);
        note = noteList.getNote(noteIndex);

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

            @Override public void afterTextChanged(Editable editable) {
                Editable title_text  = TitleInput.getText();
                if(title_text != null){
                    note.setTitle(title_text.toString());
                }
            }
        });

        //focus the title if it's empty
        if(note.getTitle().isEmpty()){
            TitleInput.requestFocus();

            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            if(imm == null){ return; }

            //open the keyboard
            imm.showSoftInput(TitleInput,0);
        }

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
        ItemTouchUtil.bind(this,
                new ItemTouchUtil.Actions() {
            @Override public void move(int fromPos, int toPos) {
                note.moveLine(fromPos, toPos);
                LineAdapter.notifyItemMoved(fromPos, toPos);
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
        }, LineRecycler);

        //double tap creates new lines
        GestureUtil.bindGesture(this, LineRecycler, new GestureUtil.DoubleTap() {
            @Override public void onDoubleTap() {
                EmptyMessage.setVisibility(View.GONE);

                //add an entry and update the adapter
                note.newLine();

                LineAdapter.notifyDataSetChanged();

                new Timer().schedule(new TimerTask() {
                    @Override public void run() {
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                //focus on the new element
                                LineRecycler.scrollToPosition(0);
                                RecyclerView.LayoutManager lm = LineRecycler.getLayoutManager();

                                if(lm == null){ return; }

                                View LineView = lm.findViewByPosition(0);

                                if(LineView == null){ return; }

                                EditText LineText = LineView.findViewById(R.id.LineText);
                                LineText.requestFocus();

                                InputMethodManager imm = (InputMethodManager)
                                        getSystemService(Context.INPUT_METHOD_SERVICE);

                                if(imm == null){ return; }

                                //open the keyboard
                                imm.showSoftInput(LineText,0);
                            }
                        });
                    }
                }, FOCUS_LINE_DELAY_MS);
            }
        });

        //start a timer that saves the note every 5 seconds
        interval = new AutoInterval(new AutoInterval.Task() {
            @Override public void run() {
                save();
            }
        }, AUTO_SAVE_INTERVAL_MS);
        interval.start();
    }

    private void save(){
        noteIO.save(noteList);

        //fade in the save icon
        final View SaveIcon  = findViewById(R.id.SaveIcon);
        SaveIcon.setAlpha(1.0f);

        //use a timer to fade out the icon
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override public void run() {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        //tween the alpha towards 0
                        float a = SaveIcon.getAlpha()*SAVE_ICON_FADE_RATE;

                        //cancel the timer when close to 0
                        if(a < 0.01) {
                            SaveIcon.setAlpha(0);
                            timer.cancel();
                        } else {
                            SaveIcon.setAlpha(a);
                        }
                    }
                });
            }
        }, SAVE_ICON_APPEAR_MS, 10);
    }

    @Override
    public void onResume() {
        super.onResume();
        interval.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        save();
        interval.stop();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        save();
        interval.stop();
    }
}
