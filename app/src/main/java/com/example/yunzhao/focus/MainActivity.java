package com.example.yunzhao.focus;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.yunzhao.focus.MyListViewAdapter.onItemDoneListener;
import com.example.yunzhao.focus.helper.DatabaseHelper;
import com.example.yunzhao.focus.util.DimenUtil;
import com.example.yunzhao.focus.util.JsonParser;
import com.example.yunzhao.focus.util.StatusBarUtil;
import com.example.yunzhao.focus.widget.MyEditDialog;
import com.example.yunzhao.focus.widget.MyListView;
import com.example.yunzhao.focus.widget.MyListView2;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.sunflower.FlowerCollector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.yunzhao.focus.util.DimenUtil.dp2px;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnTouchListener, SensorEventListener {

    // UI
    private MyListView todo_listview;
    private MyListView inbox_listview;
    private ListView donetask_listview;
    private LinearLayout donetask_layout;  // 已完成页面
    private ScrollView scrollView;
    private Toolbar toolbar;
    private TextView toolbar_title;

    // 今日待办、收件箱、已完成 3个清单的数据
    private ArrayList<TaskItem> todayTaskItems;
    private ArrayList<TaskItem> inboxTaskItems;
    private ArrayList<TaskItem> doneTaskItems;

    // 今日待办、收件箱、已完成 3个清单的适配器
    private MyListViewAdapter adapter0;  // 今日待办的适配器
    private MyListViewAdapter adapter1;  // 收件箱的适配器
    private MyListViewAdapter adapter2;  // 已完成的适配器

    // 标记
    private float startX = 0, curX = 0, startY = 0, curY = 0;  // 记录手指触摸的位置
    private boolean newTouch = true;
    private int operation_position;  // 记录当前要进行操作的item位置

    // 音效
    private SoundPool soundPool;  // 声明一个SoundPool
    private int soundID;  // 完成任务的音效
    private int soundID_;  // 开始语音识别的音效

    // 摇一摇操作
    private static final int SENSOR_SHAKE = 35;
    private boolean isShake = false;
    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;

    // 科大讯飞语音识别
    private SpeechRecognizer mIat = null;  // 语音听写对象
    private String mEngineType = SpeechConstant.TYPE_CLOUD;  // 引擎类型
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();  // 用HashMap存储听写结果
    int ret = 0; // 函数调用返回值
    private BottomSheetDialog yuyinDialog;

    // 权限
    private static final int MY_PERMISSION_REQUEST_CODE = 10000;

    // 数据存储
    private DatabaseHelper db;
    private SharedPreferences sp;
    private static final String FILE_NAME = "FirstStart";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case 0:
                    TaskItem doneTaskItem = todayTaskItems.get(operation_position);
                    todayTaskItems.get(operation_position).setTodayTask(false);
                    todayTaskItems.get(operation_position).setLastMoveTime(new Date().getTime());
                    db.updateTask(todayTaskItems.get(operation_position));  // 在数据库中更新task数据
                    todayTaskItems.remove(operation_position);
                    doneTaskItems.add(0, doneTaskItem);
                    refreshListView();
                    break;
                case 1:
                    TaskItem doneTaskItem1 = inboxTaskItems.get(operation_position);
                    inboxTaskItems.get(operation_position).setTodayTask(false);
                    inboxTaskItems.get(operation_position).setLastMoveTime(new Date().getTime());
                    db.updateTask(inboxTaskItems.get(operation_position));  // 在数据库中更新task数据
                    inboxTaskItems.remove(operation_position);
                    doneTaskItems.add(0, doneTaskItem1);
                    refreshListView();
                    break;
                case 2:
                    TaskItem doneTaskItem2 = doneTaskItems.get(operation_position);
                    doneTaskItems.get(operation_position).setLastMoveTime(new Date().getTime());
                    db.updateTask(doneTaskItems.get(operation_position));  // 在数据库中更新task数据
                    doneTaskItems.remove(operation_position);
                    inboxTaskItems.add(0, doneTaskItem2);
                    refreshListView();
                    Snackbar.make(scrollView, "已将该任务移回「收件箱」", Snackbar.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * 配置页面整体UI
         */
        setToolbar();  // 设置toolbar
        StatusBarUtil.setStatusBarColor(getWindow(), this);  // 设置状态栏颜色
        setDrawerLayout();  // 设置左侧抽屉

        /**
         * 初始化数据
         */
        db = new DatabaseHelper(this);
        sp = getSharedPreferences(FILE_NAME, 0);
        Boolean user_first = sp.getBoolean("FIRST", true);
        if (user_first) {//第一次
            sp.edit().putBoolean("FIRST", false).commit();
            writeSampleTaskToDB();
        }
        initData();

        /**
         * 配置ListView
         */
        setTodayTaskListView();  // 设置“今日待办”的Listview
        setInboxTaskListView();  // 设置“收件箱”的Listview
        initDoneTaskListView();  // 设置“已完成”的Listview
        fixRollConflict();  // 解决Listview和Scrollview的滚动冲突

        /**
         * 处理交互事件
         */
        setAddTask();  // 设置添加待办项事件
        // 初始化音效
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initSound();
        }
        // 初始化科大讯飞语音识别
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5aac8016");
        initYuYin();

    }

    private void writeSampleTaskToDB() {
        // “今日待办”示例task
        db.createTask(new TaskItem("左滑还有操作哦(＾－＾)", true, new Date().getTime()));
        db.createTask(new TaskItem("任务可以添加描述和子任务", true, new Date().getTime()));
        db.createTask(new TaskItem("点击左侧的小黑框Done掉任务", true, new Date().getTime()));

        // “收件箱”示例task
        db.createTask(new TaskItem("从侧边栏可以进入「已完成」的任务哦", false, new Date().getTime()));
        db.createTask(new TaskItem("番茄工作法，了解一下", false, new Date().getTime()));
        db.createTask(new TaskItem("文字太慢？摇一摇语音输入任务", false, new Date().getTime()));
        db.createTask(new TaskItem("每天从「收件箱」里选任务到「今日待办」", false, new Date().getTime()));

        // “已完成”示例task
        db.createTask(new TaskItem(true, "重做任务？点左侧小黑框移回「收件箱」", false, new Date().getTime()));
        db.createTask(new TaskItem(true, "这是一条已经完成的任务", false, new Date().getTime()));
    }

    private void initData() {
        todayTaskItems = new ArrayList<>();
        todayTaskItems = (ArrayList<TaskItem>) db.getTodayTasks();
        inboxTaskItems = new ArrayList<>();
        inboxTaskItems = (ArrayList<TaskItem>) db.getInboxTasks();
        doneTaskItems = new ArrayList<>();
        doneTaskItems = (ArrayList<TaskItem>) db.getDoneTasks();
    }

    private void initYuYin() {
        mIat = null;
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面
        mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(R.string.app_name);
        toolbar_title.setTypeface(Typeface.SERIF);
    }

    private void setDrawerLayout() {
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initSound() {
        soundPool = new SoundPool.Builder().build();
        soundID = soundPool.load(this, R.raw.done, 1);
        soundID_ = soundPool.load(this, R.raw.ding, 1);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_todo) {
            donetask_layout.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            toolbar_title.setText(R.string.app_name);

        } else if (id == R.id.nav_done) {
            scrollView.setVisibility(View.GONE);
            donetask_layout.setVisibility(View.VISIBLE);
            toolbar_title.setText(R.string.done);

        } else if (id == R.id.tag_work) {
            Toast.makeText(this, "标签功能正在开发中，敬请期待", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.tag_learn) {
            Toast.makeText(this, "标签功能正在开发中，敬请期待", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.tag_live) {
            Toast.makeText(this, "标签功能正在开发中，敬请期待", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setTodayTaskListView() {
        todo_listview = findViewById(R.id.todo_listview);
        todo_listview.setMenuCreator(creator);
        adapter0 = new MyListViewAdapter(this, todayTaskItems, 0);
        todo_listview.setAdapter(adapter0);

        // 设置左滑按钮点击事件
        todo_listview.setOnMenuItemClickListener(myOnMenuItemClickListener0);

        // 设置Listview列表项点击事件
        todo_listview.setOnItemClickListener(myOnItemClickListener0);

        // 设置checkbox点击事件
        adapter0.setOnItemDoneClickListener(myOnItemDoneListener0);

        // 设置置顶操作的点击事件
        adapter0.setOnItemPutTodayClickListener(myOnItemPutTodayListener0);
    }

    private void setInboxTaskListView() {
        inbox_listview = findViewById(R.id.inbox_listview);
        inbox_listview.setMenuCreator(creator);
        adapter1 = new MyListViewAdapter(this, inboxTaskItems, 1);
        inbox_listview.setAdapter(adapter1);

        // 左滑按钮点击事件
        inbox_listview.setOnMenuItemClickListener(myOnMenuItemClickListener1);

        // 设置Listview列表项点击事件
        inbox_listview.setOnItemClickListener(myOnItemClickListener1);

        // 设置checkbox点击事件
        adapter1.setOnItemDoneClickListener(myOnItemDoneListener1);

        // 设置置顶操作的点击事件
        adapter1.setOnItemPutTodayClickListener(myOnItemPutTodayListener1);
    }

    private void initDoneTaskListView() {
        donetask_layout = findViewById(R.id.donetask_layout);
        donetask_listview = findViewById(R.id.donetask_listview);
        adapter2 = new MyListViewAdapter(MainActivity.this, doneTaskItems, 2);
        donetask_listview.setAdapter(adapter2);
        adapter2.setOnItemDoneClickListener(myOnItemDoneListener2);
    }

    // 设置左滑按钮样式
    SwipeMenuCreator creator = new SwipeMenuCreator() {
        @Override
        public void create(SwipeMenu menu) {
            // create "clock" item
            SwipeMenuItem clockItem = new SwipeMenuItem(getApplicationContext());
            clockItem.setBackground(new ColorDrawable(Color.WHITE));
            clockItem.setWidth(dp2px(MainActivity.this, 60));
            clockItem.setIcon(R.drawable.ic_clock);
            clockItem.setBackground(R.color.bggrey);
            menu.addMenuItem(clockItem);

            // create "delete" item
            SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
            deleteItem.setBackground(new ColorDrawable(Color.WHITE));
            deleteItem.setWidth(dp2px(MainActivity.this, 60));
            deleteItem.setIcon(R.drawable.ic_delete);
            deleteItem.setBackground(R.color.bggrey);
            menu.addMenuItem(deleteItem);
        }
    };

    private void fixRollConflict() {
        scrollView = findViewById(R.id.scrollView);
        inbox_listview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                scrollView.requestDisallowInterceptTouchEvent(true);
                int action = motionEvent.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        startX = motionEvent.getX();
                        startY = motionEvent.getY();
                        newTouch = true;
                        //Toast.makeText(MainActivity.this, "0:startY="+startY, Toast.LENGTH_SHORT).show();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        curX = motionEvent.getX();
                        curY = motionEvent.getY();
                        if (newTouch) {
                            // 左滑按钮出现后，本次触摸事件不再处理Scrollview的滑动
                            if (startX - curX > 20 && Math.abs(curY - startY) < 50) {
                                // Toast.makeText(MainActivity.this, "触发左滑", Toast.LENGTH_SHORT).show();
                                newTouch = false;
                            }

                            if (Math.abs(curY - startY) > 100) {
                                //Toast.makeText(MainActivity.this, "1:curY="+curY, Toast.LENGTH_SHORT).show();
                                scrollView.requestDisallowInterceptTouchEvent(false);
                            } else {
                                //Toast.makeText(MainActivity.this, "2:curY="+curY, Toast.LENGTH_SHORT).show();
                                scrollView.requestDisallowInterceptTouchEvent(true);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        //Toast.makeText(MainActivity.this, "4", Toast.LENGTH_SHORT).show();
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        break;
                }

                return false;
            }
        });
        todo_listview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                scrollView.requestDisallowInterceptTouchEvent(true);
                int action = motionEvent.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        startX = motionEvent.getX();
                        startY = motionEvent.getY();
                        newTouch = true;
                        //Toast.makeText(MainActivity.this, "0:startY="+startY, Toast.LENGTH_SHORT).show();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        curX = motionEvent.getX();
                        curY = motionEvent.getY();
                        if (newTouch) {
                            // 左滑按钮出现后，本次触摸事件不再处理Scrollview的滑动
                            if (startX - curX > 20 && Math.abs(curY - startY) < 50) {
                                // Toast.makeText(MainActivity.this, "触发左滑", Toast.LENGTH_SHORT).show();
                                newTouch = false;
                            }

                            if (Math.abs(curY - startY) > 100) {
                                //Toast.makeText(MainActivity.this, "1:curY="+curY, Toast.LENGTH_SHORT).show();
                                scrollView.requestDisallowInterceptTouchEvent(false);
                            } else {
                                //Toast.makeText(MainActivity.this, "2:curY="+curY, Toast.LENGTH_SHORT).show();
                                scrollView.requestDisallowInterceptTouchEvent(true);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        //Toast.makeText(MainActivity.this, "4", Toast.LENGTH_SHORT).show();
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        break;
                }

                return false;
            }
        });
    }

    private void refreshData() {
        if (db == null) {
            db = new DatabaseHelper(this);
        }

        todayTaskItems.clear();
        inboxTaskItems.clear();
        doneTaskItems.clear();

        ArrayList<TaskItem> Items0 = (ArrayList<TaskItem>) db.getTodayTasks();
        ArrayList<TaskItem> Items1 = (ArrayList<TaskItem>) db.getInboxTasks();
        ArrayList<TaskItem> Items2 = (ArrayList<TaskItem>) db.getDoneTasks();

        todayTaskItems.addAll(Items0);
        inboxTaskItems.addAll(Items1);
        doneTaskItems.addAll(Items2);

//        for (int i = 0; i < Items1.size(); i++) {
//            Log.e("inboxdata", i+":"+Items1.get(i).getLastMoveTime());
//        }

        refreshListView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //获取 SensorManager 负责管理传感器
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager != null) {
            //获取加速度传感器
            mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (mAccelerometerSensor != null) {
                mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }

        // 从数据库中读取数据，更新UI
        refreshData();
    }

    @Override
    protected void onPause() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        db.closeDB();
        super.onDestroy();
    }

    private void setAddTask() {
        Button btn_addtodaytask = findViewById(R.id.btn_addtodaytask);
        Button btn_addinboxtask = findViewById(R.id.btn_addinboxtask);
        btn_addtodaytask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTaskDialog(0);
            }
        });
        btn_addinboxtask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTaskDialog(1);
            }
        });
    }

    // tag：0代表今日待办，1代表收件箱
    private void showAddTaskDialog(final int tag) {
        final MyEditDialog dialog = new MyEditDialog(this);
        dialog.setNoOnclickListener("取消", new MyEditDialog.onNoOnclickListener() {
            @Override
            public void onNoClick() {
                dialog.dismiss();
            }
        });
        dialog.setYesOnclickListener("确定", new MyEditDialog.onYesOnclickListener() {
            @Override
            public void onYesClick() {
                String newTaskName = dialog.getEditString();
                boolean isTodayTask = (tag == 0);

                if (newTaskName.length() > 0) {
                    TaskItem newTaskItem = new TaskItem(newTaskName, isTodayTask, new Date().getTime());
                    newTaskItem.setID(db.createTask(newTaskItem));  // 把新增的task写入数据库
                    if (isTodayTask) {
                        todayTaskItems.add(0, newTaskItem);
                        Snackbar.make(scrollView, "成功添加一条任务到「今日待办」", Snackbar.LENGTH_LONG).show();
                    } else {
                        inboxTaskItems.add(0, newTaskItem);
                        Snackbar.make(scrollView, "成功添加一条任务到「收件箱」", Snackbar.LENGTH_LONG).show();
                    }

                    refreshListView();
                    dialog.dismiss();
                }
            }
        });

        dialog.show();

        // Dialog显示0.2秒后弹出软键盘
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                showKeyboard(dialog.getEditText());
            }
        }, 200);
    }

    public void showKeyboard(EditText editText) {
        if (editText != null) {
            //设置可获得焦点
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            //请求获得焦点
            editText.requestFocus();
            //调用系统输入法
            InputMethodManager inputManager = (InputMethodManager) editText
                    .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(editText, 0);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = motionEvent.getX();
                startY = motionEvent.getY();
                newTouch = true;
                break;
        }
        return false;
    }

    private void refreshListView() {
        adapter0.notifyDataSetChanged();
        adapter1.notifyDataSetChanged();
        adapter2.notifyDataSetChanged();
    }


    // “今日待办”置顶按钮点击监听事件
    private MyListViewAdapter.onItemPutTodayListener myOnItemPutTodayListener0 = new MyListViewAdapter.onItemPutTodayListener() {
        @Override
        public void onPutTodayClick(int i) {
            todayTaskItems.get(i).setTodayTask(false);
            todayTaskItems.get(i).setLastMoveTime(new Date().getTime());
            db.updateTask(todayTaskItems.get(i));  // 在数据库中更新task数据

            // 将任务移出“今日待办”，并放回“收件箱”
            TaskItem taskItem = todayTaskItems.get(i);
            todayTaskItems.remove(i);
            inboxTaskItems.add(0, taskItem);
            refreshListView();
            Snackbar.make(scrollView, "已将该任务移动至「收件箱」", Snackbar.LENGTH_LONG).show();
        }
    };


    // “收件箱”置顶按钮点击监听事件
    private MyListViewAdapter.onItemPutTodayListener myOnItemPutTodayListener1 = new MyListViewAdapter.onItemPutTodayListener() {
        @Override
        public void onPutTodayClick(int i) {
            inboxTaskItems.get(i).setTodayTask(true);
            inboxTaskItems.get(i).setLastMoveTime(new Date().getTime());
            db.updateTask(inboxTaskItems.get(i));  // 在数据库中更新task数据

            // 将任务移出“收件箱”，并添加至“今日待办”
            TaskItem taskItem = inboxTaskItems.get(i);
            inboxTaskItems.remove(i);
            todayTaskItems.add(0, taskItem);
            refreshListView();
            Snackbar.make(scrollView, "已将该任务移动至「今日待办」", Snackbar.LENGTH_LONG).show();
        }
    };


    // “今日待办”任务checkbox点击监听事件
    private onItemDoneListener myOnItemDoneListener0 = new onItemDoneListener() {
        @Override
        public void onDoneClick(final int i) {
            todayTaskItems.get(i).setDone(true);

            // 播放音效
            soundPool.play(soundID, 0.5f, 0.5f, 0, 0, 1);

            // 更新删除线的UI
            refreshListView();

            // 延迟0.5秒进行删除操作，并更新UI
            operation_position = i;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message msg = new Message();
                    msg.arg1 = 0;
                    handler.sendMessage(msg);
                }
            }).start();
        }
    };

    // “收件箱”任务checkbox点击监听事件
    private onItemDoneListener myOnItemDoneListener1 = new onItemDoneListener() {
        @Override
        public void onDoneClick(final int i) {
            inboxTaskItems.get(i).setDone(true);

            // 播放音效
            soundPool.play(soundID, 0.5f, 0.5f, 0, 0, 1);

            // 更新删除线的UI
            refreshListView();

            // 延迟0.5秒进行删除操作，并更新UI
            operation_position = i;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message msg = new Message();
                    msg.arg1 = 1;
                    handler.sendMessage(msg);
                }
            }).start();
        }
    };

    // “已完成”任务checkbox点击监听事件
    private onItemDoneListener myOnItemDoneListener2 = new onItemDoneListener() {
        @Override
        public void onDoneClick(final int i) {
            doneTaskItems.get(i).setDone(false);

            // 更新删除线的UI
            refreshListView();

            // 延迟0.5秒进行删除操作，并更新UI
            operation_position = i;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message msg = new Message();
                    msg.arg1 = 2;
                    handler.sendMessage(msg);
                }
            }).start();
        }
    };

    // “今日待办”任务左滑按钮点击事件
    private SwipeMenuListView.OnMenuItemClickListener myOnMenuItemClickListener0 = new SwipeMenuListView.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
            switch (index) {
                case 0:
                    // 进入番茄钟
                    new Thread(new Runnable() {
                        @Override
                        // 延迟0.15秒跳转页面，保证左滑按钮复位
                        public void run() {
                            try {
                                Thread.sleep(150);
                                Intent intent = new Intent(MainActivity.this, PomodoroActivity.class);
                                intent.putExtra("taskname", todayTaskItems.get(position).getTaskName());
                                startActivity(intent);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

//                    Intent intent = new Intent(MainActivity.this, PomodoroActivity.class);
//                    intent.putExtra("taskname", todayTaskItems.get(position).getTaskName());
//                    startActivity(intent);
                    break;
                case 1:
                    // 删除的逻辑
                    db.deleteTask(todayTaskItems.get(position).getID());  // 从数据库中删除
                    todayTaskItems.remove(position);  // 从内存中删除
                    refreshListView();
                    //Toast.makeText(MainActivity.this, "删除操作", Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    };

    // “收件箱”任务左滑按钮点击事件
    private SwipeMenuListView.OnMenuItemClickListener myOnMenuItemClickListener1 = new SwipeMenuListView.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
            switch (index) {
                case 0:
                    // 进入番茄钟
                    new Thread(new Runnable() {
                        @Override
                        // 延迟0.15秒跳转页面，保证左滑按钮复位
                        public void run() {
                            try {
                                Thread.sleep(150);
                                Intent intent = new Intent(MainActivity.this, PomodoroActivity.class);
                                intent.putExtra("taskname", inboxTaskItems.get(position).getTaskName());
                                startActivity(intent);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    break;
                case 1:
                    // 删除的逻辑
                    db.deleteTask(inboxTaskItems.get(position).getID());  // 从数据库中删除
                    inboxTaskItems.remove(position);  // 从内存中删除
                    refreshListView();
                    //Toast.makeText(MainActivity.this, "删除操作", Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    };

    // “今日待办”列表项点击事件
    private AdapterView.OnItemClickListener myOnItemClickListener0 = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            long id = todayTaskItems.get(position).getID();
            String name = todayTaskItems.get(position).getTaskName();
            boolean istoday = todayTaskItems.get(position).isTodayTask();
            String description = todayTaskItems.get(position).getDescription();
            Long lastmovetime = todayTaskItems.get(position).getLastMoveTime();

            Intent intent = new Intent(MainActivity.this, TaskdetailActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("name", name);
            intent.putExtra("istoday", istoday);
            intent.putExtra("description", description);
            intent.putExtra("lastmovetime", lastmovetime);

            startActivity(intent);
        }
    };

    // “收件箱”列表项点击事件
    private AdapterView.OnItemClickListener myOnItemClickListener1 = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            long id = inboxTaskItems.get(position).getID();
            String name = inboxTaskItems.get(position).getTaskName();
            boolean istoday = inboxTaskItems.get(position).isTodayTask();
            String description = inboxTaskItems.get(position).getDescription();
            Long lastmovetime = inboxTaskItems.get(position).getLastMoveTime();

            Intent intent = new Intent(MainActivity.this, TaskdetailActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("name", name);
            intent.putExtra("istoday", istoday);
            intent.putExtra("description", description);
            intent.putExtra("lastmovetime", lastmovetime);

            startActivity(intent);
        }
    };


    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();

        if (type == Sensor.TYPE_ACCELEROMETER) {
            //获取三个方向值
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];

            if ((Math.abs(x) > SENSOR_SHAKE || Math.abs(y) > SENSOR_SHAKE || Math.abs(z) > SENSOR_SHAKE) && !isShake && (scrollView.getVisibility() == View.VISIBLE)) {
                isShake = true;
                soundPool.play(soundID_, 0.5f, 0.5f, 0, 0, 1);

                Log.d("yuyin", "进入onSensorChanger");

                // 检查是否有「语音转文字」所需的相应权限
                final boolean isAllGranted = checkPermissionAllGranted(
                        new String[]{
                                Manifest.permission.RECORD_AUDIO
                        }
                );

                // 如果有权限，启动语音识别功能；如果没有，申请权限
                if (isAllGranted) {
                    xunfei();
                } else {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{
                                    Manifest.permission.RECORD_AUDIO
                            },
                            MY_PERMISSION_REQUEST_CODE
                    );
                }
            }
        }
    }

    /**
     * 申请权限结果返回处理
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            // 如果需要的权限都授予了，启动语音识别功能；如果没有，引导用户手动打开权限
            if (isAllGranted) {
                xunfei();
            } else {
                openAppDetails();
            }
        }

        isShake = false;
    }

    /**
     * 打开 APP 的详情设置
     */
    private void openAppDetails() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("使用「语音输入」功能需要开通“麦克风”权限，请到“应用信息 -> 权限”中开通。");
        builder.setPositiveButton("去开通", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");

        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "700");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    private void addTask(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.clear();
        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        String newTaskName = resultBuffer.toString();
        TaskItem newTask = new TaskItem(newTaskName, false, new Date().getTime());
        newTask.setID(db.createTask(newTask));  // 把新增的task写入数据库
        inboxTaskItems.add(0, newTask);
        refreshListView();

        isShake = false;

        Snackbar.make(scrollView, "成功添加一条任务到「收件箱」", Snackbar.LENGTH_LONG).show();
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d("yuyin", "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                // Toast.makeText(MainActivity.this, "初始化失败，错误码：" + code, Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "好像出了点小问题", Toast.LENGTH_SHORT).show();
            }
        }
    };


    /**
     * 检查是否拥有指定的所有权限
     */
    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    // 科大讯飞语音转文字
    private void xunfei() {

        if (mIat == null) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            //Toast.makeText(this, "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "好像出了点小问题", Toast.LENGTH_SHORT).show();
            return;
        }

        // 移动数据分析，收集开始听写事件
        FlowerCollector.onEvent(MainActivity.this, "iat_recognize");

        mIatResults.clear();

        // 设置参数
        setParam();

        ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            isShake = false;
            //Toast.makeText(this, "听写失败,错误码：" + ret, Toast.LENGTH_SHORT).show();
            Snackbar.make(scrollView, "好像出了点小问题，请稍后再试", Snackbar.LENGTH_LONG).show();
        } else {
            showYuYinDialog();
        }
    }


    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            //showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            //Toast.makeText(MainActivity.this, error.getPlainDescription(true), Toast.LENGTH_SHORT).show();

            if (error.getErrorCode() == 10118) {
                Snackbar.make(scrollView, "你好像没有说话哦", Snackbar.LENGTH_LONG).show();
            } else if (error.getErrorCode() == 20001 || error.getErrorCode() == 20002 || error.getErrorCode() == 20003) {
                Snackbar.make(scrollView, "要连网才能使用「语音输入」功能啦", Snackbar.LENGTH_LONG).show();
            }

            yuyinDialog.dismiss();
            isShake = false;
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            //Toast.makeText(MainActivity.this, "结束说话", Toast.LENGTH_SHORT).show();
            yuyinDialog.dismiss();
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            if (!isLast)
                addTask(results);
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            //showTip("当前正在说话，音量大小：" + volume);
            //Log.d(TAG, "返回音频数据："+data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };


    private void showYuYinDialog() {
        yuyinDialog = new BottomSheetDialog(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_yuyin, null);
        yuyinDialog.setContentView(dialogView);

        ImageView iv = dialogView.findViewById(R.id.iv_microphone);
        iv.setBackgroundResource(R.drawable.microphone_anim);
        AnimationDrawable ad = (AnimationDrawable) iv.getBackground();
        ad.start();

        yuyinDialog.show();
    }

}

