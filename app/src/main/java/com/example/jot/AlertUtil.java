package com.example.jot;

import android.app.AlertDialog;
import android.content.DialogInterface;

import androidx.appcompat.app.AppCompatActivity;

public class AlertUtil{
    public AlertUtil(AppCompatActivity activity){ this.activity = activity; }

    public AppCompatActivity activity;

    //small utility functions for making dialog boxes
    public AlertDialog make(String title, String[] options,
                                   DialogInterface.OnClickListener l){
        return new AlertDialog.Builder(activity)
                .setTitle(title)
                .setItems(options, l)
                .create();
    }

    public AlertDialog make(String title, String message,
                            String positive, DialogInterface.OnClickListener pos,
                            String negative, DialogInterface.OnClickListener neg){
        return new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive, pos)
                .setNegativeButton(negative, neg)
                .create();
    }
}
