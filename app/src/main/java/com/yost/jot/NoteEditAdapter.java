package com.yost.jot;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NoteEditAdapter extends RecyclerView.Adapter<NoteEditAdapter.ViewHolder> {
    public interface NoteBindInterface {
        void onBindNote(ViewHolder holder, int pos);
    }

    public interface NoteUpdateInterface {
        void onLineUpdate(ViewHolder holder, Editable s);
    }

    public interface NoteLengthInterface{
        int getLength();
    }

    private NoteBindInterface bindInterface;
    private NoteUpdateInterface updateInterface;
    private NoteLengthInterface lengthInterface;

    public NoteEditAdapter(NoteBindInterface bi, NoteUpdateInterface up, NoteLengthInterface le) {
        bindInterface = bi;
        updateInterface = up;
        lengthInterface = le;
    }

    @Override
    public NoteEditAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //set the xml formatting of the Recycler's rows to note_row
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        bindInterface.onBindNote(holder, position);
    }

    @Override
    public int getItemCount() {
        return lengthInterface.getLength();
    }

    public class ViewHolder extends RecyclerView.ViewHolder /*implements View.OnFocusChangeListener */ {
        protected EditText LineText;

        public ViewHolder(View view) {
            super(view);

            //add a text change listener to the line text
            LineText = view.findViewById(R.id.LineText);
            LineText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int i, int j, int k) {}
                @Override public void onTextChanged(CharSequence s, int i, int j, int k){}

                @Override
                public void afterTextChanged(Editable s) {
                    updateInterface.onLineUpdate(ViewHolder.this, s);
                }
            });
        }
    }
}
