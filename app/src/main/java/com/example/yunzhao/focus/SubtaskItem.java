package com.example.yunzhao.focus;

/**
 * Created by Yunzhao on 2018/3/13.
 */

public class SubtaskItem {
    private boolean isDone;
    private String subtaskName;

    public SubtaskItem(boolean isDone, String subtaskName) {
        this.isDone = isDone;
        this.subtaskName = subtaskName;
    }

    public boolean isDone() {
        return isDone;
    }

    public String getSubtaskName() {
        return subtaskName;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public void setSubtaskName(String subtaskName) {
        this.subtaskName = subtaskName;
    }
}
