package com.example.yunzhao.focus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yunzhao.focus.util.StatusBarUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Yunzhao on 2018/3/16.
 */

public class RegisterActivity2 extends AppCompatActivity {
    private TextView toolbar_title;
    private TextView toolbar_nextstep;
    private AutoCompleteTextView tv_username;
    private AutoCompleteTextView tv_password;
    private AutoCompleteTextView tv_confirmpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        // 设置toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText("注册");
        toolbar_nextstep = findViewById(R.id.btn_nextstep);
        toolbar_nextstep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInput()) {
                    registerRequest();
                }
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
                showKeyboard(tv_username);
            }  }, 200);

        tv_username = findViewById(R.id.username);
        tv_password = findViewById(R.id.password);
        tv_confirmpassword = findViewById(R.id.confirmpassword);

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

    private boolean checkInput() {
        String str_username = tv_username.getText().toString().trim();
        String str_password = tv_password.getText().toString();
        String str_comfirmpassword = tv_confirmpassword.getText().toString();

        if (str_username.length() == 0) {
            tv_username.setError(getString(R.string.error_empty_username));
            return false;
        }

        if (str_password.length() == 0) {
            tv_password.setError(getString(R.string.error_field_required_password));
            return false;
        }

        if (!str_password.equals(str_comfirmpassword)) {
            tv_confirmpassword.setError(getString(R.string.error_confirmpassword));
            return false;
        }

        return true;
    }

    private void registerRequest() {
        Intent intent = new Intent(RegisterActivity2.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
