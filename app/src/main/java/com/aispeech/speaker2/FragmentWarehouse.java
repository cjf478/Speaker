package com.aispeech.speaker2;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by chenjiefeng on 15/8/25.
 */
public class FragmentWarehouse extends Fragment {

    private MainActivity mainActivity;
    private View context;
    private ActionBar actionBar;
    private String username;
    private List<List<Map<String, Object>>> data=new ArrayList<>();
    private Button renew;
    private ProgressDialog progressDialog = null;
    ExpandableListView expandableListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context=inflater.inflate(R.layout.fragment_warehouse, container, false);
        return context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //设置actionbar
        actionBar=mainActivity.getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.actionbar_main);
        renew=(Button)mainActivity.findViewById(R.id.renew);
        renew.setVisibility(View.VISIBLE);
        renew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取信息
                progressDialog= ProgressDialog.show(mainActivity, "提示", "载入中，请稍后……", false, false);
                Thread accessWebServiceThread = new Thread(new WebGetData());
                accessWebServiceThread.start();
            }
        });

        username=mainActivity.getUsername();

        //获取信息
        progressDialog= ProgressDialog.show(mainActivity, "提示", "载入中，请稍后……", false, false);
        Thread accessWebServiceThread = new Thread(new WebGetData());
        accessWebServiceThread.start();

        final ExpandableListAdapter adapter = new WarehouseListAdapter(mainActivity,data);

        expandableListView = (ExpandableListView) mainActivity.findViewById(R.id.warehouse_list);
        expandableListView.setAdapter(adapter);

        //设置item点击的监听器
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                switch (groupPosition)
                {
                    case 0:
                        Intent intent=new Intent();
                        intent.setClass(mainActivity,Dialog3Activity.class);
                        intent.putExtra("username",username);
                        intent.putExtra("create_time",data.get(groupPosition).get(childPosition).get("create_time").toString());
                        startActivity(intent);
                        break;
                    case 1:
                        break;
                }
                return false;
            }
        });

    }

    private final int GETDATA=001;

    class WebGetData implements Runnable{

        @Override
        public void run() {
            String strUrl="http://speechlab.sjtu.edu.cn/tts/kit/warehouse.php";
            WebServer webServer=new WebServer();
            webServer.getHttpClient();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", username));
            String json=webServer.doPost(strUrl,params);
            if (!json.startsWith("error")) {
                try {
                    data.clear();
                    JSONObject Data = new JSONObject(json);
                    JSONArray Dialogs=Data.getJSONArray("dialog");
                    List<Map<String, Object>> list1=new ArrayList<>();
                    for (int i=0;i<Dialogs.length();++i)
                    {
                        Map<String ,Object> ob=new HashMap<>();
                        ob.put("title", Dialogs.getJSONObject(i).get("title").toString());
                        ob.put("create_time", Dialogs.getJSONObject(i).get("create_time").toString());
                        ob.put("dialog_id", Dialogs.getJSONObject(i).get("dialog_id").toString());
                        list1.add(ob);
                    }
                    data.add(list1);
                    List<Map<String, Object>> list2=new ArrayList<>();
                    data.add(list2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Message message=new Message();
            message.what=GETDATA;
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
                    expandableListView.collapseGroup(0);
                    expandableListView.collapseGroup(1);
                    break;
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity=(MainActivity)activity;
    }
}
