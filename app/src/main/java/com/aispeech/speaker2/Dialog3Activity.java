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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class Dialog3Activity extends ActionBarActivity {

    private ActionBar actionBar;
    private String username;
    private String create_time;
    private ProgressDialog progressDialog = null;

    private String title;
    private String dialog_url;

    private TextView play_title;
    private Button share;
    private Button fanhui;
    private Button play_whole;
    private Button delete;

    Player player=new Player();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog3);
        actionBar=getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.actionbar_dialog3);

        fanhui=(Button)findViewById(R.id.fanhui);
        fanhui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog3Activity.this.finish();
            }
        });
        share=(Button)findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v) {

            }
        });
        play_whole=(Button)findViewById(R.id.play_whole);
        play_whole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.setURL(dialog_url);
                player.playUrl();
            }
        });
        delete=(Button)findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new  AlertDialog.Builder(Dialog3Activity.this)
                        .setTitle("确认" )
                        .setMessage("确定删除吗？" )
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog= ProgressDialog.show(Dialog3Activity.this, "提示", "删除中……", false, false);
                                Thread accessWebServiceThread = new Thread(new DeleteData());
                                accessWebServiceThread.start();
                            }
                        })
                        .setNegativeButton("否" , null)
                        .show();
            }
        });
        play_title=(TextView)findViewById(R.id.playtitle);

        Intent intent=getIntent();
        username=intent.getStringExtra("username");
        create_time=intent.getStringExtra("create_time");

        progressDialog= ProgressDialog.show(Dialog3Activity.this, "提示", "载入中，请稍后……", false, false);
        Thread accessWebServiceThread = new Thread(new GetData());
        accessWebServiceThread.start();
    }

    private final int GETDATA=001;
    private final int DELTEDATA=002;

    private List<Map<String,Object>> speakers_init=new ArrayList<>();
    private Map<String,Object> speakers_image=new HashMap<>();
    private List<Map<String,Object>> sentences=new ArrayList<>();

    class DeleteData implements Runnable {

        @Override
        public void run() {
            String strUrl = "http://speechlab.sjtu.edu.cn/tts/kit/dialog_delete.php";
            WebServer webServer = new WebServer();
            webServer.getHttpClient();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("create_time", create_time));
            String json = webServer.doPost(strUrl, params);

            Message message = new Message();
            message.what = DELTEDATA;
            handler.sendMessage(message);
        }
    }


    class GetData implements Runnable {
        @Override
        public void run() {
            String strUrl = "http://speechlab.sjtu.edu.cn/tts/kit/dialog_play.php";
            WebServer webServer = new WebServer();
            webServer.getHttpClient();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("create_time", create_time));
            String json = webServer.doPost(strUrl, params);
            if (!json.startsWith("error")) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray speakers = jsonObject.getJSONArray("speakers");
                    for (int i = 0; i < speakers.length(); ++i) {
                        Map<String, Object> ob = new HashMap<>();
                        ob.put("speaker_id", speakers.getJSONObject(i).get("speaker_id").toString());
                        ob.put("profile_url", speakers.getJSONObject(i).get("profile_url").toString());
                        speakers_init.add(ob);
                    }
                    for (int i = 0; i < speakers_init.size(); ++i) {
                        Bitmap bitmap;
                        try {
                            //创建一个url对象
                            URL url = new URL(speakers_init.get(i).get("profile_url").toString());
                            //打开URL对应的资源输入流
                            InputStream is = url.openStream();
                            //从InputStream流中解析出图片
                            bitmap = BitmapFactory.decodeStream(is);
                            //关闭输入流
                            is.close();
                            speakers_image.put(speakers_init.get(i).get("speaker_id").toString(), bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    title = jsonObject.get("title").toString();
                    dialog_url = jsonObject.get("dialog_url").toString();

                    JSONArray json_sentences = jsonObject.getJSONArray("sentences");
                    for (int i = 0; i < json_sentences.length(); ++i) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("speaker_id", json_sentences.getJSONObject(i).get("speaker_id").toString());
                        map.put("text", json_sentences.getJSONObject(i).get("text").toString());
                        map.put("audio_url", json_sentences.getJSONObject(i).get("audio_url").toString());
                        sentences.add(map);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Message message = new Message();
            message.what = GETDATA;
            handler.sendMessage(message);
        }
    }

        private Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what)
                {
                    case GETDATA:
                        progressDialog.dismiss();
                        generate_listView();
                        play_title.setText(title);
                        break;
                    case DELTEDATA:
                        progressDialog.dismiss();
                        Dialog3Activity.this.finish();
                        break;
                }
            }
        };

    void generate_listView()
    {
        LinearLayout list_layout=(LinearLayout)findViewById(R.id.dialog3_list);
        for (int i=0;i<sentences.size();++i)
        {
            View ItemView=this.getLayoutInflater().inflate(R.layout.dialog2_listitem, null);
            final ImageView iv=(ImageView)ItemView.findViewById(R.id.list_item_profile);
            iv.setImageBitmap((Bitmap)speakers_image.get(sentences.get(i).get("speaker_id")));
            final int index=i;
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    player.setURL(sentences.get(index).get("audio_url").toString());
                    player.playUrl();
                }
            });
            TextView tv=(TextView)ItemView.findViewById(R.id.list_item_text);
            tv.setText(sentences.get(i).get("text").toString());
            list_layout.addView(ItemView);
        }
    }
}

