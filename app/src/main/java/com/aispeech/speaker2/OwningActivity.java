package com.aispeech.speaker2;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class OwningActivity extends ActionBarActivity {

    private ActionBar actionBar;
    private String username;
    private List<Map<String, Object>> speakerlist=new ArrayList<Map<String,Object>>();
    private ProgressDialog progressDialog = null;
    private ListView speaker_listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owning);
        actionBar=getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.actionbar_main);
        speaker_listview=(ListView)findViewById(R.id.owning_speaklist);

        //读取用户信息
        progressDialog= ProgressDialog.show(OwningActivity.this, "提示", "载入中，请稍后……",false,false);
        username=getIntent().getStringExtra("username");
        Thread accessWebServiceThread = new Thread(new WebServiceHandler());
        accessWebServiceThread.start();
    }

    private final int ANDROID_ACCESS_CXF_WEBSERVICES = 001;

    class WebServiceHandler implements Runnable{

        @Override
        public void run() {
            String strUrl="http://speechlab.sjtu.edu.cn/tts/kit/user_speakerlist.php";
            WebServer webServer=new WebServer();
            webServer.getHttpClient();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", username));
            String response=webServer.doPost(strUrl,params);
            String json = response;
            if (!json.startsWith("error")) {
                try {
                    JSONArray jsonArray = new JSONArray(json);
                    speakerlist.clear();
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        Bitmap bitmap;
                        try {
                            //创建一个url对象
                            URL url = new URL(jsonArray.getJSONObject(i).get("profile_url").toString());
                            //打开URL对应的资源输入流
                            InputStream is = url.openStream();
                            //从InputStream流中解析出图片
                            bitmap = BitmapFactory.decodeStream(is);
                            //关闭输入流
                            is.close();
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("name", jsonArray.getJSONObject(i).get("name").toString());
                            map.put("profile", bitmap);
                            map.put("due_time", jsonArray.getJSONObject(i).get("due_time").toString());
                            speakerlist.add(map);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Message message=new Message();
            message.what=ANDROID_ACCESS_CXF_WEBSERVICES;
            handler.sendMessage(message);
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case ANDROID_ACCESS_CXF_WEBSERVICES:
                    progressDialog.dismiss();
                    speaker_listview.setAdapter(new SpeakerListAdapter(OwningActivity.this,speakerlist));
                    break;
            }
        }
    };
}
