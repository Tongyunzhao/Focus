package com.example.yunzhao.focus.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.yunzhao.focus.SubtaskItem;
import com.example.yunzhao.focus.TaskItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yunzhao on 2018/3/19.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "focus";

    // Table Names
    private static final String TABLE_TASK = "Tasks";
    private static final String TABLE_SUBTASK = "Subtasks";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_ISDONE = "isdone";

    // TASK Table - column names
    private static final String KEY_ISTODAY = "istoday";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_LASTMOVETIME = "lastmovetime";

    // SUBTASK Table - column names
    private static final String KEY_TASKID = "taskid";


    // Table Create Statements
    // Task table create statement
    private static final String CREATE_TABLE_TASK = "CREATE TABLE "
            + TABLE_TASK + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_NAME + " TEXT,"
            + KEY_ISDONE + " INTEGER,"
            + KEY_ISTODAY + " INTEGER,"
            + KEY_DESCRIPTION + " TEXT,"
            + KEY_LASTMOVETIME + " INTEGER"
            + ")";

    // SubTask table create statement
    private static final String CREATE_TABLE_SUBTASK = "CREATE TABLE "
            + TABLE_SUBTASK + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_NAME + " TEXT,"
            + KEY_ISDONE + " INTEGER,"
            + KEY_TASKID + " INTEGER"
            + ")";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_TASK);
        db.execSQL(CREATE_TABLE_SUBTASK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBTASK);

        // create new tables
        onCreate(db);
    }


    /*
     * Creating a task
     */
    public long createTask(TaskItem task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, task.getTaskName());
        values.put(KEY_ISDONE, (task.isDone() ? 1 : 0));
        values.put(KEY_ISTODAY, (task.isTodayTask() ? 1 : 0));
        values.put(KEY_DESCRIPTION, task.getDescription());
        values.put(KEY_LASTMOVETIME, task.getLastMoveTime());

        // insert row
        long task_id = db.insert(TABLE_TASK, null, values);

        return task_id;
    }

    /*
     * Get single task
     */
    public TaskItem getTask(long task_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_TASK + " WHERE "
                + KEY_ID + " = " + task_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        TaskItem task = null;

        if (c != null) {
            c.moveToFirst();
            int ID = c.getInt(c.getColumnIndex(KEY_ID));
            String taskName = c.getString(c.getColumnIndex(KEY_NAME));
            boolean isDone = (c.getInt(c.getColumnIndex(KEY_ISDONE))) != 0;
            boolean isTodayTask = (c.getInt(c.getColumnIndex(KEY_ISTODAY))) != 0;
            String description = c.getString(c.getColumnIndex(KEY_DESCRIPTION));
            long lastMoveTime = c.getLong(c.getColumnIndex(KEY_LASTMOVETIME));

            task = new TaskItem(ID, taskName, isDone, isTodayTask, description, lastMoveTime);
        }

        return task;
    }

    /*
     * 获取所有属于“今日待办”列表的task
     */
    public List<TaskItem> getTodayTasks() {
        List<TaskItem> todayTasks = new ArrayList<TaskItem>();
        String selectQuery = "SELECT * FROM " + TABLE_TASK
                + " WHERE " + KEY_ISDONE + "=0 AND " + KEY_ISTODAY + "=1"
                + " ORDER BY " + KEY_LASTMOVETIME + " DESC";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                int ID = c.getInt(c.getColumnIndex(KEY_ID));
                String taskName = c.getString(c.getColumnIndex(KEY_NAME));
                boolean isDone = (c.getInt(c.getColumnIndex(KEY_ISDONE))) != 0;
                boolean isTodayTask = (c.getInt(c.getColumnIndex(KEY_ISTODAY))) != 0;
                String description = c.getString(c.getColumnIndex(KEY_DESCRIPTION));
                long lastMoveTime = c.getLong(c.getColumnIndex(KEY_LASTMOVETIME));

                TaskItem task = new TaskItem(ID, taskName, isDone, isTodayTask, description, lastMoveTime);
                todayTasks.add(task);

            } while (c.moveToNext());
        }

        return todayTasks;
    }

    /*
     * 获取所有属于“收件箱”列表的task
     */
    public List<TaskItem> getInboxTasks() {
        List<TaskItem> inboxTasks = new ArrayList<TaskItem>();
        String selectQuery = "SELECT * FROM " + TABLE_TASK
                + " WHERE " + KEY_ISDONE + "=0 AND " + KEY_ISTODAY + "=0"
                + " ORDER BY " + KEY_LASTMOVETIME + " DESC";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                int ID = c.getInt(c.getColumnIndex(KEY_ID));
                String taskName = c.getString(c.getColumnIndex(KEY_NAME));
                boolean isDone = (c.getInt(c.getColumnIndex(KEY_ISDONE))) != 0;
                boolean isTodayTask = (c.getInt(c.getColumnIndex(KEY_ISTODAY))) != 0;
                String description = c.getString(c.getColumnIndex(KEY_DESCRIPTION));
                long lastMoveTime = c.getLong(c.getColumnIndex(KEY_LASTMOVETIME));

                TaskItem task = new TaskItem(ID, taskName, isDone, isTodayTask, description, lastMoveTime);
                inboxTasks.add(task);

            } while (c.moveToNext());
        }

        return inboxTasks;
    }

    /*
     * 获取所有属于“已完成”列表的task
     */
    public List<TaskItem> getDoneTasks() {
        List<TaskItem> doneTasks = new ArrayList<TaskItem>();
        String selectQuery = "SELECT * FROM " + TABLE_TASK
                + " WHERE " + KEY_ISDONE + "=1"
                + " ORDER BY " + KEY_LASTMOVETIME + " DESC";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                int ID = c.getInt(c.getColumnIndex(KEY_ID));
                String taskName = c.getString(c.getColumnIndex(KEY_NAME));
                boolean isDone = (c.getInt(c.getColumnIndex(KEY_ISDONE))) != 0;
                boolean isTodayTask = (c.getInt(c.getColumnIndex(KEY_ISTODAY))) != 0;
                String description = c.getString(c.getColumnIndex(KEY_DESCRIPTION));
                long lastMoveTime = c.getLong(c.getColumnIndex(KEY_LASTMOVETIME));

                TaskItem task = new TaskItem(ID, taskName, isDone, isTodayTask, description, lastMoveTime);
                doneTasks.add(task);

            } while (c.moveToNext());
        }

        return doneTasks;
    }

    /*
     * Deleting a task
     */
    public void deleteTask(long task_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASK, KEY_ID + " = ?",
                new String[]{String.valueOf(task_id)});

        // 将属于这个task的subtask也删掉
        List<SubtaskItem> subtaskItems = getSubtasksByTask(task_id);
        for (int i = 0; i < subtaskItems.size(); i++) {
            deleteSubtask(subtaskItems.get(i).getID());
        }
    }

    /*
     * Updating a task
     */
    public int updateTask(TaskItem task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, task.getID());
        values.put(KEY_NAME, task.getTaskName());
        values.put(KEY_ISDONE, (task.isDone() ? 1 : 0));
        values.put(KEY_ISTODAY, (task.isTodayTask() ? 1 : 0));
        values.put(KEY_DESCRIPTION, task.getDescription());
        values.put(KEY_LASTMOVETIME, task.getLastMoveTime());

        // updating row
        return db.update(TABLE_TASK, values, KEY_ID + " = ?",
                new String[]{String.valueOf(task.getID())});
    }

    /*
     * 获取一个task的所有subtask
     */
    public List<SubtaskItem> getSubtasksByTask(long task_id) {
        List<SubtaskItem> subtasks = new ArrayList<SubtaskItem>();
        String selectQuery = "SELECT * FROM " + TABLE_SUBTASK
                + " WHERE " + KEY_TASKID + "=" + task_id;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                int ID = c.getInt(c.getColumnIndex(KEY_ID));
                String taskName = c.getString(c.getColumnIndex(KEY_NAME));
                boolean isDone = (c.getInt(c.getColumnIndex(KEY_ISDONE))) != 0;

                SubtaskItem subtask = new SubtaskItem(ID, taskName, isDone, task_id);
                subtasks.add(subtask);

            } while (c.moveToNext());
        }

        return subtasks;
    }

    /*
     * Creating a subtask
     */
    public long createSubtask(SubtaskItem subtask) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, subtask.getSubtaskName());
        values.put(KEY_ISDONE, (subtask.isDone() ? 1 : 0));
        values.put(KEY_TASKID, subtask.getTaskID());

        // insert row
        long subtask_id = db.insert(TABLE_SUBTASK, null, values);

        return subtask_id;
    }

    /*
     * Updating a subtask
     */
    public int updateSubtask(SubtaskItem subtask) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, subtask.getID());
        values.put(KEY_NAME, subtask.getSubtaskName());
        values.put(KEY_ISDONE, (subtask.isDone() ? 1 : 0));
        values.put(KEY_TASKID, subtask.getTaskID());

        // updating row
        return db.update(TABLE_SUBTASK, values, KEY_ID + " = ?",
                new String[]{String.valueOf(subtask.getID())});
    }

    /*
     * Deleting a subtask
     */
    public void deleteSubtask(long subtask_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SUBTASK, KEY_ID + " = ?",
                new String[]{String.valueOf(subtask_id)});
    }


    //closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}
