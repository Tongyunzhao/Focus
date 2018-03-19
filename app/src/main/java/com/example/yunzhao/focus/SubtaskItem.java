package com.example.yunzhao.focus;

/**
 * Created by Yunzhao on 2018/3/13.
 */

public class SubtaskItem {
    private long ID;
    private String subtaskName;
    private boolean isDone;
    private long taskID;


    public SubtaskItem(long ID, String subtaskName, boolean isDone, long taskID) {
        this.ID = ID;
        this.subtaskName = subtaskName;
        this.isDone = isDone;
        this.taskID = taskID;
    }

    public SubtaskItem(String subtaskName, long taskID) {
        this.subtaskName = subtaskName;
        this.taskID = taskID;

        this.ID = -1;
        this.isDone = false;
    }

    public SubtaskItem(boolean isDone, String subtaskName, long taskID) {
        this.isDone = isDone;
        this.subtaskName = subtaskName;
        this.taskID = taskID;

        this.ID = -1;
    }

    public long getID() {
        return ID;
    }

    public long getTaskID() {
        return taskID;
    }

    public boolean isDone() {
        return isDone;
    }

    public String getSubtaskName() {
        return subtaskName;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public void setTaskID(long taskID) {
        this.taskID = taskID;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public void setSubtaskName(String subtaskName) {
        this.subtaskName = subtaskName;
    }
}
