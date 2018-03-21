package com.example.yunzhao.focus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yunzhao.focus.helper.DatabaseHelper;
import com.example.yunzhao.focus.util.StatusBarUtil;
import com.example.yunzhao.focus.widget.MyListView2;

import java.util.ArrayList;
import java.util.Date;

public class TaskdetailActivity extends AppCompatActivity {

    private long taskID = -1;
    private String name;
    private boolean istoday;
    private String description;
    private boolean isdone;
    private long lastmovetime;

    private EditText et_taskname;
    private EditText et_subtask;
    private EditText et_taskdescription;
    private MyListView2 subtask_listview;
    private ArrayList<SubtaskItem> subtaskItems;
    private MyListViewAdapter2 adapter;
    private CheckBox checkbox;
    private CheckBox istodaytask;
    //private LinearLayout linearlayout;

    // 音效
    private SoundPool soundPool;  // 声明一个SoundPool
    private int soundID;  // 创建某个声音对应的音频ID

    // 数据存储
    private DatabaseHelper db;

    private boolean isDelete = false;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskdetail);

        setToolBar();  // 设置toolbar
        StatusBarUtil.setStatusBarColor(getWindow(), this);  // 设置状态栏颜色

        initSound();  // 初始化音效

        findView();
        db = new DatabaseHelper(this);
        initData();

        initSubtaskListView();

        et_subtask.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String newSubtaskName = et_subtask.getText().toString().trim();
                if (i == EditorInfo.IME_ACTION_DONE && newSubtaskName.length() > 0) {
                    et_subtask.setText("");
                    SubtaskItem subtaskItem = new SubtaskItem(newSubtaskName, taskID);
                    subtaskItem.setID(db.createSubtask(subtaskItem));  // 赋值ID并存入数据库
                    subtaskItems.add(subtaskItem);
                    adapter.notifyDataSetChanged();
                }
                return false;
            }
        });

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                lastmovetime = new Date().getTime();
                if (b) {
                    et_taskname.setInputType(InputType.TYPE_NULL);
                    soundPool.play(soundID, 0.5f, 0.5f, 0, 0, 1);
                    istodaytask.setVisibility(View.INVISIBLE);
                } else {
                    istodaytask.setVisibility(View.VISIBLE);
                    et_taskname.setInputType(InputType.TYPE_CLASS_TEXT);
                }
            }
        });

        istodaytask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                lastmovetime = new Date().getTime();
            }
        });
    }

    private void initSubtaskListView() {
        adapter = new MyListViewAdapter2(this, subtaskItems);
        adapter.setOnItemDoneClickListener(myOnItemDoneListener);
        subtask_listview.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initSound() {
        soundPool = new SoundPool.Builder().build();
        soundID = soundPool.load(this, R.raw.done, 1);
    }

    private void findView() {
        et_taskname = findViewById(R.id.et_taskname);
        et_subtask = findViewById(R.id.et_subtask);
        et_taskdescription = findViewById(R.id.et_taskdescription);
        subtask_listview = findViewById(R.id.subtask_listview);
        checkbox = findViewById(R.id.checkbox);
        istodaytask = findViewById(R.id.istodaytask);
        //linearlayout = findViewById(R.id.linearlayout);
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setTypeface(Typeface.SERIF);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void updateTaskData() {
        name = et_taskname.getText().toString().trim();
        isdone = checkbox.isChecked();
        istoday = istodaytask.isChecked();
        description = et_taskdescription.getText().toString();

        TaskItem task = new TaskItem(taskID, name, isdone, istoday, description, lastmovetime);
        db.updateTask(task);
    }

    private void initData() {
        // 获取数据
        taskID = getIntent().getLongExtra("id", -1);
        name = getIntent().getStringExtra("name");
        istoday = getIntent().getBooleanExtra("istoday", false);
        description = getIntent().getStringExtra("description");
        lastmovetime = getIntent().getLongExtra("lastmovetime", 0);

        // 设置UI
        et_taskname.setText(name);
        istodaytask.setChecked(istoday);
        et_taskdescription.setText(description);

        // 初始化子任务信息
        subtaskItems = new ArrayList<>();
        subtaskItems = (ArrayList<SubtaskItem>) db.getSubtasksByTask(taskID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.taskdetail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_pomodoro) {
            Intent intent = new Intent(TaskdetailActivity.this, PomodoroActivity.class);
            intent.putExtra("taskname", et_taskname.getText().toString());
            startActivity(intent);
        } else if (id == R.id.action_delete) {
            db.deleteTask(taskID);  // 从数据库中删除这条任务
            isDelete = true;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private MyListViewAdapter2.onItemDoneListener myOnItemDoneListener = new MyListViewAdapter2.onItemDoneListener() {
        @Override
        public void onDoneClick(final int i) {
            boolean curCheckedStatus = subtaskItems.get(i).isDone();
            subtaskItems.get(i).setDone(!curCheckedStatus);
            db.updateSubtask(subtaskItems.get(i));  // 存入数据库

            // 播放音效
            if (subtaskItems.get(i).isDone())
                soundPool.play(soundID, 0.5f, 0.5f, 0, 0, 1);

            // 更新删除线的UI
            adapter.notifyDataSetChanged();
        }
    };


    @Override
    protected void onPause() {
        if (!isDelete)
            updateTaskData();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        db.closeDB();
        super.onDestroy();
    }
}
