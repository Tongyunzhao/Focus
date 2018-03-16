package com.example.yunzhao.focus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yunzhao.focus.R;
import com.example.yunzhao.focus.util.StatusBarUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Yunzhao on 2018/3/16.
 */

public class RegisterActivity1 extends AppCompatActivity {

    private TextView toolbar_title;
    private TextView toolbar_nextstep;
    private AutoCompleteTextView tv_phone;
    private AutoCompleteTextView tv_code;
    private TextView btn_code;

    private boolean isPass = false;

    private int curTime = 60;
    Timer timer = null;
    TimerTask task = null;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (curTime == 0) {
                    resetCodeButton();
                } else {
                    btn_code.setText("重新获取("+curTime+")");
                    curTime--;
                }
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 设置toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText("注册");
        toolbar_nextstep = findViewById(R.id.btn_nextstep);
        toolbar_nextstep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 加判断条件

                Intent intent = new Intent(RegisterActivity1.this, RegisterActivity2.class);
                startActivity(intent);
            }
        });
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 设置状态栏颜色
        StatusBarUtil.setStatusBarColor(getWindow(), this);

        // 0.2秒后弹出软键盘
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                showKeyboard(tv_phone);
            }  }, 200);

        tv_phone = findViewById(R.id.phone);
        tv_code = findViewById(R.id.code);
        btn_code = findViewById(R.id.btn_code);

        btn_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btn_code.getText().toString().equals("获取验证码")) {
                    if (isPhoneValid(tv_phone.getText().toString()))
                        sendCode();
                    else
                        tv_phone.setError(getString(R.string.error_invalid_phone));
                }
            }
        });
    }

    private void sendCode() {
        btn_code.setTextColor(getResources().getColor(R.color.iconGrey));
        btn_code.setClickable(false);
        startTimer();

        // 发送验证码


    }

    private void resetCodeButton() {
        btn_code.setTextColor(getResources().getColor(R.color.colorAccent));
        btn_code.setClickable(true);
        btn_code.setText(R.string.get_code);
        stopTimer();
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
        curTime = 60;
    }

    private boolean isPhoneValid(String phone) {
        //TODO: Replace this with your own logic
        return phone.length() == 11;
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

}
