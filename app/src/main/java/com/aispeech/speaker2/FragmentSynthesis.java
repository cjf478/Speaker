package com.aispeech.speaker2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by chenjiefeng on 15/8/25.
 */
public class FragmentSynthesis extends Fragment {

    private MainActivity mainActivity;
    private View context;
    private ActionBar actionBar;
    private Button bt_read;
    private Button bt_talk;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context=inflater.inflate(R.layout.fragment_synthesis, container, false);
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
        bt_read=(Button)context.findViewById(R.id.read);
        bt_talk=(Button)context.findViewById(R.id.talk);
        bt_read.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                //  intent.setClass(mainActivity,ArticleActivity.class);
                intent.putExtra("username",mainActivity.getUsername());
               // mainActivity.startActivity(intent);
            }
        });
        bt_talk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(mainActivity,Dialog1Activity.class);
                intent.putExtra("username", mainActivity.getUsername());
                mainActivity.startActivity(intent);
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity=(MainActivity)activity;
    }
}
