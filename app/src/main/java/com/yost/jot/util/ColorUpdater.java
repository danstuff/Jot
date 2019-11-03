package com.yost.jot.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import androidx.preference.PreferenceManager;

import com.yost.jot.R;

public class ColorUpdater {
    private static final float DARKEN_FACTOR = 0.9f;

    private static int darken(int color){
        int r = Math.round(Color.red(color) * DARKEN_FACTOR);
        int g = Math.round(Color.green(color) * DARKEN_FACTOR);
        int b = Math.round(Color.blue(color) * DARKEN_FACTOR);

        return Color.rgb(
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }

    public static void updateColors(Activity activity){
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(activity);

        Window window = activity.getWindow();

        //get header and background colors
        String headerColorStr = sPrefs.getString("header_color", "#000000");
        int headerColor = Color.BLACK;
        try{
            headerColor = Color.parseColor(headerColorStr);
        } catch (IllegalArgumentException e){
            System.out.println("[WARNING] Could not parse header color, using default");
        }

        String backgroundColorStr = sPrefs.getString("background_color", "#FFFFFF");
        int backgroundColor = Color.WHITE;
        try{
            backgroundColor = Color.parseColor(backgroundColorStr);
        } catch (IllegalArgumentException e){
            System.out.println("[WARNING] Could not parse background color, using default");
        }

        //find the root view of the activity and set its background color
        View rootView = window.getDecorView().getRootView();
        rootView.setBackgroundColor(backgroundColor);

        //set settings button color, if one exists
        View optsButton = activity.findViewById(R.id.ViewOptions);
        if(optsButton != null){
            optsButton.setBackgroundTintList(ColorStateList.valueOf(headerColor));
        }

        //set recyclerview background colors
        View recyclerNotes = activity.findViewById(R.id.NotesRecycler);
        if(recyclerNotes != null){
            recyclerNotes.setBackgroundColor(backgroundColor);
        }

        View recyclerLines = activity.findViewById(R.id.LineRecycler);
        if(recyclerLines != null){
            recyclerLines.setBackgroundColor(backgroundColor);
        }

        //set editText header color, if one exists
        View textLayout = activity.findViewById(R.id.TitleInputLayout);
        if(textLayout != null){
            textLayout.setBackgroundColor(headerColor);
        }

        //set settings menu header color
        View settingsHeader = activity.findViewById(R.id.SettingsHeader);
        if(settingsHeader != null){
            settingsHeader.setBackgroundColor(headerColor);
        }

        //set status bar color
        activity.getWindow().setStatusBarColor(headerColor);

        //set nav bar color
        int navbarColor = darken(backgroundColor);
        window.setNavigationBarColor(navbarColor);
    }
}
