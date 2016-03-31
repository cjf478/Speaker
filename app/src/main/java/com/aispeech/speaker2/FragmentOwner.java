package com.aispeech.speaker2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by chenjiefeng on 15/8/25.
 */
public class FragmentOwner extends Fragment {

    private MainActivity mainActivity;
    private Bitmap bitmap;
    private String username;
    private String nickname;
    private ImageView profile;
    private String profile_url="";
    private TextView tv_username;
    private TextView tv_nickname;
    private Button bt_logout;
    private Button bt_owning;
    private View context;
    private SharedPreferences sp;
    private ActionBar actionBar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context=inflater.inflate(R.layout.fragment_owner, container, false);
        return context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        username=mainActivity.getUsername();
        //设置actionbar
        actionBar=mainActivity.getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.actionbar_main);

        profile=(ImageView)context.findViewById(R.id.profile);
        profile.setBackgroundResource(R.mipmap.default_profile);
        tv_username=(TextView)context.findViewById(R.id.tv_username);
        tv_username.setText(username);
        tv_nickname=(TextView)context.findViewById(R.id.tv_nickname);
        bt_logout=(Button)context.findViewById(R.id.logout);
        bt_logout.setOnClickListener(new LogoutOnClickListener());
        bt_owning=(Button)context.findViewById(R.id.owning);
        bt_owning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("username",username);
                intent.setClass(mainActivity, OwningActivity.class);
                mainActivity.startActivity(intent);
            }
        });

        //读取用户信息
        Thread accessWebServiceThread = new Thread(new WebServiceHandler());
        accessWebServiceThread.start();
    }

    class LogoutOnClickListener implements View.OnClickListener
    {

        @Override
        public void onClick(View v) {
            sp = mainActivity.getSharedPreferences("speaker", 0);
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.commit();
            Intent intent = new Intent();
            intent.setClass(mainActivity, LoginActivity.class);
            mainActivity.startActivity(intent);
            mainActivity.finish();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity=(MainActivity)activity;
    }

    private final int ANDROID_ACCESS_CXF_WEBSERVICES = 001;
    private final int DOWN_LOAD_IMAGE=002;
    private ProgressDialog progressDialog = null;

    class WebServiceHandler implements Runnable{

        @Override
        public void run() {
            WebServer webServer=new WebServer();
            String strUrl="http://speechlab.sjtu.edu.cn/tts/kit/user_information.php";
            webServer.getHttpClient();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username",username));
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
            switch (msg.what)
            {
                case ANDROID_ACCESS_CXF_WEBSERVICES:
                    String json = (String) msg.getData().get("result");
                    if (!json.startsWith("error"))
                    {
                        JSONObject jsonObject;
                        try {
                            jsonObject=new JSONObject(json);
                            profile_url=jsonObject.getString("head_photo_url");
                            nickname=jsonObject.getString("nickname");
                            tv_nickname.setText(nickname);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new Thread(){
                            @Override
                            public void run() {
                                try {
                                    //创建一个url对象
                                    URL url=new URL(profile_url);
                                    //打开URL对应的资源输入流
                                    InputStream is= url.openStream();
                                    //从InputStream流中解析出图片
                                    bitmap = BitmapFactory.decodeStream(is);
                                    //  imageview.setImageBitmap(bitmap);
                                    //发送消息，通知UI组件显示图片
                                    handler.sendEmptyMessage(DOWN_LOAD_IMAGE);
                                    //关闭输入流
                                    is.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                    else
                    {
                        profile_url="";
                    }
                    break;
                case DOWN_LOAD_IMAGE:
                    profile.setImageBitmap(bitmap);
                    break;
            }
        }
    };
}
