package com.example.yunzhao.focus;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.SoundPool;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yunzhao.focus.widget.CustomCircleProgressBar;
import com.tencent.stat.StatService;

public class PomodoroActivity extends AppCompatActivity {
    // UI
    private CustomCircleProgressBar progressBar;
    private Button btn_timekeeping;
    private TextView taskname;

    private long taskID = -1;

    private MsgReceiver msgReceiver;
    private Intent mIntent = null;

    // 番茄钟时长设置为1500秒，即25分钟
    int POMODORO_LENGTH = 1500;
    private int progress = 0;

    // 背景音效
    private SoundPool soundPool;  // 声明一个SoundPool
    private int soundID0;  // "静音"音频ID
    private int soundID1;  // "树林鸟鸣"音频ID
    private int playID = 0;
    private boolean willSound = false;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);
        setToolbar();

        // 初始化任务名
        taskname = findViewById(R.id.taskname);
        taskname.setText(getIntent().getStringExtra("taskname"));

        // 初始化任务ID
        taskID = getIntent().getLongExtra("taskid", 0);

        // 初始化背景音效
        soundPool = new SoundPool.Builder().build();
        soundID0 = soundPool.load(this, R.raw.silence, 1);
        soundID1 = soundPool.load(this, R.raw.forest, 1);

        progressBar = findViewById(R.id.progress);
        btn_timekeeping = findViewById(R.id.btn_timekeeping);
        btn_timekeeping.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if (btn_timekeeping.getText().toString().equals("开始专注")) {
                    // 开启service
                    mIntent = new Intent(PomodoroActivity.this, ClockService.class);
                    mIntent.putExtra("taskname", taskname.getText().toString());
                    mIntent.putExtra("taskid", taskID);
                    startService(mIntent);

                    // 更新UI
                    btn_timekeeping.setText("放弃");
                    btn_timekeeping.setBackground(getDrawable(R.drawable.btn2_selector));
                    btn_timekeeping.setTextColor(getResources().getColor(R.color.grey));

                    // 播放白噪音
                    if (willSound) {
                        playID = soundPool.play(soundID1, 0.5f, 0.5f, 0, -1, 1);
                    } else {
                        playID = soundPool.play(soundID0, 0.5f, 0.5f, 0, -1, 1);
                    }

                    // 开始专注
                    StatService.trackCustomKVEvent(PomodoroActivity.this, "StartFocus", null);

                } else {
                    showDialog();
                }
            }
        });
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void finishPomodoro() {
        // 更新UI
        progress = 0;
        progressBar.setProgress(progress);
        btn_timekeeping.setText("开始专注");
        btn_timekeeping.setBackground(getDrawable(R.drawable.btn1_selector));
        btn_timekeeping.setTextColor(getResources().getColor(R.color.black_overlay));

        // 停止白噪音
        soundPool.stop(playID);

        // 停止Service
        stopService(mIntent);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确定放弃专注吗？").setMessage("你正在专注完成一个任务，集中注意力会让你更有效率。")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishPomodoro();

                        // 取消专注事件
                        StatService.trackCustomKVEvent(PomodoroActivity.this, "CancelFocus", null);
                    }
                }).show();
    }

    private void showQuitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确定退出专注吗？").setMessage("你正在专注完成一个任务，集中注意力会让你更有效率。")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishPomodoro();

                        // 取消专注事件
                        StatService.trackCustomKVEvent(PomodoroActivity.this, "CancelFocus", null);

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
        } else if (id == R.id.action_sound) {
            if (willSound) {
                item.setIcon(getResources().getDrawable(R.drawable.ic_nosound));
                willSound = false;
            } else {
                item.setIcon(getResources().getDrawable(R.drawable.ic_sound));
                willSound = true;
            }

            if (btn_timekeeping.getText().toString().equals("放弃")) {
                if (willSound) {
                    // 停止“静音”背景音播放，开始“树林鸟鸣”背景音播放
                    soundPool.stop(playID);
                    playID = soundPool.play(soundID1, 0.5f, 0.5f, 0, -1, 1);
                } else {
                    soundPool.stop(playID);
                    playID = soundPool.play(soundID0, 0.5f, 0.5f, 0, -1, 1);
                }
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();

        //动态注册广播接收器
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.yunzhao.focus.RECEIVER");
        registerReceiver(msgReceiver, intentFilter);

        // 页面开始
        StatService.onResume(this);

        // 进入专注模式页事件, 统计用户进入专注模式页的次数
        StatService.trackCustomKVEvent(this, "TimerPage", null);

    }

    @Override
    protected void onPause() {
        super.onPause();

        //注销广播
        unregisterReceiver(msgReceiver);

        // 页面结束
        StatService.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //停止服务
        if (mIntent != null)
            stopService(mIntent);

        // 停止白噪音
        soundPool.stop(playID);
    }

    /**
     * 广播接收器
     *
     * @author len
     */
    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //拿到进度，更新UI
            progress = intent.getIntExtra("progress", 0);
            progressBar.setProgress(progress);

            if (progress >= POMODORO_LENGTH) {
                finishPomodoro();
            }
        }
    }
}
