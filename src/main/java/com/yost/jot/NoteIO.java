package com.yost.jot;

import android.content.Context;
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NoteIO {
    private static final String LOCAL_SAVE_FN = "jot.data";
    private static final String NEW_NOTE_TAG = "`~";

    private AppCompatActivity activity;

    private DateFormat file_date_format;
    private Date today;

    private NoteList saveList;
    private FileOutputStream saveStream;

    public NoteIO(AppCompatActivity activity){
        this.activity = activity;

        this.file_date_format = new SimpleDateFormat("MM_dd_yy - HH:mm", Locale.US);
        this.today = new Date();
    }

    private NoteList readFile(NoteList noteList, FileInputStream fStream){
        try {
            //read in the file line by line
            InputStreamReader inReader = new InputStreamReader(fStream);
            BufferedReader lineReader = new BufferedReader(inReader);

            String read_line;

            NoteList buffer = new NoteList();

            while((read_line = lineReader.readLine()) != null){
                if(read_line.startsWith(NEW_NOTE_TAG)){
                    //if the line starts with the new tag, create a new note
                    buffer.newNote().setTitle(read_line.substring(NEW_NOTE_TAG.length()));
                } else if(!read_line.isEmpty()){
                    //if line has content but no new tag, add it as a note line
                    buffer.getLast().addLine(read_line);
                }
            }

            inReader.close();
            lineReader.close();
            fStream.close();

            if(buffer.getNoteCount() > 0){
                return buffer;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return noteList;
    }

    public NoteList load(NoteList noteList){
        try{
            FileInputStream inStream = activity.openFileInput(LOCAL_SAVE_FN);
            return readFile(noteList, inStream);
        } catch (FileNotFoundException e) {
            System.out.println("File " + LOCAL_SAVE_FN + " doesn't exist, read ignored.");
        } catch(Exception e){
            e.printStackTrace();
        }

        return noteList;
    }

    public NoteList importBackup(NoteList noteList, String bup_name){
        File bup = new File(getBackupFolder(), bup_name);

        try{
            showText(bup_name + " imported");
            return readFile(noteList, new FileInputStream(bup));
        } catch(Exception e){
            e.printStackTrace();
        }

        return noteList;
    }


    private void writeFile(){
        try {
            //create output stream
            OutputStreamWriter outWriter = new OutputStreamWriter(saveStream);
            outWriter.write("");

            //iterate through each note and write it line by line
            for(int i = 0; i < saveList.getNoteCount(); i++){
                Note note = saveList.getNote(i);

                //output the fixed title
                outWriter.write(NEW_NOTE_TAG + note.getTitle() +"\n");

                for(int j = note.getLineCount()-1; j >= 0; j--){
                    outWriter.write(note.getLine(j).getContent() + "\n");
                }

                outWriter.write("\n");
            }

            saveStream.flush();
            outWriter.flush();

            outWriter.close();
            saveStream.close();
        } catch (Throwable t){
            t.printStackTrace();
        }
    }

    public void save(NoteList noteList){
        try{
            saveList = noteList;
            saveStream = activity.openFileOutput(LOCAL_SAVE_FN, Context.MODE_PRIVATE);

            writeFile();
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
            saveList = noteList;
            saveStream = new FileOutputStream(bup);

            writeFile();
        } catch(FileNotFoundException e) {
            System.out.println("Backup file " + bup_name + " not found");
        } catch(Exception e){
            e.printStackTrace();
        }

        showText(bup_name + " saved in Documents");
    }


    private File getBackupFolder(){
        //find documents directory, create new file in a subfolder
        File home_dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS);
        System.out.println(home_dir.toString());

        //create directories if they don't exist
        File bup_folder = new File(home_dir.getPath() + File.separator + "jot_backups");

        if(!bup_folder.exists()){
            bup_folder.mkdirs();
        }

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
        String last_day = last_name.split(" - ")[0];

        String todays_name = file_date_format.format(new Date());
        String todays_day = todays_name.split(" - ")[0];

        return last_day.equals(todays_day);
    }

    public void cleanBackupDir(int cull_after_days) {
        //find a time before the current date
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DATE, -cull_after_days);

        Date cull_date = cal.getTime();

        //fetch all backup files
        File[] bup_files = getBackupFolder().listFiles();
        if (bup_files == null) return;

        for(int i = 0; i < bup_files.length; i++) {
            //get the main title, which includes a date in the format
            String date_str = bup_files[i].getName().replace(".txt", "");

            try {
                //if bup was made before the cull date, delete it
                Date parsed = file_date_format.parse(date_str);
                if (parsed != null && parsed.before(cull_date)) {
                    bup_files[i].delete();
                } 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        showText("Deleted all backups older than " + cull_after_days + " days");
    }


    private void showText(final String text){
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
            }
        });
    }
}
