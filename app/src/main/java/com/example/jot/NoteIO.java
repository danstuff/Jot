package com.example.jot;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    private static final String FILENAME0 = "JotNotes0.dat";
    private static final String FILENAME1 = "JotNotes1.dat";

    private static final int SOFT_SAVE_WAIT_MS = 5000;

    private static boolean already_saved = false;

    private static AppCompatActivity activity;

    public static void setActivity(AppCompatActivity activity){
        NoteIO.activity = activity;
    }

    private static NoteList loadF(String filename){
        NoteList noteList = null;

        try {
            //open a file input stream and read the serializable object
            InputStream fileIn = activity.openFileInput(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            noteList = (NoteList) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return noteList;
    }

    public static NoteList load() {
        NoteList noteList = loadF(FILENAME0);

        if(noteList == null){
            noteList = loadF(FILENAME1);

            if(noteList == null){
                noteList = new NoteList();
                Toast.makeText(activity, "No save file found", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Secondary save file used", Toast.LENGTH_SHORT).show();
            }
        }

        return noteList;
    }

    public static void save(NoteList noteList){
        try {
            //copy the contents of save0 into save1
            FileInputStream sec_in = activity.openFileInput(FILENAME0);
            FileOutputStream sec_out = activity.openFileOutput(FILENAME1, 0);

            byte[] copy_buffer = new byte[1024];
            int len;

            while((len = sec_in.read(copy_buffer)) > 0){
                sec_out.write(copy_buffer, 0, len);
            }

            sec_in.close();
            sec_out.close();

        } catch (Throwable t){
            Toast.makeText(activity, t.toString(), Toast.LENGTH_LONG).show();
        }

        try {
            //overwrite the original save file
            ObjectOutputStream out = new ObjectOutputStream(
                    activity.openFileOutput(FILENAME0, 0));

            out.writeObject(noteList);
            out.close();
        } catch (Throwable t){
            Toast.makeText(activity, t.toString(), Toast.LENGTH_LONG).show();
        }

        //display a toast in the corner to indicate saving
        Toast notif = new Toast(activity);

        //add a nice format to the save toast
        LayoutInflater inf = activity.getLayoutInflater();
        View layout = inf.inflate(R.layout.save_toast,
                (ViewGroup) activity.findViewById(R.id.saveToast));
        notif.setView(layout);

        //setup position and duration
        notif.setDuration(Toast.LENGTH_SHORT);
        notif.setGravity(Gravity.TOP | Gravity.RIGHT, 8, 8);

        //display the toast
        notif.show();
    }

    public static void softSave(NoteList noteList) {
        //avoid spamming the save when this function is polled frequently
        if (already_saved) { return; }

        save(noteList);
        already_saved = true;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override public void run() { already_saved = false; }
        }, SOFT_SAVE_WAIT_MS);
    }

    private static File getBackupFolder(){
        //find documents directory, create new file in a subfolder
        File home_dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS);

        //create directories if they don't exist
        File bup_folder = new File(home_dir.getPath() + File.separator + "jot_backups");
        bup_folder.mkdirs();

        return bup_folder;
    }

    public static void exportBackup(NoteList noteList){
        try {
            DateFormat dtf = new SimpleDateFormat("MM_dd_yy", Locale.US);
            Date today = new Date();

            String stamp = dtf.format(today);
            String bup_name = stamp + ".txt";

            File bup =  new File(getBackupFolder(), bup_name);

            //create output stream
            FileWriter out = new FileWriter(bup);

            //iterate through each note and write it line by line
            for(int i = 0; i < noteList.getLength(); i++){
                Note note = noteList.getNote(i);

                //make sure note titles don't begin with dashes to prevent errors
                String title = note.getTitle();

                while (title.length() > 0 && title.charAt(0) == '-'){
                    title = title.substring(1);
                }

                //output the fixed title
                out.write(title+"\n");

                for(int j = 0; j < note.getLength(); j++){
                    out.write("- " + note.getLine(j) + "\n");
                }

                out.write("\n");
            }

            out.close();

            Toast.makeText(activity, bup_name + " saved in Documents",
                    Toast.LENGTH_SHORT).show();
        } catch (Throwable t){
            Toast.makeText(activity, t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public static NoteList importBackup(String filename){
        NoteList noteList = new NoteList();

        try {
            File bup =  new File(getBackupFolder(), filename);
            BufferedReader reader = new BufferedReader(new FileReader(bup));

            String read_line;

            //append the file data to the current noteList
            while((read_line = reader.readLine()) != null){
                if(read_line.startsWith("- ")){
                    //if line begins with a dash, add it as a note line
                    String content = read_line.substring(2);
                    noteList.getLast().addLine(content);
                } else if(!read_line.isEmpty()){
                    //if the line isn't empty but has no dash it's a note name
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
            File[] bup_files = getBackupFolder().listFiles();

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
