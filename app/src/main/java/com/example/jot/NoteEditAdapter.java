package com.example.jot;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

public class NoteEditAdapter extends RecyclerView.Adapter<NoteEditAdapter.ViewHolder> {
    private LayoutInflater inflater;

    public NoteEditAdapter(Context ctx) {
        inflater = LayoutInflater.from(ctx);
    }

    @Override
    public NoteEditAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //set the xml formatting of the Recycler's rows to note_row
        View itemView = inflater.inflate(R.layout.note_row, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //fetch note from list; set the holder's LineText entry
        String content_str = NoteIO.noteList.getSelected().getLine(position);
        holder.LineText.setText(content_str);
        holder.LineText.requestFocus();
    }

    @Override
    public int getItemCount() {
        return NoteIO.noteList.getSelected().getLength();
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
                    //update the note when the text is changed
                    NoteIO.noteList.getSelected().setLine(getAdapterPosition(), s.toString());
                }
            });
        }
    }
}
