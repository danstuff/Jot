package com.example.jot;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private static final String FILENAME = "JotNotes.dat";
    private static final int SOFT_SAVE_WAIT_MS = 2500;

    private static boolean already_saving = false;

    private static AppCompatActivity activity;

    public static void setActivity(AppCompatActivity activity){
        NoteIO.activity = activity;
    }

    public static NoteList load() {
        NoteList noteList = null;

        try {
            InputStream fileIn = activity.openFileInput(FILENAME);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            noteList = (NoteList) in.readObject();
        } catch (Exception e) {
            Toast.makeText(activity, e.toString(), Toast.LENGTH_LONG).show();
        }

        if(noteList == null){
            noteList = new NoteList();
        }

        return noteList;
    }

    public static void save(NoteList noteList){
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    activity.openFileOutput(FILENAME, 0));

            out.writeObject(noteList);
            out.close();

            LayoutInflater inf = activity.getLayoutInflater();
            View layout = inf.inflate(R.layout.save_toast,
                    (ViewGroup) activity.findViewById(R.id.saveToast));

            Toast notif = new Toast(activity);

            notif.setView(layout);
            notif.setDuration(Toast.LENGTH_SHORT);
            notif.setGravity(Gravity.TOP | Gravity.RIGHT, 8, 8);
            notif.show();

        } catch (Throwable t){
            Toast.makeText(activity, t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public static void softSave(final NoteList noteList) {
        //avoid spamming the save when this function is polled frequently
        if (already_saving) { return; }

        already_saving = true;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                save(noteList);

                already_saving = false;
            }
        }, SOFT_SAVE_WAIT_MS);
    }

    public static void exportBackup(NoteList noteList){
        try {
            DateFormat dtf = new SimpleDateFormat("MM_dd_yy", Locale.US);
            Date today = new Date();

            String stamp = dtf.format(today);
            String bup_name = stamp + ".txt";

            //find downloads directory, create new file in a subfolder
            File dl_dir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);

            File bup_folder = new File(dl_dir.getPath() + "\\jot_backups\\");
            bup_folder.mkdirs();

            File bup =  new File(bup_folder, bup_name);

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

            Toast.makeText(activity, bup_name + " saved in Downloads",
                    Toast.LENGTH_SHORT).show();
        } catch (Throwable t){
            Toast.makeText(activity, t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public static NoteList importBackup(String filename){
        NoteList noteList = new NoteList();

        try {
            File dl_dir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);

            File bup_folder = new File(dl_dir.getPath() + "\\jot_backups\\");
            bup_folder.mkdirs();

            File bup =  new File(bup_folder, filename);
            BufferedReader reader = new BufferedReader(new FileReader(bup));

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

            Toast.makeText(activity, filename + " imported",
                    Toast.LENGTH_SHORT).show();
        } catch (Throwable t){
            Toast.makeText(activity, t.toString(), Toast.LENGTH_LONG).show();
            t.printStackTrace();
        }

        return noteList;
    }

    public static String[] getBackupNames(){
        String[] bup_names = new String[0];

        try {
            File dl_dir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);

            File bup_folder = new File(dl_dir.getPath() + "\\jot_backups\\");
            bup_folder.mkdirs();

            File[] bup_files = bup_folder.listFiles();

            bup_names = new String[bup_files.length];
            for(int i = 0; i < bup_files.length; i++){
                bup_names[i] = bup_files[i].getName();
            }
        } catch (Throwable t){
            Toast.makeText(activity, t.toString(), Toast.LENGTH_LONG).show();
            t.printStackTrace();
        }

        return bup_names;
    }
}
