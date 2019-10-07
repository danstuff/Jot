package com.example.jot;

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

    public static void make(Activity activity, String title, String message,
                            String positive, DialogInterface.OnClickListener pos,
                            String negative, DialogInterface.OnClickListener neg){
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive, pos)
                .setNegativeButton(negative, neg)
                .create();
        dialog.show();
    }
}
