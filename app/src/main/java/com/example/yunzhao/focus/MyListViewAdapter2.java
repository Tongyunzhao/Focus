package com.example.yunzhao.focus;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Yunzhao on 2018/3/15.
 */

public class MyListViewAdapter2 extends BaseAdapter {
    private List<SubtaskItem> subtaskItems;
    private LayoutInflater inflater;

    public MyListViewAdapter2(Context context, List<SubtaskItem> subtaskItems) {
        this.inflater = LayoutInflater.from(context);
        this.subtaskItems = subtaskItems;
    }

    @Override
    public int getCount() {//获取item的个数
        return subtaskItems.size();
    }

    @Override
    public Object getItem(int position) {//获得item
        return subtaskItems.get(position);
    }

    @Override
    public long getItemId(int position) {//获得item的id
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //获取每个item的样式并赋值
        MyListViewAdapter2.Viewholder vh;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_subtask, null);
            vh = new MyListViewAdapter2.Viewholder();
            vh.checkbox = convertView.findViewById(R.id.checkbox);
            vh.subtaskname = convertView.findViewById(R.id.subtaskname);
            convertView.setTag(vh);
        } else {
            vh = (MyListViewAdapter2.Viewholder) convertView.getTag();
        }

        vh.checkbox.setChecked(subtaskItems.get(position).isDone());
        vh.subtaskname.setText(subtaskItems.get(position).getSubtaskName());

        if (subtaskItems.get(position).isDone()) {
            // 设置删除线
            vh.subtaskname.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            vh.subtaskname.getPaint().setFlags(0);
        }

        vh.checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemDoneListener.onDoneClick(position);
            }
        });

        return convertView;
    }

    /**
     * 点击checkbox的监听接口
     */
    public interface onItemDoneListener {
        void onDoneClick(int i);
    }

    private MyListViewAdapter2.onItemDoneListener mOnItemDoneListener;

    public void setOnItemDoneClickListener(MyListViewAdapter2.onItemDoneListener mOnItemDoneListener) {
        this.mOnItemDoneListener = mOnItemDoneListener;
    }

    /*我们新增了一个内部类ViewHolder，用于对控件的实例进行缓存。当convertView为空的时候，创建一个ViewHolder对象，并将控件的实例都存放在ViewHolder里，然后调用View的setTag()方法，将ViewHolder对象存储在View中。当convertView不为空的时候则调用View的getTag()方法，把ViewHolder重新取出。这样所有控件的实例都缓存在了ViewHolder里，就没有必要每次都通过findViewById()方法来获取控件实例了。*/
    public static class Viewholder {
        CheckBox checkbox;
        TextView subtaskname;
    }
}
