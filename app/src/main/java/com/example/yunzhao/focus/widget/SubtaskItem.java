package com.example.yunzhao.focus.widget;

/**
 * Created by Yunzhao on 2018/3/13.
 */

public class SubtaskItem {
    private boolean isDone;
    private String taskName;
    private boolean isTodayTask;

    public SubtaskItem(boolean isDone, String taskName, boolean isTodayTask) {
        this.isDone = isDone;
        this.taskName = taskName;
        this.isTodayTask = isTodayTask;
    }

    public boolean isDone() {
        return isDone;
    }

    public String getTaskName() {
        return taskName;
    }

    public boolean isTodayTask() {
        return isTodayTask;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setTodayTask(boolean todayTask) {
        isTodayTask = todayTask;
    }
}
