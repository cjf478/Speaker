package com.aispeech.speaker2;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by chenjiefeng on 15/8/27.
 */
public class SpeakerListAdapter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public final class Zujian{
        public ImageView tv_profile;
        public TextView tv_name;
        public TextView tv_duetime;
    }

    public SpeakerListAdapter(Context context,List<Map<String, Object>> data){
        this.context=context;
        this.data=data;
        this.layoutInflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Zujian zujian=null;
        if(convertView==null){
            zujian=new Zujian();
            //获得组件，实例化组件
            convertView=layoutInflater.inflate(R.layout.speakerlist_item, null);
            zujian.tv_profile=(ImageView)convertView.findViewById(R.id.speaker_profile);
            zujian.tv_name=(TextView)convertView.findViewById(R.id.speaker_name);
            zujian.tv_duetime=(TextView)convertView.findViewById(R.id.speaker_duetime);
            convertView.setTag(zujian);
        }else{
            zujian=(Zujian)convertView.getTag();
        }
        //绑定数据
        zujian.tv_profile.setImageBitmap((Bitmap) data.get(position).get("profile"));
        zujian.tv_name.setText((String)data.get(position).get("name"));
        zujian.tv_duetime.setText((String)data.get(position).get("due_time"));
        return convertView;
    }
}
