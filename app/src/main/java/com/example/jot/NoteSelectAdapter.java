package com.example.jot;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class NoteSelectAdapter extends
        RecyclerView.Adapter<NoteSelectAdapter.ViewHolder> {
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.list_row, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //fetch note from list; set the holder's title and lines
        final int pos = position;

        Note note = NoteIO.noteList.getNote(pos);
        String line0 = note.getLength() > 0 ? note.getLine(0) : "";

        holder.title.setText(note.getTitle());
        holder.first_line.setText(line0);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //select this note
                NoteIO.noteList.selectNote(pos);

                //switch to the note edit activity
                Intent intent = new Intent(v.getContext(), NoteEditActivity.class);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return NoteIO.noteList.getLength();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, first_line;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            first_line = view.findViewById(R.id.firstLine);
        }
    }
}
