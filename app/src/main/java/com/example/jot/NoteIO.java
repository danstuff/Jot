package com.example.jot;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteIO {
    private static final String filename = "JotNotes.dat";

    public static NoteList noteList = null;

    public static void loadAll(Context context) {
        try {
            InputStream fileIn = context.openFileInput(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            noteList = (NoteList) in.readObject();
        } catch (Exception e) {
            Toast.makeText(context, filename + " not found, creating a new file", Toast.LENGTH_LONG).show();
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

    public static void exportBackup(Context context){
        try {
            DateFormat dtf = new SimpleDateFormat("MM_dd_yy", Locale.US);
            Date today = new Date();

            String stamp = dtf.format(today);
            String bup_name = "jot_backup_" + stamp + ".txt";

            //find downloads directory, create new file there
            File dl_dir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);

            File bup =  new File(dl_dir, bup_name);

            //create output stream
            FileWriter out = new FileWriter(bup);

            //iterate through each note and write it line by line
            for(int i = 0; i < noteList.getLength(); i++){
                Note note = noteList.getNote(i);

                out.write(note.getTitle()+"\n");

                for(int j = 0; j < note.getLength(); j++){
                    out.write("- " + note.getLine(j) + "\n");
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

    public static void importBackup(Context context, String file_path){
        try {
            System.out.println(file_path);
            File file = new File(file_path);
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String read_line;

            //append the file data to the current noteList
            while((read_line = reader.readLine()) != null){
                if(read_line.startsWith("- ")){
                    String content = read_line.substring(2);
                    noteList.getLast().addLine(content);
                } else if(!read_line.isEmpty()){
                    noteList.newNote();
                    noteList.getLast().setTitle(read_line);
                }
            }

            Toast.makeText(context, file_path + " imported",
                    Toast.LENGTH_SHORT).show();
        } catch (Throwable t){
            Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show();
            t.printStackTrace();
        }
    }
}
