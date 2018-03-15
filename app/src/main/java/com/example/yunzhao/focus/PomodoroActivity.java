package com.example.yunzhao.focus;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yunzhao.focus.widget.CustomCircleProgressBar;

import java.util.Timer;
import java.util.TimerTask;

public class PomodoroActivity extends AppCompatActivity {

    private CustomCircleProgressBar progressBar;
    private int progress = 0;
    private Button btn_timekeeping;
    private TextView taskname;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                progressBar.setProgress(progress);
                if (progress == 1500) {
                    finishPomodoro();
                }
                progress++;
            }
            super.handleMessage(msg);
        }
    };

    Timer timer = null;
    TimerTask task = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        setToolbar();

        taskname = findViewById(R.id.taskname);
        taskname.setText(getIntent().getStringExtra("taskname"));

        progressBar = findViewById(R.id.progress);
        btn_timekeeping = findViewById(R.id.btn_timekeeping);
        btn_timekeeping.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if (btn_timekeeping.getText().toString().equals("开始番茄钟")) {
                    startTimer();
                    btn_timekeeping.setText("放弃");
                    btn_timekeeping.setBackground(getDrawable(R.drawable.btn2_selector));
                    btn_timekeeping.setTextColor(getResources().getColor(R.color.grey));
                } else {
                    showDialog();
                }
            }
        });

    }

    void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void startTimer() {
        if (timer == null) {
            timer = new Timer();
        }

        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            };
        }

        if (timer != null && task != null) {
            timer.schedule(task, 0, 1000);
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
        progress = 0;
        progressBar.setProgress(progress);
    }

    private void finishPomodoro() {
        stopTimer();
        btn_timekeeping.setText("开始番茄钟");
        btn_timekeeping.setBackground(getDrawable(R.drawable.btn1_selector));
        btn_timekeeping.setTextColor(getResources().getColor(R.color.black_overlay));
        Toast.makeText(this, "番茄钟已完成", Toast.LENGTH_SHORT).show();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("放弃番茄").setMessage("你目前正在一个番茄时间中，确定要放弃这个番茄吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        stopTimer();
                        btn_timekeeping.setText("开始番茄钟");
                        btn_timekeeping.setBackground(getDrawable(R.drawable.btn1_selector));
                        btn_timekeeping.setTextColor(getResources().getColor(R.color.black_overlay));
                    }
                }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pomodoro, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_description) {
            //Toast.makeText(this, "显示番茄钟说明", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("什么是番茄钟？").setMessage("「番茄钟」是时间管理理论「番茄工作法」里的核心概念。番茄工作法主张把任务分解成半小时左右，集中精力工作25分钟后休息5分钟，如此视作完成一个番茄钟。哪怕工作没有完成，也要定时休息，然后再进入下一个番茄钟。完成4个番茄钟后，能休息15分钟。\n用番茄工作法克服惰性、屏蔽诱惑，Focus on something，试试看？")
                    .setPositiveButton("知道啦", null).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
