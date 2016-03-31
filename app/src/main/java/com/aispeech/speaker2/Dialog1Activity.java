package com.aispeech.speaker2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Dialog1Activity extends ActionBarActivity {

    private String username;
    private ActionBar actionBar;
    private Button actionbar_left;
    private Button actionbar_right;
    private EditText actionbar_up;
    private EditText dialog1_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog1);
        MyActivityManager mam = MyActivityManager.getInstance();
        mam.pushOneActivity(this);

        username=getIntent().getStringExtra("username");
        actionBar=getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.actionbar_dialog1);
        actionbar_left=(Button)findViewById(R.id.leftbt);
        actionbar_left.setText("返回");
        actionbar_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog1Activity.this.finish();
            }
        });
        actionbar_right=(Button)findViewById(R.id.rightbt);
        actionbar_right.setText("下一步");
        actionbar_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = actionbar_up.getText().toString();
                String content = dialog1_et.getText().toString();
                if (title.isEmpty()) {
                    Toast.makeText(Dialog1Activity.this, "标题不为空！", Toast.LENGTH_SHORT).show();
                } else if (content.isEmpty()) {
                    Toast.makeText(Dialog1Activity.this, "内容不为空！", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent=new Intent();
                    Bundle bundle=new Bundle();
                    bundle.putString("title",title);
                    bundle.putString("content",content);
                    bundle.putString("username",username);
                    intent.putExtras(bundle);
                    intent.setClass(Dialog1Activity.this,Dialog2Activity.class);
                    startActivity(intent);
                }
            }
        });
        actionbar_up=(EditText)findViewById(R.id.up_et);
        actionbar_up.setText("新对话");
        dialog1_et=(EditText)findViewById(R.id.dialog1_et);
        dialog1_et.setHint("请输入对话，换行分隔每句话");
    }
}
