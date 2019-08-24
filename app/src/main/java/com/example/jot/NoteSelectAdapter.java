package com.example.jot;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class NoteSelectAdapter extends
        RecyclerView.Adapter<NoteSelectAdapter.ViewHolder> {
    public interface OnNoteClickListener {
        //interface for listening to clicks
        void onNoteClick(Note note);
    }

    private OnNoteClickListener listener;
    private List<Note> notesList;

    public NoteSelectAdapter(List<Note> notesList, OnNoteClickListener listener) {
        this.listener = listener;
        this.notesList = notesList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.list_row, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //fetch note from list; set the holder's title and lines
        final Note note = notesList.get(position);
        final int pos = position;

        String line0 = note.lines.size() > 0 ? note.lines.get(0) : "";

        holder.title.setText(note.title);
        holder.first_line.setText(line0);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onNoteClick(note);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v){
                AlertDialog deleteAsk = new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Confirm Delete")
                        .setMessage("Do you want to delete '" + note.title + "'?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                notesList.remove(pos);
                                NoteIO.delete(holder.itemView.getContext(), note);
                                notifyItemRemoved(pos);

                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface d, int i) { d.dismiss(); }
                        })
                        .create();
                deleteAsk.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notesList.size();
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
