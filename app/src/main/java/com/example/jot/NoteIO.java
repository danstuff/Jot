package com.example.jot;

import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
    public AppCompatActivity activity;

    public NoteIO(AppCompatActivity activity){ this.activity = activity; }

    public String getFilename(int index){
        return index + ".dat";
    }

    public Note load(String filename){
        Note note = null;

        try {
            //open a file input stream and read the serializable object
            InputStream fileIn = activity.openFileInput(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            note = (Note) in.readObject();

            fileIn.close();
        } catch (FileNotFoundException e) {
            System.out.println("File " + filename + " doesn't exist, load ignored.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return note;
    }

    public NoteList loadList() {
        NoteList noteList = new NoteList();

        for(int i = 0; i < 1000; i++){
            Note note = load(getFilename(i));

            if(note != null) { noteList.addNote(note); }
            else { break; }
        }

        return noteList;
    }

    public void delete(String filename){
        activity.deleteFile(filename);
    }

    public void save(Note note){
        try {
            //overwrite the original save file
            ObjectOutputStream out = new ObjectOutputStream(
                    activity.openFileOutput(getFilename(note.getFileIndex()), 0));

            out.writeObject(note);
            out.close();
        } catch (Throwable t){
            t.printStackTrace();
        }

        //ensure the Toast runs on the UI thread
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                saveToast();
            }
        });
    }

    public void saveList(NoteList noteList){
        for(int i = 0; i < noteList.getNoteCount(); i++){
            noteList.getNote(i).setFileIndex(i);
            save(noteList.getNote(i));
        }

        //ensure the Toast runs on the UI thread
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                saveToast();
            }
        });
    }

    public NoteList cycleList(NoteList noteList){
        saveList(noteList);
        return loadList();
    }

    private File getBackupFolder(){
        //find documents directory, create new file in a subfolder
        File home_dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS);

        //create directories if they don't exist
        File bup_folder = new File(home_dir.getPath() + File.separator + "jot_backups");
        bup_folder.mkdirs();

        return bup_folder;
    }

    public String[] getBackupNames(){
        //return all the names of the files in the bup folder as a string array
        File[] bup_files = getBackupFolder().listFiles();
        String[] bup_names = new String[bup_files.length];

        for(int i = 0; i < bup_files.length; i++){
            bup_names[i] = bup_files[i].getName();
        }

        return bup_names;
    }

    public void exportBackup(NoteList noteList){
        try {
            DateFormat dtf = new SimpleDateFormat("MM_dd_yy", Locale.US);
            Date today = new Date();

            String stamp = dtf.format(today);
            final String bup_name = stamp + ".txt";

            File bup =  new File(getBackupFolder(), bup_name);

            //create output stream
            FileWriter out = new FileWriter(bup);

            //iterate through each note and write it line by line
            for(int i = 0; i < noteList.getNoteCount(); i++){
                Note note = noteList.getNote(i);

                //make sure note titles don't begin with dashes to prevent errors
                String title = note.getTitle();

                while (title.length() > 0 && title.charAt(0) == '-'){
                    title = title.substring(1);
                }

                //output the fixed title
                out.write(title+"\n");

                for(int j = 0; j < note.getLineCount(); j++){
                    out.write("- " + note.getLine(j).getContent() + "\n");
                }

                out.write("\n");
            }

            out.close();

            activity.runOnUiThread(new Runnable() {
                @Override public void run() {
                    Toast.makeText(activity, bup_name + " saved in Documents",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Throwable t){
            t.printStackTrace();
        }
    }

    public NoteList importBackup(final String filename){
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
                    //if the line isn't empty but has no dash it's a note name, create it
                    noteList.newNote().setTitle(read_line);
                }
            }
            activity.runOnUiThread(new Runnable() {
                @Override public void run() {
                    Toast.makeText(activity, filename + " imported",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Throwable t){
            t.printStackTrace();
        }

        return noteList;
    }

    private void saveToast(){
        //display a toast in the corner to indicate saving
        Toast notification = new Toast(activity);

        //add a nice format to the save toast
        LayoutInflater inf = activity.getLayoutInflater();
        View layout = inf.inflate(R.layout.save_toast,
                (ViewGroup) activity.findViewById(R.id.saveToast));
        notification.setView(layout);

        //setup position and duration
        notification.setDuration(Toast.LENGTH_SHORT);
        notification.setGravity(Gravity.TOP | Gravity.END, 8, 8);

        //display the toast
        notification.show();
    }
}
