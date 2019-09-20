package com.example.jot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class NoteSelectAdapter extends
        RecyclerView.Adapter<NoteSelectAdapter.ViewHolder> {
    public interface NoteBindInterface{
        void onBindNote(ViewHolder holder, final int position);
    }

    public interface NoteLengthInterface{
        int getLength();
    }

    private NoteBindInterface bindInterface;
    private NoteLengthInterface lengthInterface;

    public NoteSelectAdapter(NoteBindInterface bi, NoteLengthInterface le) {
        bindInterface = bi;
        lengthInterface = le;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.list_row, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        bindInterface.onBindNote(holder, position);
    }

    @Override
    public int getItemCount() {
        return lengthInterface.getLength();
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
