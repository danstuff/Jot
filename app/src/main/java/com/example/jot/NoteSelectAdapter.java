package com.example.jot;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class NoteSelectAdapter extends
        RecyclerView.Adapter<NoteSelectAdapter.ViewHolder> {
    public interface OnNoteClickListener {
        //interface for listening to clicks
        void onNoteClick(Note note);
    }

    private OnNoteClickListener listener;

    public NoteSelectAdapter(OnNoteClickListener listener) {
        this.listener = listener;
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
        final Note note = NoteIO.noteList.get(position);
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
                                NoteIO.noteList.remove(pos);
                                NoteIO.saveAll(holder.itemView.getContext());

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
        return NoteIO.noteList.data.size();
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
