package com.yost.jot;

import java.util.Timer;
import java.util.TimerTask;

public class AutoInterval {
    interface Task {
        void run();
    }

    private Timer timer;

    private Task task;
    private int interval_ms;

    private boolean timer_on = false;

    public AutoInterval(Task task, int interval_ms){
        this.task = task;
        this.interval_ms = interval_ms;
    }

    public void start(){
        if(!timer_on){
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    task.run();
                }
            }, 0, interval_ms);

            timer_on = true;
        }
    }

    public void stop(){
        if(timer_on){
            timer.cancel();
            timer_on = false;
        }
    }
}
