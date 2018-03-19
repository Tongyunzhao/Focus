package com.example.yunzhao.focus;

/**
 * Created by Yunzhao on 2018/3/13.
 */

public class TaskItem {
    private long ID;
    private String taskName;
    private boolean isDone;
    private boolean isTodayTask;
    private String description;
    private long lastMoveTime;

    public TaskItem(long ID, String taskName, boolean isDone, boolean isTodayTask, String description, long lastMoveTime) {
        this.ID = ID;
        this.taskName = taskName;
        this.isDone = isDone;
        this.isTodayTask = isTodayTask;
        this.description = description;
        this.lastMoveTime = lastMoveTime;
    }

    public TaskItem(boolean isDone, String taskName, boolean isTodayTask, long lastMoveTime) {
        this.isDone = isDone;
        this.taskName = taskName;
        this.isTodayTask = isTodayTask;
        this.lastMoveTime = lastMoveTime;

        this.ID = -1;
        this.description = "";
    }

    public TaskItem(String taskName, boolean isTodayTask, long lastMoveTime) {
        this.taskName = taskName;
        this.isTodayTask = isTodayTask;
        this.lastMoveTime = lastMoveTime;

        this.isDone = false;
        this.ID = -1;
        this.description = "";
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLastMoveTime(long lastMoveTime) {
        this.lastMoveTime = lastMoveTime;
    }

    public long getID() {
        return ID;
    }

    public String getDescription() {
        return description;
    }

    public long getLastMoveTime() {
        return lastMoveTime;
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
