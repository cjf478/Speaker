package com.aispeech.speaker2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class LoginActivity extends ActionBarActivity {

    private CheckBox remember;
    private CheckBox autologin;
    private SharedPreferences sp;
    private EditText et_username;
    private EditText et_password;
    private ActionBar actionBar;
    String username;
    String password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = new Intent();
        intent.putExtra("username", "test");
        intent.setClass(LoginActivity.this, MainActivity.class);
        LoginActivity.this.startActivity(intent);
        LoginActivity.this.finish();



        actionBar=getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.actionbar_main);

        Button login=(Button)findViewById(R.id.login);
        Button register=(Button)findViewById(R.id.register);
        remember = (CheckBox) findViewById(R.id.remember);
        autologin = (CheckBox) findViewById(R.id.autologin);
        et_username=(EditText)findViewById(R.id.username);
        et_password=(EditText)findViewById(R.id.password);

        autologin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    remember.setChecked(true);
                }
            }
        });

        remember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    if (autologin.isChecked()) {
                        remember.setChecked(true);
                    }
                }
            }
        });

        sp = getSharedPreferences("speaker", 0);
        username=sp.getString("username", "");
        password=sp.getString("password", "");
        boolean choseRemember =sp.getBoolean("remember", false);
        boolean choseAutoLogin =sp.getBoolean("autologin", false);
        if(choseAutoLogin){
            autologin.setChecked(true);
            remember.setChecked(true);
            progressDialog= ProgressDialog.show(LoginActivity.this, "提示", "正在登陆，请稍后……",false,true);
            Thread accessWebServiceThread = new Thread(new WebServiceHandler());
            accessWebServiceThread.start();
        }
        if(choseRemember){
            et_username.setText(username);
            et_password.setText(password);
            remember.setChecked(true);
        }

        login.setOnClickListener(new loginOnClickListener());
        register.setOnClickListener(new registerOnClickListener());
    }

    class loginOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            username = et_username.getText().toString();
            password = et_password.getText().toString();

            if (username.isEmpty()) {
                Toast.makeText(LoginActivity.this, "账号不为空!", Toast.LENGTH_SHORT).show();
            } else if (password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "密码不为空!", Toast.LENGTH_SHORT).show();
            } else {
                progressDialog= ProgressDialog.show(LoginActivity.this, "提示", "正在登陆，请稍后……",false,true);
                Thread accessWebServiceThread = new Thread(new WebServiceHandler());
                accessWebServiceThread.start();
            }
        }
    }

    class registerOnClickListener implements View.OnClickListener
    {

        @Override
        public void onClick(View v) {
            Toast.makeText(LoginActivity.this,"内测账号:test 密码:1234",Toast.LENGTH_SHORT).show();
        }
    }

    private int ANDROID_ACCESS_CXF_WEBSERVICES = 001;
    private ProgressDialog progressDialog = null;

    class WebServiceHandler implements Runnable{

        @Override
        public void run() {
            WebServer webServer=new WebServer();
            String strUrl="http://speechlab.sjtu.edu.cn/tts/kit/login.php";
            webServer.getHttpClient();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username",username));
            params.add(new BasicNameValuePair("password",password));
            String response=webServer.doPost(strUrl,params);
            Message message=new Message();
            Bundle bundle=new Bundle();
            bundle.putString("result",response);
            message.what=ANDROID_ACCESS_CXF_WEBSERVICES;
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==ANDROID_ACCESS_CXF_WEBSERVICES)
            {
                if (progressDialog.isShowing())
                {
                    String result = (String) msg.getData().get("result");
                    switch (result) {
                        case "success":
                            SharedPreferences.Editor editor = sp.edit();
                            //保存用户名和密码
                            editor.putString("username", username);
                            editor.putString("password", password);
                            //是否记住密码
                            if (remember.isChecked()) {
                                editor.putBoolean("remember", true);
                            } else {
                                editor.putBoolean("remember", false);
                            }
                            //是否自动登录
                            if (autologin.isChecked()) {
                                editor.putBoolean("autologin", true);
                            } else {
                                editor.putBoolean("autologin", false);
                            }
                            editor.commit();
                            Intent intent = new Intent();
                            intent.putExtra("username", username);
                            intent.setClass(LoginActivity.this, MainActivity.class);
                            LoginActivity.this.startActivity(intent);
                            LoginActivity.this.finish();
                            break;
                        case "nousername":
                            Toast.makeText(LoginActivity.this, "账号不存在!", Toast.LENGTH_SHORT).show();
                            break;
                        case "failure":
                            Toast.makeText(LoginActivity.this, "密码错误!", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(LoginActivity.this, "网络错误!", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    progressDialog.dismiss();
                }
            }
        }
    };
}
