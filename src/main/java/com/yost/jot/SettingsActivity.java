package com.yost.jot;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.yost.jot.util.AlertUtil;
import com.yost.jot.util.ColorUpdater;

import java.util.Timer;
import java.util.TimerTask;

public class SettingsActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback{
    private static final int REQUEST_BACKUP_EXPORT = 101;
    private static final int REQUEST_BACKUP_IMPORT = 102;
    private static final int REQUEST_BACKUP_CULL = 103;

    private NoteIO noteIO;
    private NoteList noteList;

    public static SettingsActivity settingsInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        settingsInstance = this;

        noteIO = new NoteIO(this);
        noteList = noteIO.load(new NoteList());

        ColorUpdater.updateColors(SettingsActivity.this);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference headerColor = findPreference("header_color");
            if(headerColor != null){
                headerColor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        ColorUpdater.updateColors(settingsInstance);
                        return true;
                    }
                });
            }

            Preference backgroundColor = findPreference("background_color");
            if(backgroundColor != null){
                backgroundColor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        ColorUpdater.updateColors(settingsInstance);
                        return true;
                    }
                });
            }

            Preference backup = findPreference("backup_all_notes");
            if(backup != null){
                backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //request external write permissions; later, back up the notes
                        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        settingsInstance.requestPermissions(perms, REQUEST_BACKUP_EXPORT);
                        return true;
                    }
                });
            }

            Preference recover = findPreference("backup_recover_notes");
            if(recover != null){
                recover.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //request external read permissions; later, recover from backup
                        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        settingsInstance.requestPermissions(perms, REQUEST_BACKUP_IMPORT);
                        return true;
                    }
                });
            }

            Preference deleteOld = findPreference("backup_delete_old_notes");
            if(deleteOld != null){
                deleteOld.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        //request external read permissions; later, recover from backup
                        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        settingsInstance.requestPermissions(perms, REQUEST_BACKUP_CULL);
                        return true;
                    }
                });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int c, @NonNull String[] p, @NonNull int[] r) {
        if (c == REQUEST_BACKUP_EXPORT && r[0] == PackageManager.PERMISSION_GRANTED) {
            //back up the notes if you got permission for external file saving
            noteIO.exportBackup(noteList);

        } else if (c == REQUEST_BACKUP_IMPORT && r[0] == PackageManager.PERMISSION_GRANTED) {
            //fetch all backup names and ask user to pick one
            final String[] options = noteIO.getBackupNames();

            AlertUtil.make(SettingsActivity.this,
                    "Choose a Backup File", options,
                    new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialogInterface, int i) {
                            //import the selected file
                            noteList = noteIO.importBackup(noteList, options[i]);

                            //save everything
                            noteIO.save(noteList);

                        }
                    });

        } else if (c == REQUEST_BACKUP_CULL && r[0] == PackageManager.PERMISSION_GRANTED) {
            SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            int cull_days = sPrefs.getInt("backup_delete_old_notes", 10);

            if(cull_days > 0){
                noteIO.cleanBackupDir(cull_days);
            }
        }
    }

}