package com.example.jot;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NoteIO {
    private static final int CULL_AFTER_DAYS = 10;

    private static final String LOCAL_SAVE_FN = "jot.data";
    private static final String NEW_NOTE_TAG = "`~";

    private AppCompatActivity activity;

    private DateFormat file_date_format;
    private Date today;

    public NoteIO(AppCompatActivity activity){
        this.activity = activity;

        this.file_date_format = new SimpleDateFormat("MM_dd_yy", Locale.US);
        this.today = new Date();
    }

    private NoteList readFile(String filename, FileInputStream fStream){
        NoteList noteList = null;

        try {
            //read in the file line by line
            InputStreamReader inReader = new InputStreamReader(fStream);
            BufferedReader lineReader = new BufferedReader(inReader);

            String read_line;

            while((read_line = lineReader.readLine()) != null){
                if(read_line.startsWith(NEW_NOTE_TAG)){
                    //if the line starts with the new tag, create a new note
                    noteList.newNote().setTitle(read_line.substring(NEW_NOTE_TAG.length()+1));
                } else if(!read_line.isEmpty()){
                    //if line has content but no new tag, add it as a note line
                    noteList.getLast().addLine(read_line);
                }
            }

            inReader.close();
            lineReader.close();
            fStream.close();
        } catch (FileNotFoundException e) {
            System.out.println("File " + filename + " doesn't exist, read ignored.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return noteList;
    }

    public NoteList load(){
        NoteList noteList = null;

        try{
            FileInputStream inStream = activity.openFileInput(LOCAL_SAVE_FN);
            noteList = readFile(LOCAL_SAVE_FN, inStream);
        } catch(Exception e){
            e.printStackTrace();
        }

        return noteList;
    }

    public NoteList importBackup(String bup_name){
        NoteList noteList = null;

        File bup = new File(getBackupFolder(), bup_name);

        try{
            noteList = readFile(bup_name, new FileInputStream(bup));
        } catch(Exception e){
            e.printStackTrace();
        }

        showText(bup_name + " imported");

        return noteList;
    }


    private void writeFile(final NoteList noteList, final FileOutputStream fStream){
        AsyncTask.execute(new Runnable(){
            @Override public void run(){
                try {
                    //create output stream
                    OutputStreamWriter outWriter = new OutputStreamWriter(fStream);

                    //iterate through each note and write it line by line
                    for(int i = 0; i < noteList.getNoteCount(); i++){
                        Note note = noteList.getNote(i);


                        //output the fixed title
                        outWriter.write(NEW_NOTE_TAG + note.getTitle() +"\n");

                        for(int j = note.getLineCount()-1; j >= 0; j--){
                            outWriter.write("- " + note.getLine(j).getContent() + "\n");
                        }

                        outWriter.write("\n");
                    }

                    outWriter.close();
                    fStream.close();
                } catch (Throwable t){
                    t.printStackTrace();
                }
            }
        });

    }

    public void save(NoteList noteList){
        try{
            FileOutputStream outStream = activity.openFileOutput(LOCAL_SAVE_FN, Context.MODE_PRIVATE);
            writeFile(noteList, outStream);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void exportBackup(NoteList noteList){
        Date today = new Date();

        String stamp = file_date_format.format(today);
        final String bup_name = stamp + ".txt";

        File bup =  new File(getBackupFolder(), bup_name);

        try{
            writeFile(noteList, new FileOutputStream(bup));
        } catch(Exception e){
            e.printStackTrace();
        }

        showText(bup_name + " saved in Documents");
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

        if(bup_files == null) return new String[0];

        String[] bup_names = new String[bup_files.length];

        for(int i = 0; i < bup_files.length; i++){
            bup_names[i] = bup_files[i].getName();
        }

        Arrays.sort(bup_names);

        return bup_names;
    }

    public boolean needBackup(){
        //get the most recent backup
        String[] names = getBackupNames();

        if(names.length <= 0) return true;

        String last_name = names[names.length-1].replace(".txt","");

        try{
            //parse the last filename into a date
            Date last_bup = file_date_format.parse(last_name);

            if(last_bup == null) return true;

            //get calendar instances
            Calendar ctoday = Calendar.getInstance();
            ctoday.setTime(today);

            Calendar clast_bup = Calendar.getInstance();
            clast_bup.setTime(last_bup);

            //return true if today is a different day from last backup
            return !(ctoday.get(Calendar.DAY_OF_YEAR) == clast_bup.get(Calendar.DAY_OF_YEAR) &&
                    ctoday.get(Calendar.YEAR) == clast_bup.get(Calendar.YEAR));
        }catch(ParseException e){
            e.printStackTrace();
        }

        return true;
    }

    public void cleanBackupDir() {
        //find a time before the current date
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DATE, -CULL_AFTER_DAYS);

        Date cull_date = cal.getTime();

        //fetch all backup files
        File[] bup_files = getBackupFolder().listFiles();
        if (bup_files == null) return;

        for(int i = 0; i < bup_files.length; i++) {
            //get the main title, which includes a date in the format
            String date_str = bup_files[i].getName().replace(".txt", "");

            try {
                //if bup was made before the cull date, delete it
                if (file_date_format.parse(date_str).before(cull_date)) {
                    bup_files[i].delete();
                } 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        showText("Deleted all backups older than " + CULL_AFTER_DAYS + " days");
    }

    private void showText(final String text){
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
            }
        });
    }
}
