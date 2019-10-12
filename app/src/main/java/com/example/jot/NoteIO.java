package com.example.jot;

import android.os.Environment;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NoteIO {
    public static final int NOTE_MAX = 100;
    public static final int CULL_AFTER_DAYS = 10;

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

        for(int i = 0; i < NOTE_MAX; i++){
            Note note = load(getFilename(i));

            if(note != null) { noteList.addNote(note); }
            else { break; }
        }

        return noteList;
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
    }

    public void saveList(NoteList noteList){
        for(int i = 0; i < noteList.getNoteCount(); i++){
            noteList.getNote(i).setFileIndex(i);
            save(noteList.getNote(i));
        }
    }

    public NoteList cycleList(NoteList noteList){
        saveList(noteList);
        return loadList();
    }

    public void cleanLocalDir(NoteList noteList){
        //deletes all files in the local dir that are unused by the notelist
        //assumes the notelist is properly indexed
        String[] filenames = activity.fileList();

        for(int i = noteList.getNoteCount(); i < filenames.length; i++){
            activity.deleteFile(filenames[i]);
        }
    }

    public void cleanBackupDir(){
        DateFormat dtf = new SimpleDateFormat("MM_dd_yy", Locale.US);
        Date today = new Date();

        //find a time before the current date
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DATE, -CULL_AFTER_DAYS);

        Date cull_date = cal.getTime();

        //fetch all backup files
        File[] bup_files = getBackupFolder().listFiles();


        for(int i = 0; i < bup_files.length; i++){
            String date_str = bup_files[i].getName().replace(".txt", "");

            try{
                Date date = dtf.parse(date_str);

                //if bup was made before the cull date, delete it
                if(date.before(cull_date)){
                    bup_files[i].delete();
                } else {
                    //since files are in order, the following files can be ignored
                    break;
                }
            }catch(ParseException e){
                e.printStackTrace();
            }
        }

        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(activity, "Deleted all backups older than " +
                                CULL_AFTER_DAYS + " days", Toast.LENGTH_SHORT).show();
            }
        });
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

    public boolean needBackup(){
        //create a date format and create todays date
        DateFormat dtf = new SimpleDateFormat("MM_dd_yy", Locale.US);
        Date today = new Date();

        //get the most recent backup
        String[] names = getBackupNames();
        String last_name = names[names.length-1].replace(".txt","");

        try{
            Date last_bup = dtf.parse(last_name);

            return today.after(last_bup);
        }catch(ParseException e){
            e.printStackTrace();
        }

        return true;
    }
}
