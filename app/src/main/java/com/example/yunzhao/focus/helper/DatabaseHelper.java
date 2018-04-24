package com.example.yunzhao.focus.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.yunzhao.focus.SubtaskItem;
import com.example.yunzhao.focus.TaskItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Yunzhao on 2018/3/19.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final long MSADAY = 24*60*60*1000;

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "focus";

    // Table Names
    private static final String TABLE_TASK = "Tasks";
    private static final String TABLE_SUBTASK = "Subtasks";
    private static final String TABLE_OPRECORD = "OpRecords";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_ISDONE = "isdone";
    private static final String KEY_TASKID = "taskid";

    // TASK Table - column names
    private static final String KEY_ISTODAY = "istoday";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_LASTMOVETIME = "lastmovetime";

    // OPRECORD Table - column names
    private static final String KEY_TYPE = "type";
    private static final String KEY_OPTIME = "optime";


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

    // OpRecord table create statement
    private static final String CREATE_TABLE_OPRECORD = "CREATE TABLE "
            + TABLE_OPRECORD + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_TYPE + " INTEGER,"
            + KEY_TASKID + " INTEGER,"
            + KEY_OPTIME + " INTEGER"
            + ")";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_TASK);
        db.execSQL(CREATE_TABLE_SUBTASK);
        db.execSQL(CREATE_TABLE_OPRECORD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBTASK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OPRECORD);

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


    /*
     * Creating a oprecord
     */
    public long createOpRecord(int type, long taskID, long opTime) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TYPE, type);
        values.put(KEY_TASKID, taskID);
        values.put(KEY_OPTIME, opTime);

        // insert row
        long oprecord_id = db.insert(TABLE_OPRECORD, null, values);

        return oprecord_id;
    }


    /*
     * 获取总的数据记录数量
     * 返回值1：专注次数
     * 返回值2：专注小时
     * 返回值3：完成任务数
     */
    public List<Float> getAllRecordNum() {
        List<Float> allRecordNum = new ArrayList<Float>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 获取完成任务数
        String selectQuery0 = "SELECT COUNT(*) FROM " + TABLE_OPRECORD
                + " WHERE " + KEY_TYPE + "=0";
        //Log.e(LOG, selectQuery0);
        Cursor cursor0 = db.rawQuery(selectQuery0, null);
        cursor0.moveToFirst();
        long doneTaskNum = cursor0.getLong(0);
        cursor0.close();

        // 获取专注次数
        String selectQuery1 = "SELECT COUNT(*) FROM " + TABLE_OPRECORD
                + " WHERE " + KEY_TYPE + "=1";
        //Log.e(LOG, selectQuery1);
        Cursor cursor1 = db.rawQuery(selectQuery1, null);
        cursor1.moveToFirst();
        long pomodoroNum = cursor1.getLong(0);
        cursor1.close();

        allRecordNum.add((float) pomodoroNum);
        float hour =  (float)(Math.round(((float) pomodoroNum*25/60)*10))/10;
        allRecordNum.add(hour);
        allRecordNum.add((float) doneTaskNum);

        return allRecordNum;
    }


    /*
     * 获取今日数据记录数量
     * 返回值1：专注次数
     * 返回值2：专注小时
     * 返回值3：完成任务数
     */
    public List<Float> getTodayRecordNum() {
        List<Float> todayRecordNum = new ArrayList<Float>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 计算今日的时间范围
        long curTime = new Date().getTime();
        long todayStartTime = curTime - (curTime % MSADAY);

        // 获取今日完成任务数
        String selectQuery0 = "SELECT COUNT(*) FROM " + TABLE_OPRECORD
                + " WHERE " + KEY_TYPE + "=0 AND " + KEY_OPTIME + ">=" + todayStartTime
                + " AND " + KEY_OPTIME + "<=" + curTime;
        //Log.e(LOG, selectQuery0);
        Cursor cursor0 = db.rawQuery(selectQuery0, null);
        cursor0.moveToFirst();
        long doneTaskNum = cursor0.getLong(0);
        cursor0.close();

        // 获取专注次数
        String selectQuery1 = "SELECT COUNT(*) FROM " + TABLE_OPRECORD
                + " WHERE " + KEY_TYPE + "=1 AND " + KEY_OPTIME + ">=" + todayStartTime
                + " AND " + KEY_OPTIME + "<=" + curTime;
        //Log.e(LOG, selectQuery1);
        Cursor cursor1 = db.rawQuery(selectQuery1, null);
        cursor1.moveToFirst();
        long pomodoroNum = cursor1.getLong(0);
        cursor1.close();

        todayRecordNum.add((float) pomodoroNum);
        float hour =  (float)(Math.round(((float) pomodoroNum*25/60)*10))/10;
        todayRecordNum.add(hour);
        todayRecordNum.add((float) doneTaskNum);

        return todayRecordNum;
    }


    /*
     * 获取过去7天的专注时长、完成任务数据
     */
    public ArrayList<HashMap<String, Object>> getLast7DayRecordNum() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

        // 计算7天的日期
        long curTime = new Date().getTime();
        long todayStartTime = curTime - (curTime % MSADAY);
        ArrayList<Long> dayStartTimes = new ArrayList<>();
        ArrayList<String> dateStr = new ArrayList<>();
        for (int i = 0; i < 7; ++i) {
            long temp = todayStartTime-(6-i)*MSADAY;
            dayStartTimes.add(temp);
            dateStr.add(getFormatedDateTime(temp));
        }

        // 获取过去7天的专注时长
        ArrayList<Long> pomodoroNums = new ArrayList<>();
        ArrayList<Float> pomodoroHours = new ArrayList<>();
        for (int i = 0; i < 7; ++i) {
            String selectQuery;
            if (i == 6) {
                selectQuery = "SELECT COUNT(*) FROM " + TABLE_OPRECORD
                        + " WHERE " + KEY_TYPE + "=1 AND " + KEY_OPTIME + ">=" + dayStartTimes.get(i);
            } else {
                selectQuery = "SELECT COUNT(*) FROM " + TABLE_OPRECORD
                        + " WHERE " + KEY_TYPE + "=1 AND " + KEY_OPTIME + ">=" + dayStartTimes.get(i)
                        + " AND " + KEY_OPTIME + "<" + dayStartTimes.get(i+1);
            }
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            cursor.moveToFirst();
            long pomodoroNum = cursor.getLong(0);
            cursor.close();

            pomodoroNums.add(pomodoroNum);

            float hour =  (float)(Math.round(((float) pomodoroNum*25/60)*10))/10;
            pomodoroHours.add(hour);
        }

        // 获取过去7天的完成任务数
        ArrayList<Float> doneTaskNums = new ArrayList<>();
        for (int i = 0; i < 7; ++i) {
            String selectQuery;
            if (i == 6) {
                selectQuery = "SELECT COUNT(*) FROM " + TABLE_OPRECORD
                        + " WHERE " + KEY_TYPE + "=0 AND " + KEY_OPTIME + ">=" + dayStartTimes.get(i);
            } else {
                selectQuery = "SELECT COUNT(*) FROM " + TABLE_OPRECORD
                        + " WHERE " + KEY_TYPE + "=0 AND " + KEY_OPTIME + ">=" + dayStartTimes.get(i)
                        + " AND " + KEY_OPTIME + "<" + dayStartTimes.get(i+1);
            }
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            cursor.moveToFirst();
            float doneTaskNum = cursor.getLong(0);
            cursor.close();

            doneTaskNums.add(doneTaskNum);
        }

        // 往data中赋值
        for (int i = 0; i < 7; ++i) {
            HashMap<String, Object> item = new HashMap<String, Object>();
            item.put("date", dateStr.get(i));
            item.put("pomodoroHour", pomodoroHours.get(i));
            item.put("doneTaskNum", doneTaskNums.get(i));
            data.add(item);
        }

        return data;
    }


    public static String getFormatedDateTime(long dateTime) {
        Date date = new Date(dateTime);
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        return sdf.format(date);
    }


    //closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}
