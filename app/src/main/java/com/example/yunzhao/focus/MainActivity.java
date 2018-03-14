package com.example.yunzhao.focus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.yunzhao.focus.util.DimenUtil;
import com.example.yunzhao.focus.util.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.yunzhao.focus.util.ListViewUtil.setListViewHeightBasedOnChildren;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SwipeMenuListView todo_listview;
    private SwipeMenuListView inbox_listview;
    private ArrayList<TaskItem> todayTaskItems;
    private ArrayList<TaskItem> inboxTaskItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

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
            // Handle the camera action
        } else if (id == R.id.nav_done) {

        } else if (id == R.id.nav_logout) {

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
        todo_listview.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // 进入番茄钟
                        Intent intent = new Intent(MainActivity.this, PomodoroActivity.class);
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
        //setListViewHeightBasedOnChildren(todo_listview);
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
        //setListViewHeightBasedOnChildren(inbox_listview);
    }

    SwipeMenuCreator creator = new SwipeMenuCreator() {
        @Override
        public void create(SwipeMenu menu) {
            // create "clock" item
            SwipeMenuItem clockItem = new SwipeMenuItem(getApplicationContext());
            clockItem.setBackground(new ColorDrawable(Color.WHITE));
            clockItem.setWidth(dp2px(60));
            clockItem.setIcon(R.drawable.ic_clock);
            clockItem.setBackground(R.color.bggrey);
            menu.addMenuItem(clockItem);

            // create "delete" item
            SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
            deleteItem.setBackground(new ColorDrawable(Color.WHITE));
            deleteItem.setWidth(dp2px(60));
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
        todayTaskItems.add(new TaskItem(true, "买洗衣液", true));
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


//    private void showAddTaskDialog() {
//        final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
//        View dialogView = getLayoutInflater().inflate(R.layout.dialog_addtask, null);
//
//        Button btn_ok = dialogView.findViewById(R.id.btn_ok);
//        final EditText et_addtask = dialogView.findViewById(R.id.et_addtask);
//        mBottomSheetDialog.setContentView(dialogView);
//
//        btn_ok.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mBottomSheetDialog.dismiss();
//            }
//        });
//
//        mBottomSheetDialog.show();
//
//        // Dialog显示0.2秒后弹出软键盘
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                showKeyboard(et_addtask);
//            }  }, 200);
//
//    }
//
//    public void showKeyboard(EditText editText) {
//        if(editText!=null){
//            //设置可获得焦点
//            editText.setFocusable(true);
//            editText.setFocusableInTouchMode(true);
//            //请求获得焦点
//            editText.requestFocus();
//            //调用系统输入法
//            InputMethodManager inputManager = (InputMethodManager) editText
//                    .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//            inputManager.showSoftInput(editText, 0);
//
//        }
//    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
