package com.aispeech.speaker2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chenjiefeng on 15/8/28.
 */
public class WarehouseListAdapter extends BaseExpandableListAdapter {
    //设置组视图的显示文字
    private String[] generalsTypes = new String[] { "对话","文章" };

    private List<List<Map<String, Object>>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public WarehouseListAdapter(Context context,List<List<Map<String, Object>>> data){
        this.context=context;
        this.data=data;
        this.layoutInflater=LayoutInflater.from(context);
    }


    //重写ExpandableListAdapter中的各个方法
    @Override
    public int getGroupCount() {
        // TODO Auto-generated method stub
        return generalsTypes.length;
    }

    @Override
    public Object getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return generalsTypes[groupPosition];
    }

    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return groupPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // TODO Auto-generated method stub
        return data.get(groupPosition).size();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return data.get(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view=layoutInflater.inflate(R.layout.warehouse_groupview, null);
        TextView tv=(TextView)view.findViewById(R.id.groupview_text);
        tv.setText(generalsTypes[groupPosition]);
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view=layoutInflater.inflate(R.layout.warehouse_childview, null);
        TextView tv1=(TextView)view.findViewById(R.id.childview_text_title);
        TextView tv2=(TextView)view.findViewById(R.id.childview_text_time);
        tv1.setText((String)data.get(groupPosition).get(childPosition).get("title"));
        tv2.setText((String)data.get(groupPosition).get(childPosition).get("create_time"));
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition,
                                     int childPosition) {
        // TODO Auto-generated method stub
        return true;
    }
}
