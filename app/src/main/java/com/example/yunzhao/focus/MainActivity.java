package com.example.yunzhao.focus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
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
import com.example.yunzhao.focus.util.DimenUtil;
import com.example.yunzhao.focus.util.StatusBarUtil;
import com.example.yunzhao.focus.widget.MyEditDialog;
import com.example.yunzhao.focus.widget.MyListView;
import com.example.yunzhao.focus.widget.MyListView2;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.yunzhao.focus.util.DimenUtil.dp2px;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnTouchListener {

    private MyListView todo_listview;
    private MyListView inbox_listview;

    // 今日待办、收件箱、已完成 3个清单的数据
    private ArrayList<TaskItem> todayTaskItems;
    private ArrayList<TaskItem> inboxTaskItems;
    private ArrayList<TaskItem> doneTaskItems;

    private ScrollView scrollView;
    private float startX = 0, curX = 0, startY = 0, curY = 0;  // 记录手指触摸的位置

    // 标题栏
    private Toolbar toolbar;
    private TextView toolbar_title;

    // 已完成页面
    private ListView donetask_listview;
    private LinearLayout donetask_layout;

    // 音效
    private SoundPool soundPool;  // 声明一个SoundPool
    private int soundID;  // 创建某个声音对应的音频ID

    // 今日待办、收件箱、已完成 3个清单的适配器
    private MyListViewAdapter adapter0;  // 今日待办的适配器
    private MyListViewAdapter adapter1;  // 收件箱的适配器
    private MyListViewAdapter adapter2;  // 已完成的适配器

    private int operation_position;  // 记录当前要进行操作的item位置


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case 0:
                    TaskItem doneTaskItem = todayTaskItems.get(operation_position);
                    todayTaskItems.get(operation_position).setTodayTask(false);
                    todayTaskItems.remove(operation_position);
                    doneTaskItems.add(0, doneTaskItem);
                    refreshListView();
                    break;
                case 1:
                    TaskItem doneTaskItem1 = inboxTaskItems.get(operation_position);
                    inboxTaskItems.remove(operation_position);
                    doneTaskItems.add(0, doneTaskItem1);
                    refreshListView();
                    break;
                case 2:
                    TaskItem doneTaskItem2 = doneTaskItems.get(operation_position);
                    doneTaskItems.remove(operation_position);
                    inboxTaskItems.add(0, doneTaskItem2);
                    refreshListView();
                    Snackbar.make(scrollView, "已将该任务移回「收件箱」", Snackbar.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setToolbar();  // 设置toolbar
        StatusBarUtil.setStatusBarColor(getWindow(), this);  // 设置状态栏颜色
        setDrawerLayout();  // 设置左侧抽屉

        setTodayTaskListView();  // 设置今日待办的Listview
        setInboxTaskListView();  // 设置收件箱的Listview
        fixRollConflict();  // 解决Listview和Scrollview的滚动冲突
        setAddTask();  // 设置添加待办项事件

        initDoneTask();  // 设置“已完成”任务页面

        // 初始化音效
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initSound();
        }

    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(R.string.app_name);
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
    }

    private void initDoneTask() {
        donetask_listview = findViewById(R.id.donetask_listview);
        initDoneTaskData();
        adapter2 = new MyListViewAdapter(MainActivity.this, doneTaskItems, 2);
        donetask_listview.setAdapter(adapter2);
        adapter2.setOnItemDoneClickListener(myOnItemDoneListener2);
        donetask_layout = findViewById(R.id.donetask_layout);
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

        } else if (id == R.id.nav_logout) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setTodayTaskListView() {
        todo_listview = findViewById(R.id.todo_listview);
        todo_listview.setMenuCreator(creator);
        initTodayData();
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
        initInboxData();
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

    // 设置今日待办列表中的数据
    private void initTodayData() {
        todayTaskItems = new ArrayList<>();

        // 添加任务信息
        todayTaskItems.add(new TaskItem(false, "点击左侧的小黑框Done掉任务", true));
        todayTaskItems.add(new TaskItem(false, "任务可以添加描述和子任务", true));
        todayTaskItems.add(new TaskItem(false, "左滑有惊喜(＾－＾)", true));
    }

    // 设置收件箱列表中的数据
    private void initInboxData() {
        inboxTaskItems = new ArrayList<>();

        // 添加任务信息
        inboxTaskItems.add(new TaskItem(false, "每天从「收件箱」里选任务到「今日待办」", false));
        inboxTaskItems.add(new TaskItem(false, "番茄工作法，了解一下", false));
        inboxTaskItems.add(new TaskItem(false, "文字太慢？摇一摇语音输入任务", false));
        inboxTaskItems.add(new TaskItem(false, "从侧边栏可以进入「已完成」的任务哦", false));
    }

    // 设置已完成列表中的数据
    private void initDoneTaskData() {
        doneTaskItems = new ArrayList<>();

        // 添加任务信息
        doneTaskItems.add(new TaskItem(true, "这是一条已经完成的任务", false));
        doneTaskItems.add(new TaskItem(true, "重做任务？点左侧小黑框移回「收件箱」", false));
    }

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
                        //Toast.makeText(MainActivity.this, "0:startY="+startY, Toast.LENGTH_SHORT).show();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        curX = motionEvent.getX();
                        curY = motionEvent.getY();
                        if (Math.abs(curY - startY) > 100 && startX - curX < 60) {
                            //Toast.makeText(MainActivity.this, "1:curY="+curY, Toast.LENGTH_SHORT).show();
                            scrollView.requestDisallowInterceptTouchEvent(false);
                        } else {
                            //Toast.makeText(MainActivity.this, "2:curY="+curY, Toast.LENGTH_SHORT).show();
                            scrollView.requestDisallowInterceptTouchEvent(true);
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
                        //Toast.makeText(MainActivity.this, "0:startY="+startY, Toast.LENGTH_SHORT).show();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        curX = motionEvent.getX();
                        curY = motionEvent.getY();
                        if (Math.abs(curY - startY) > 100 && startX - curX < 60) {
                            //Toast.makeText(MainActivity.this, "1:curY="+curY, Toast.LENGTH_SHORT).show();
                            scrollView.requestDisallowInterceptTouchEvent(false);
                        } else {
                            //Toast.makeText(MainActivity.this, "2:curY="+curY, Toast.LENGTH_SHORT).show();
                            scrollView.requestDisallowInterceptTouchEvent(true);
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
                    TaskItem newTaskItem = new TaskItem(false, newTaskName, isTodayTask);
                    if (isTodayTask) {
                        todayTaskItems.add(0, newTaskItem);
                    } else {
                        inboxTaskItems.add(0, newTaskItem);
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
            boolean curIsToday = todayTaskItems.get(i).isTodayTask();
            todayTaskItems.get(i).setTodayTask(!curIsToday);

            // 将任务移出“今日待办”，并放回“收件箱”
            if (curIsToday) {
                TaskItem taskItem = todayTaskItems.get(i);
                todayTaskItems.remove(i);
                inboxTaskItems.add(0, taskItem);
                refreshListView();
            }
        }
    };


    // “收件箱”置顶按钮点击监听事件
    private MyListViewAdapter.onItemPutTodayListener myOnItemPutTodayListener1 = new MyListViewAdapter.onItemPutTodayListener() {
        @Override
        public void onPutTodayClick(int i) {
            boolean curIsToday = inboxTaskItems.get(i).isTodayTask();
            inboxTaskItems.get(i).setTodayTask(!curIsToday);

            // 将任务移出“收件箱”，并添加至“今日待办”
            if (!curIsToday) {
                TaskItem taskItem = inboxTaskItems.get(i);
                inboxTaskItems.remove(i);
                todayTaskItems.add(0, taskItem);
                refreshListView();
            }
        }
    };


    // “今日待办”任务checkbox点击监听事件
    private onItemDoneListener myOnItemDoneListener0 = new onItemDoneListener() {
        @Override
        public void onDoneClick(final int i) {
            boolean curCheckedStatus = todayTaskItems.get(i).isDone();
            todayTaskItems.get(i).setDone(!curCheckedStatus);

            // 播放音效
            if (todayTaskItems.get(i).isDone())
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
            boolean curCheckedStatus = inboxTaskItems.get(i).isDone();
            inboxTaskItems.get(i).setDone(!curCheckedStatus);

            // 播放音效
            if (inboxTaskItems.get(i).isDone())
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
        public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
            switch (index) {
                case 0:
                    // 进入番茄钟
                    Intent intent = new Intent(MainActivity.this, PomodoroActivity.class);
                    intent.putExtra("taskname", todayTaskItems.get(position).getTaskName());
                    startActivity(intent);
                    break;
                case 1:
                    // 删除的逻辑
                    todayTaskItems.remove(position);
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
        public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
            switch (index) {
                case 0:
                    // 进入番茄钟
                    Intent intent = new Intent(MainActivity.this, PomodoroActivity.class);
                    intent.putExtra("taskname", inboxTaskItems.get(position).getTaskName());
                    startActivity(intent);
                    break;
                case 1:
                    // 删除的逻辑
                    inboxTaskItems.remove(position);
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
            //Toast.makeText(MainActivity.this, todayTaskItems.get(position).getTaskName(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, TaskdetailActivity.class);
            startActivity(intent);
        }
    };

    // “收件箱”列表项点击事件
    private AdapterView.OnItemClickListener myOnItemClickListener1 = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            //Toast.makeText(MainActivity.this, todayTaskItems.get(position).getTaskName(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, TaskdetailActivity.class);
            startActivity(intent);
        }
    };
}
