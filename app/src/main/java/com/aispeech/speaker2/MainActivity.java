package com.aispeech.speaker2;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity {
    private FragmentTabHost fragmentTabHost;
    private String texts[] = { "合成", "仓库", "采集", "个人",};
    private Class fragmentArray[] = {FragmentSynthesis.class,
            FragmentWarehouse.class,
            FragmentCollect.class,
            FragmentOwner.class};
    private int mImageViewArray[] = {R.drawable.tab_synthesis_btn,
            R.drawable.tab_warehouse_btn,
            R.drawable.tab_collect_btn,
            R.drawable.tab_owner_btn};

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyActivityManager mam = MyActivityManager.getInstance();
        mam.pushOneActivity(this);
        // 实例化tabhost
        fragmentTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        fragmentTabHost.setup(this, getSupportFragmentManager(), R.id.maincontent);
        for (int i = 0; i < texts.length; i++) {
            TabHost.TabSpec spec=fragmentTabHost.newTabSpec(texts[i]).setIndicator(getView(i));
            fragmentTabHost.addTab(spec, fragmentArray[i], null);
        }
        Intent intent=getIntent();
        //获取用户信息
        username=intent.getStringExtra("username");
        fragmentTabHost.setCurrentTab(intent.getIntExtra("tab",0));
        int type=intent.getIntExtra("type",0);
        switch (type) {
            case 1:
                Intent intent1=new Intent();
                intent1.setClass(this,Dialog3Activity.class);
                intent1.putExtra("username",username);
                intent1.putExtra("create_time",intent.getStringExtra("create_time"));
                startActivity(intent1);
                break;
        }
    }

    public String getUsername()
    {
        return username;
    }
    public FragmentTabHost getFragmentTabHost()
    {
        return fragmentTabHost;
    }

    private View getView(int i) {
        //取得布局实例
        View view=View.inflate(MainActivity.this, R.layout.tab_content, null);
        TextView textView=(TextView) view.findViewById(R.id.text);
        ImageView imageView=(ImageView)view.findViewById(R.id.image);
        imageView.setBackgroundResource(mImageViewArray[i]);
        //设置标题
        textView.setText(texts[i]);
        return view;
    }
}
