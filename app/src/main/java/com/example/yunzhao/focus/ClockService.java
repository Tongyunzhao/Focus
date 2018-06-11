package com.example.yunzhao.focus;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.yunzhao.focus.helper.DatabaseHelper;

import java.util.Date;
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
    private SoundPool soundPool;  // 声明一个SoundPool
    private int soundID;  // 创建某个声音对应的音频ID
    private Vibrator vibrator;  // 震动
    private String taskname;
    private long taskID;
    private Notification notification;  // 通知
    private NotificationManager notificationManager;
    private Timer timer = null;
    private TimerTask task = null;
    private DatabaseHelper db;  // 数据存储
    private PowerManager.WakeLock wakeLock = null;

    //private AlarmManager am;
    //private PendingIntent pi;

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化音效
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initSound();
        }

        // 初始化震动
        vibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);

        initNotification();  // 初始化通知
        db = new DatabaseHelper(this);  // 初始化数据库
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        taskname = intent.getStringExtra("taskname");
        taskID = intent.getLongExtra("taskid", 0);
        startTimer();
        //startTimer_();
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
                        soundPool.play(soundID, 0.5f, 0.5f, 100, 0, 1);  // 播放音效
                        notificationManager.notify(0, notification);
                        vibrator.vibrate(new long[]{0, 1200, 300, 1200, 300, 1200}, -1);
                        // 将操作写入OpRecord表
                        db.createOpRecord(1, taskID, new Date().getTime());
                    }

                    //发送广播
                    intent.putExtra("progress", progress);
                    sendBroadcast(intent);

                    Log.e("Timer", progress + "");
                }
            };
            timer.schedule(task, 1000, 1000);
        }
    }

    // 关闭计时器
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
                .setContentIntent(pendingIntent).setAutoCancel(true).build();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
        db.closeDB();
    }




//    public void startTimer_() {
//
//        Log.e("Timer", "on startTimer_()");
//        am = (AlarmManager) getSystemService(ALARM_SERVICE);
//        Intent intent = new Intent(this, AlarmReceiver.class);
//        pi = PendingIntent.getBroadcast(this, 0, intent, 0);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Log.e("Timer", "1");
//            am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5*60*1000, pi);
//        } else {
//            Log.e("Timer", "2");
//            am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5*60*1000, pi);
//        }
//    }

}
