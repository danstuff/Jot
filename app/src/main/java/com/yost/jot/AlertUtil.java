package com.yost.jot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class AlertUtil{
    //small utility functions for making dialog boxes
    public static void make(Activity activity, String title, String[] options,
                                   DialogInterface.OnClickListener l){
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setItems(options, l)
                .create();
        dialog.show();
    }
}
