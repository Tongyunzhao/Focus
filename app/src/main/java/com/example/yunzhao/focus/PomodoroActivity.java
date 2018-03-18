package com.example.yunzhao.focus;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.SoundPool;
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

    // 番茄钟时长设置为1500秒，即25分钟
    int POMODORO_LENGTH = 1500;

    // 音效
    private SoundPool soundPool;  // 声明一个SoundPool
    private int soundID;  // 创建某个声音对应的音频ID

    // 通知
    private Notification notification;
    private NotificationManager notificationManager;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                progressBar.setProgress(progress);
                if (progress == POMODORO_LENGTH) {
                    soundPool.play(soundID, 0.5f, 0.5f, 0, 0, 1);  // 播放音效
                    notificationManager.notify(0, notification);
                    finishPomodoro();
                } else {
                    progress++;
                }
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

        // 初始化音效
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initSound();
        }

        // 初始化通知
        initNotification();

        // 初始化任务名
        taskname = findViewById(R.id.taskname);
        taskname.setText(getIntent().getStringExtra("taskname"));

        progressBar = findViewById(R.id.progress);
        btn_timekeeping = findViewById(R.id.btn_timekeeping);
        btn_timekeeping.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if (btn_timekeeping.getText().toString().equals("开始专注")) {
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

    private void initNotification() {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, PomodoroActivity.class), 0);
        notification = new Notification.Builder(this).setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("完成一次专注").setContentText("休息一下，一会继续吧")
                .setContentIntent(pendingIntent).setAutoCancel(true).build();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btn_timekeeping.getText().toString().equals("开始专注")) {
                    finish();
                } else {
                    showQuitDialog();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initSound() {
        soundPool = new SoundPool.Builder().build();
        soundID = soundPool.load(this, R.raw.ring, 1);
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
    }

    private void resetPomodoro() {
        progress = 0;
        progressBar.setProgress(progress);
        btn_timekeeping.setText("开始专注");
        btn_timekeeping.setBackground(getDrawable(R.drawable.btn1_selector));
        btn_timekeeping.setTextColor(getResources().getColor(R.color.black_overlay));
    }

    private void finishPomodoro() {
        stopTimer();
        resetPomodoro();
    }

    private void showDialog() {
        stopTimer();  // 暂停计时
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确定放弃专注吗？").setMessage("你正在专注完成一个任务，集中注意力会让你更有效率。")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startTimer();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //stopTimer();
                        resetPomodoro();
                    }
                }).show();
    }

    private void showQuitDialog() {
        stopTimer();  // 暂停计时
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确定退出专注吗？").setMessage("你正在专注完成一个任务，集中注意力会让你更有效率。")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startTimer();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PomodoroActivity.this.finish();
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
            builder.setTitle("什么是专注模式？").setMessage("在25分钟的时间里，屏蔽所有干扰因素，保持注意力高度集中，高效完成任务。倒计时结束后，Focus会通过铃声提醒你休息一会。\n你可以用 Focus 的「专注模式」来实践「番茄工作法」，更有效地管理你的时间。")
                    .setPositiveButton("知道啦", null).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
