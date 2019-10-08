package com.example.jot;

import android.app.Activity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class GestureUtil {
    interface DoubleTap{
        void onDoubleTap();
    }

    static void bindGesture(Activity act, RecyclerView recycler, final DoubleTap doubleTap){
        final GestureDetector gestureDetector = new GestureDetector(act, new GestureDetector.SimpleOnGestureListener(){
            @Override public boolean onDoubleTap(MotionEvent e){
                doubleTap.onDoubleTap();

                return true;
            }

            @Override public void onLongPress(MotionEvent e){ super.onLongPress(e); }
            @Override public boolean onDoubleTapEvent(MotionEvent e){ return true; }
            @Override public boolean onDown(MotionEvent e){ return true; }
        });
        recycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                return gestureDetector.onTouchEvent(e);
            }
        });
    }
}
