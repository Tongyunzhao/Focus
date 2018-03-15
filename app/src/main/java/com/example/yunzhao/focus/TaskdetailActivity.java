package com.example.yunzhao.focus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskdetail);

        // 设置toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 设置状态栏颜色
        StatusBarUtil.setStatusBarColor(getWindow(), this);

        initData();

    }

    private void initData() {
        et_taskname = findViewById(R.id.et_taskname);
        et_taskname.setText("写毕业论文");
        et_subtask = findViewById(R.id.et_subtask);
        et_taskdescription = findViewById(R.id.et_taskdescription);

        subtask_listview = findViewById(R.id.subtask_listview);
        initSubtaskData();
        MyListViewAdapter2 adapter = new MyListViewAdapter2(this, subtaskItems);
        subtask_listview.setAdapter(adapter);
    }

    private void initSubtaskData() {
        subtaskItems = new ArrayList<>();

        // 添加子任务信息
        subtaskItems.add(new SubtaskItem(false, "确定毕业论文目录结构"));
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
            Toast.makeText(this, "删除操作", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
