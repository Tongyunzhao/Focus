package com.example.yunzhao.focus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * Created by Yunzhao on 2018/4/9.
 */

public class ClockService extends Service {

    private int progress = 0;  // 当前值
    public static final int POMODORO_LENGTH = 1500;  // 最大值为1500秒，即25分钟
    private Intent intent = new Intent("com.example.yunzhao.focus.RECEIVER");

    // 音效
    private SoundPool soundPool;  // 声明一个SoundPool
    private int soundID;  // 创建某个声音对应的音频ID

    private String taskname;

    // 通知
    private Notification notification;
    private NotificationManager notificationManager;

    private Timer timer = null;
    private TimerTask task = null;


    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化音效
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initSound();
        }

        // 初始化通知
        initNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        taskname = intent.getStringExtra("taskname");
        startTimer();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void startTimer() {
        if (timer == null) {
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    progress++;

                    if (progress == POMODORO_LENGTH) {
                        soundPool.play(soundID, 0.5f, 0.5f, 0, 0, 1);  // 播放音效
                        notificationManager.notify(0, notification);
                        progress++;
                    }

                    //发送广播
                    intent.putExtra("progress", progress);
                    sendBroadcast(intent);
                }
            };
            timer.schedule(task, 1000, 1000);
        }


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (progress <= POMODORO_LENGTH) {
//                    progress++;
//
//                    if (progress == POMODORO_LENGTH) {
//                        soundPool.play(soundID, 0.5f, 0.5f, 0, 0, 1);  // 播放音效
//                        notificationManager.notify(0, notification);
//                        progress++;
//                    }
//
//                    //发送广播
//                    intent.putExtra("progress", progress);
//                    sendBroadcast(intent);
//
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
    }

    //关闭计时器
    private void stopTimer() {
        timer.cancel();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initSound() {
        soundPool = new SoundPool.Builder().build();
        soundID = soundPool.load(this, R.raw.ring, 1);
    }

    private void initNotification() {
        Intent mainIntent = new Intent(this, PomodoroActivity.class);
        mainIntent.putExtra("taskname", taskname);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mainIntent, FLAG_UPDATE_CURRENT);

        notification = new Notification.Builder(this).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("完成一次专注").setContentText("休息一下，一会继续吧")
                .setContentIntent(pendingIntent).setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_VIBRATE).build();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}
