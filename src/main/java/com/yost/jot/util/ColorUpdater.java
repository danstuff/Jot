package com.yost.jot.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.yost.jot.NoteEditAdapter;
import com.yost.jot.NoteSelectAdapter;
import com.yost.jot.R;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ColorUpdater {
    public static final int COLOR_UPDATE_DELAY_MS = 300;

    public static int getColor(String key, String default_val, int default_color, Activity activity){
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(activity);

        String headerColorStr = sPrefs.getString(key, default_val);
        int headerColor = default_color;
        try{
            headerColor = Color.parseColor(headerColorStr);
        } catch (IllegalArgumentException e){
            System.out.println("[WARNING] Could not parse header color, using default");
        }

        return headerColor;
    }

    public static void updateColors(final Activity activity){
        new Timer().schedule(new TimerTask() {
            @Override public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        doColorUpdate(activity);
                    }
                });
            }
        }, COLOR_UPDATE_DELAY_MS);
    }

    private static void doColorUpdate(Activity activity){
        Window window = activity.getWindow();

        //get header and background colors
        int headerColor = getColor("header_color", "black", Color.BLACK, activity);
        int backgroundColor = getColor("background_color", "white", Color.WHITE, activity);

        //find the root view of the activity and set its background color
        ViewGroup rootView = (ViewGroup) window.getDecorView().getRootView();
        rootView.setBackgroundColor(backgroundColor);

        //set settings button color, if one exists
        AppCompatButton optsButton = activity.findViewById(R.id.ViewOptions);
        if(optsButton != null){
            optsButton.setBackgroundTintList(ColorStateList.valueOf(headerColor));
            optsButton.setCompoundDrawableTintList(ColorStateList.valueOf(backgroundColor));
        }

        //set add lines button color, if one exists
        AppCompatButton lineButton = activity.findViewById(R.id.AddLine);
        if(lineButton != null){
            lineButton.setBackgroundTintList(ColorStateList.valueOf(headerColor));
            lineButton.setCompoundDrawableTintList(ColorStateList.valueOf(backgroundColor));
        }

        //set save icon color, if one exists
        AppCompatButton saveIcon = activity.findViewById(R.id.SaveIcon);
        if(saveIcon != null){
            saveIcon.setCompoundDrawableTintList(ColorStateList.valueOf(backgroundColor));
        }

        //set recyclerview background colors
        RecyclerView recyclerNotes = activity.findViewById(R.id.NotesRecycler);
        if(recyclerNotes != null){
            recyclerNotes.setBackgroundColor(backgroundColor);

            //set colors of all child elements
            for(int i = 0; i < recyclerNotes.getChildCount(); i++) {
                NoteSelectAdapter.ViewHolder holder = (NoteSelectAdapter.ViewHolder)
                        recyclerNotes.findViewHolderForAdapterPosition(i);
                assert holder != null;

                holder.title.setTextColor(headerColor);
                holder.first_line.setTextColor(headerColor);
            }
        }

        RecyclerView recyclerLines = activity.findViewById(R.id.LineRecycler);
        if(recyclerLines != null){
            recyclerLines.setBackgroundColor(backgroundColor);

            //set colors of all child elements
            for(int i = 0; i < recyclerLines.getChildCount(); i++) {
                NoteEditAdapter.ViewHolder holder = (NoteEditAdapter.ViewHolder)
                        recyclerLines.findViewHolderForAdapterPosition(i);
                assert holder != null;

                holder.LineText.setTextColor(headerColor);

                holder.Grip.setTextColor(backgroundColor);
                holder.Grip.setBackgroundColor(headerColor);
            }
        }

        //set editText header color, if one exists
        TextInputLayout textLayout = activity.findViewById(R.id.TitleInputLayout);
        if(textLayout != null){
            textLayout.setBackgroundColor(headerColor);
            Objects.requireNonNull(textLayout.getEditText()).setTextColor(backgroundColor);
            textLayout.setDefaultHintTextColor(ColorStateList.valueOf(backgroundColor));
        }

        //set settings menu header color
        TextView settingsHeader = activity.findViewById(R.id.SettingsHeader);
        if(settingsHeader != null){
            settingsHeader.setBackgroundColor(headerColor);
            settingsHeader.setTextColor(backgroundColor);
        }

        //settings background will always be white
        FrameLayout settingsFrame = activity.findViewById(R.id.settings);
        if(settingsFrame != null){
            settingsFrame.setBackgroundColor(Color.WHITE);
        }

        //set status bar color
        activity.getWindow().setStatusBarColor(headerColor);

        //set nav bar color
        window.setNavigationBarColor(headerColor);
    }
}
