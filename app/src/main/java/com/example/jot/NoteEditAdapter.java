package com.example.jot;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class NoteEditAdapter extends RecyclerView.Adapter<NoteEditAdapter.ViewHolder> {
    private LayoutInflater inflater;

    public Note note;

    public NoteEditAdapter(Context ctx, Note note) {
        inflater = LayoutInflater.from(ctx);
        this.note = note;
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
        String content_str = note.lines.get(position);
        holder.LineText.setText(content_str);
    }

    @Override
    public int getItemCount() {
        return note.lines.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected EditText LineText;

        public ViewHolder(View view) {
            super(view);

            //add a text change listener to the line text
            LineText = view.findViewById(R.id.LineText);
            LineText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void afterTextChanged(Editable s) {
                    //update the note when the text is changed
                    int position = getAdapterPosition();
                    note.lines.set(position, s.toString());
                }
            });
        }
    }
}
