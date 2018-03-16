package com.example.yunzhao.focus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
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
    private ArrayList<TaskItem> todayTaskItems;
    private ArrayList<TaskItem> inboxTaskItems;
    private ArrayList<TaskItem> doneTaskItems;
    private ScrollView scrollView;
    private float startX = 0, curX = 0, startY = 0, curY = 0;
    private Button btn_addtodaytask;
    private Button btn_addinboxtask;
    private TextView toolbar_title;
    private ListView donetask_listview;
    private LinearLayout donetask_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(R.string.app_name);

        // 设置状态栏颜色
        StatusBarUtil.setStatusBarColor(getWindow(), this);

        // 设置左侧抽屉
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        //toggle.setDrawerIndicatorEnabled(false);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 设置今日待办的Listview
        setTodayTaskListView();
        // 设置收件箱的Listview
        setInboxTaskListView();
        // 解决Listview和Scrollview的滚动冲突
        fixRollConflict();

        // 添加待办项
        addTask();

        // 已完成的任务
        donetask_listview = findViewById(R.id.donetask_listview);
        initDoneTaskData();
        MyListViewAdapter adapter = new MyListViewAdapter(MainActivity.this, doneTaskItems);
        donetask_listview.setAdapter(adapter);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

//        if (id == R.id.action_pomodoro) {
//            Intent intent = new Intent(MainActivity.this, PomodoroActivity.class);
//            startActivity(intent);
//
//            return true;
//        }

        return super.onOptionsItemSelected(item);
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
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setTodayTaskListView() {
        todo_listview = findViewById(R.id.todo_listview);
        todo_listview.setMenuCreator(creator);
        initTodayData();
        MyListViewAdapter adapter = new MyListViewAdapter(this, todayTaskItems);
        todo_listview.setAdapter(adapter);

        // 设置左滑按钮点击事件
        todo_listview.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
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
                        //删除的逻辑
                        Toast.makeText(MainActivity.this, "删除操作", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });

        // 设置Listview列表项点击事件
        todo_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //Toast.makeText(MainActivity.this, todayTaskItems.get(position).getTaskName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, TaskdetailActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setInboxTaskListView() {
        inbox_listview = findViewById(R.id.inbox_listview);
        inbox_listview.setMenuCreator(creator);
        initInboxData();
        MyListViewAdapter adapter = new MyListViewAdapter(this, inboxTaskItems);
        inbox_listview.setAdapter(adapter);
        inbox_listview.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
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
                        //删除的逻辑
                        Toast.makeText(MainActivity.this, "删除操作", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
        // 设置Listview列表项点击事件
        inbox_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //Toast.makeText(MainActivity.this, todayTaskItems.get(position).getTaskName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, TaskdetailActivity.class);
                startActivity(intent);
            }
        });
    }

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
        todayTaskItems.add(new TaskItem(false, "确定毕业论文目录结构", true));
        todayTaskItems.add(new TaskItem(false, "设计APP的功能和页面", true));
        todayTaskItems.add(new TaskItem(false, "买洗衣液", true));
    }

    // 设置收件箱列表中的数据
    private void initInboxData() {
        inboxTaskItems = new ArrayList<>();

        // 添加任务信息
        inboxTaskItems.add(new TaskItem(false, "确定毕业论文目录结构", false));
        inboxTaskItems.add(new TaskItem(false, "设计APP的功能和页面", false));
        inboxTaskItems.add(new TaskItem(false, "买洗衣液", false));
        inboxTaskItems.add(new TaskItem(false, "确定毕业论文目录结构", false));
        inboxTaskItems.add(new TaskItem(false, "设计APP的功能和页面", false));
        inboxTaskItems.add(new TaskItem(false, "买洗衣液", false));
    }

    // 设置已完成列表中的数据
    private void initDoneTaskData() {
        doneTaskItems = new ArrayList<>();

        // 添加任务信息
        doneTaskItems.add(new TaskItem(true, "确定毕业论文目录结构", false));
        doneTaskItems.add(new TaskItem(true, "设计APP的功能和页面", false));
        doneTaskItems.add(new TaskItem(true, "买洗衣液", false));
        doneTaskItems.add(new TaskItem(true, "确定毕业论文目录结构", false));
        doneTaskItems.add(new TaskItem(true, "设计APP的功能和页面", false));
        doneTaskItems.add(new TaskItem(true, "买洗衣液", false));
        doneTaskItems.add(new TaskItem(true, "确定毕业论文目录结构", false));
        doneTaskItems.add(new TaskItem(true, "设计APP的功能和页面", false));
        doneTaskItems.add(new TaskItem(true, "买洗衣液", false));
        doneTaskItems.add(new TaskItem(true, "确定毕业论文目录结构", false));
        doneTaskItems.add(new TaskItem(true, "设计APP的功能和页面", false));
        doneTaskItems.add(new TaskItem(true, "买洗衣液", false));
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
                        if (Math.abs(curY-startY) > 100 && startX-curX < 60) {
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
                        if (Math.abs(curY-startY) > 100 && startX-curX < 60) {
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

    private void addTask() {
        btn_addtodaytask = findViewById(R.id.btn_addtodaytask);
        btn_addinboxtask = findViewById(R.id.btn_addinboxtask);
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
    private void showAddTaskDialog(int tag) {
        final MyEditDialog dialog = new MyEditDialog(this);
        dialog.setNoOnclickListener("取消", new MyEditDialog.onNoOnclickListener() {
            @Override
            public void onNoClick() {
                //Toast.makeText(MainActivity.this,"取消",Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        dialog.setYesOnclickListener("确定", new MyEditDialog.onYesOnclickListener() {
            @Override
            public void onYesClick() {
                //Toast.makeText(MainActivity.this,"确定",Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        dialog.show();

        // Dialog显示0.2秒后弹出软键盘
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                showKeyboard(dialog.getEditText());
            }  }, 200);

    }

    public void showKeyboard(EditText editText) {
        if(editText!=null){
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

}
