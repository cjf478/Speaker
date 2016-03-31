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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class Dialog2Activity extends ActionBarActivity {

    private String username;
    private String title;
    private String content;
    private boolean[] selected;
    private ProgressDialog progressDialog = null;
    private List<Map<String, Object>> speakerlist=new ArrayList<Map<String,Object>>();
    private ActionBar actionBar;
    private Button actionbar_left;
    private Button actionbar_right;
    private TextView actionbar_up;
    private SimpleDateFormat formatter;
    private Date curDate;
    private String send_data;
    String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog2);
        MyActivityManager mam = MyActivityManager.getInstance();
        mam.pushOneActivity(this);

        formatter= new SimpleDateFormat ("yyyy-MM-dd-HH:mm:ss");

        Bundle bundle=getIntent().getExtras();
        username=bundle.getString("username");
        title=bundle.getString("title");
        content=bundle.getString("content");

        actionBar=getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.actionbar_dialog2);
        actionbar_left=(Button)findViewById(R.id.leftbt);
        actionbar_left.setText("上一步");
        actionbar_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog2Activity.this.finish();
            }
        });
        actionbar_right=(Button)findViewById(R.id.rightbt);
        actionbar_right.setText("合成");
        actionbar_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=0;i<list.size();++i)
                {
                    if (list.get(i).identity.isEmpty())
                    {
                        Toast.makeText(Dialog2Activity.this,"某些话未指定说话人",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                JSONObject data=new JSONObject();
                JSONArray jsonArray=new JSONArray();
                for (int i=0;i<list.size();++i)
                {
                    JSONObject jsonObject=new JSONObject();
                    try {
                        jsonObject.put("speaker_identity",list.get(i).identity);
                        jsonObject.put("sentence_text",list.get(i).text);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    jsonArray.put(jsonObject);
                }
                try {
                    data.put("dialog_content",jsonArray);
                    data.put("username",username);
                    data.put("dialog_title",title);
                    curDate = new Date(System.currentTimeMillis());
                    date = formatter.format(curDate);
                    data.put("create_time",date);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                send_data=data.toString();
                progressDialog= ProgressDialog.show(Dialog2Activity.this, "提示", "载入中，请稍后……", false, false);
                Thread accessWebServiceThread = new Thread(new synth_request());
                accessWebServiceThread.start();
            }
        });
        actionbar_up=(TextView)findViewById(R.id.up_tv);
        actionbar_up.setText(title);

        //载入说话人列表
        progressDialog= ProgressDialog.show(Dialog2Activity.this, "提示", "载入中，请稍后……", false, false);
        Thread accessWebServiceThread = new Thread(new get_user_information_thread());
        accessWebServiceThread.start();
    }

    private final int GET_USER_INFORMATION = 001;
    private final int DOWN_LOAD_IMAGE=002;
    private final int SYNTH_REQUEST=003;

    class synth_request implements Runnable{

        @Override
        public void run() {
            String strUrl="http://speechlab.sjtu.edu.cn/tts/kit/synth_dialog.php";
            WebServer webServer=new WebServer();
            webServer.getHttpClient();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("data", send_data));
            String response=webServer.doPost(strUrl,params);
            Bundle bundle=new Bundle();
            bundle.putString("result",response);
            Message message=new Message();
            message.what=SYNTH_REQUEST;
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }

    class get_user_information_thread implements Runnable{

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
                        try {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("name", jsonArray.getJSONObject(i).get("name").toString());
                            map.put("profile_url", jsonArray.getJSONObject(i).get("profile_url").toString());
                            map.put("identity",jsonArray.getJSONObject(i).get("identity").toString());
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
            message.what=GET_USER_INFORMATION;
            handler.sendMessage(message);
        }
    }



    String[] speakers;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case GET_USER_INFORMATION:
                    progressDialog.dismiss();
                    speakers=new String[speakerlist.size()];
                    selected=new boolean[speakerlist.size()];
                    for (int i=0;i<speakerlist.size();++i)
                    {
                        speakers[i]= (String) speakerlist.get(i).get("name");
                        selected[i]=false;
                    }

                    new AlertDialog.Builder(Dialog2Activity.this)
                            .setTitle("选择参与者")
                            .setCancelable(false)
                            .setMultiChoiceItems(speakers, selected, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    selected[which] = isChecked;
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Dialog2Activity.this.finish();
                                }
                            })
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (int i=selected.length-1;i>=0;--i)
                                    {
                                        if (!selected[i])
                                        {
                                            speakerlist.remove(i);
                                        }
                                    }

                                    speakers=new String[speakerlist.size()];
                                    for (int i=0;i<speakerlist.size();++i)
                                    {
                                        speakers[i]= (String) speakerlist.get(i).get("name");
                                    }

                                    progressDialog= ProgressDialog.show(Dialog2Activity.this, "提示", "载入中，请稍后……", false, false);
                                    new Thread(){
                                        @Override
                                        public void run() {
                                            try {
                                                //创建一个url对象
                                                for (int i=0;i<speakerlist.size();++i)
                                                {
                                                    URL url=new URL(speakerlist.get(i).get("profile_url").toString());
                                                    //打开URL对应的资源输入流
                                                    InputStream is= url.openStream();
                                                    //从InputStream流中解析出图片
                                                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                                                    speakerlist.get(i).put("profile", bitmap);
                                                    //关闭输入流
                                                    is.close();
                                                }
                                                handler.sendEmptyMessage(DOWN_LOAD_IMAGE);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }
                            })
                            .show();
                    break;
                case DOWN_LOAD_IMAGE:
                    progressDialog.dismiss();
                    split_text();
                    generate_listView();
                    break;
                case SYNTH_REQUEST:
                    String result = (String) msg.getData().get("result");
                    progressDialog.dismiss();
                    MyActivityManager.getInstance().finishAllActivity();
                    Intent intent=new Intent();
                    intent.setClass(Dialog2Activity.this,MainActivity.class);
                    intent.putExtra("tab", 1);
                    intent.putExtra("username", username);
                    intent.putExtra("type",1);
                    intent.putExtra("create_time",date);
                    startActivity(intent);
                    break;
            }
        }
    };

    class Item{
        public String identity="";
        public String text="";
        public Item(String i,String t){
            identity=i;
            text=t;
        }
        public Item(String t){
            text=t;
        }
    }
    private LinkedList<Item> list=new LinkedList<>();
    //切分文本
    void split_text(){
        list.clear();
        String  txt=content;
        if (txt.isEmpty())
            return;
        String[] subtxt=txt.split("\n");
        for (int i=0;i<subtxt.length;++i){
            if (subtxt[i].isEmpty())
                continue;
            list.add(new Item(subtxt[i]));
        }
    }

    LinearLayout list_layout;

    void generate_listView()
    {
        list_layout=(LinearLayout)findViewById(R.id.dialog2_list);
        for (int i=0;i<list.size();++i)
        {
            View ItemView=this.getLayoutInflater().inflate(R.layout.dialog2_listitem, null);
            final ImageView iv=(ImageView)ItemView.findViewById(R.id.list_item_profile);
            iv.setBackgroundResource(R.mipmap.default_profile);
            final int index=i;
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(Dialog2Activity.this)
                            .setTitle("选择说话人")
                            .setItems(speakers, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    iv.setImageBitmap((Bitmap)speakerlist.get(which).get("profile"));
                                    list.get(index).identity=(String)speakerlist.get(which).get("identity");
                                }
                            }).show();
                }
            });
            TextView tv=(TextView)ItemView.findViewById(R.id.list_item_text);
            tv.setText(list.get(i).text);
            list_layout.addView(ItemView);
        }
    }
}
