package com.example.yunzhao.focus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yunzhao.focus.util.StatusBarUtil;
import com.example.yunzhao.focus.widget.MyListView2;

import java.util.ArrayList;

public class TaskdetailActivity extends AppCompatActivity {

    private EditText et_taskname;
    private EditText et_subtask;
    private EditText et_taskdescription;
    private MyListView2 subtask_listview;
    private ArrayList<SubtaskItem> subtaskItems;
    private MyListViewAdapter2 adapter;
    private CheckBox checkbox;
    private CheckBox istodaytask;

    // 音效
    private SoundPool soundPool;  // 声明一个SoundPool
    private int soundID;  // 创建某个声音对应的音频ID


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskdetail);

        // 设置toolbar
        setToolBar();

        // 设置状态栏颜色
        StatusBarUtil.setStatusBarColor(getWindow(), this);

        initSound();

        findView();
        initData();

        et_subtask.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String newSubtaskName = et_subtask.getText().toString().trim();
                if (i == EditorInfo.IME_ACTION_DONE && newSubtaskName.length() > 0) {
                    et_subtask.setText("");
                    SubtaskItem subtaskItem = new SubtaskItem(false, newSubtaskName);
                    subtaskItems.add(subtaskItem);
                    adapter.notifyDataSetChanged();
                }
                return false;
            }
        });

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
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
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initData() {

        // 从数据库中读取task数据

        et_taskname.setText("写毕业论文");

        initSubtaskData();
        adapter = new MyListViewAdapter2(this, subtaskItems);
        adapter.setOnItemDoneClickListener(myOnItemDoneListener);
        subtask_listview.setAdapter(adapter);
    }

    private void initSubtaskData() {
        subtaskItems = new ArrayList<>();

        // 添加子任务信息
        subtaskItems.add(new SubtaskItem(true, "确定毕业论文目录结构"));
        subtaskItems.add(new SubtaskItem(false, "设计APP的功能和页面"));
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
            // 从数据库中删除这条任务

            finish();
            //Toast.makeText(this, "删除操作", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private MyListViewAdapter2.onItemDoneListener myOnItemDoneListener = new MyListViewAdapter2.onItemDoneListener() {
        @Override
        public void onDoneClick(final int i) {
            boolean curCheckedStatus = subtaskItems.get(i).isDone();
            subtaskItems.get(i).setDone(!curCheckedStatus);

            // 播放音效
            if (subtaskItems.get(i).isDone())
                soundPool.play(soundID, 0.5f, 0.5f, 0, 0, 1);

            // 更新删除线的UI
            adapter.notifyDataSetChanged();

        }
    };
}
