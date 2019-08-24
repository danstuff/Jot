package com.example.jot;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class NoteIO {
    public static Note load(Context context, String filename) {
        Note note = null;

        try {
            InputStream fileIn = context.openFileInput(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            note = (Note) in.readObject();
            Note.last_id = note.id;

        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }

        return note;
    }

    public static void save(Context context, Note note) {
        try {
            if(note.title.length() <= 0) {
                throw new Throwable("Please specify a title");
            }
            String filename = "Note" + note.id + ".dat";

            ObjectOutputStream out = new ObjectOutputStream(
                    context.openFileOutput(filename, 0));

            out.writeObject(note);
            out.close();

            Toast.makeText(context, note.title + " saved",
                    Toast.LENGTH_SHORT).show();
        } catch (Throwable t){
            Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public static void delete(Context context, Note note){
        String filename = "Note" + note.id + ".dat";
        File file = new File(context.getFilesDir(), filename);
        file.delete();
    }

    public static void backup(Context context, List<Note> notes){
        try {
            //find downloads directory, create new file there
            File dl_dir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);

            File bup =  new File(dl_dir, "jot_backup.txt");

            //create output stream
            FileWriter out = new FileWriter(bup);

            //iterate through each note and write it line by line
            for(int i = 0; i < notes.size(); i++){
                Note note = notes.get(i);

                out.write(note.title+"\n");

                for(int j = 0; j < note.lines.size(); j++){
                    out.write("- " + note.lines.get(j) + "\n");
                }

                out.write("\n");
            }

            out.close();

            Toast.makeText(context, "jot_backup.txt saved in Downloads",
                    Toast.LENGTH_SHORT).show();
        } catch (Throwable t){
            Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
