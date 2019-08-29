package com.example.jot;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NoteIO {
    private static final String filename = "JotNotes.dat";

    public static NoteList noteList = null;

    public static void loadAll(Context context) {
        try {
            InputStream fileIn = context.openFileInput(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            noteList = (NoteList) in.readObject();
        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }

        if(noteList == null){
            noteList = new NoteList();
        }
    }

    public static void saveAll(Context context) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    context.openFileOutput(filename, 0));

            out.writeObject(noteList);
            out.close();

            Toast.makeText(context, "All notes saved",
                    Toast.LENGTH_SHORT).show();
        } catch (Throwable t){
            Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public static void backup(Context context){
        try {
            DateFormat dtf = new SimpleDateFormat("MM/dd/yy");
            Date today = new Date();

            String stamp = dtf.format(today);
            String bup_name = "jot_backup " + stamp + ".txt";

            //find downloads directory, create new file there
            File dl_dir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);

            File bup =  new File(dl_dir, bup_name);

            //create output stream
            FileWriter out = new FileWriter(bup);

            //iterate through each note and write it line by line
            for(int i = 0; i < noteList.data.size(); i++){
                Note note = noteList.get(i);

                out.write(note.title+"\n");

                for(int j = 0; j < note.lines.size(); j++){
                    out.write("- " + note.lines.get(j) + "\n");
                }

                out.write("\n");
            }

            out.close();

            Toast.makeText(context, bup_name + " saved in Downloads",
                    Toast.LENGTH_SHORT).show();
        } catch (Throwable t){
            Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
