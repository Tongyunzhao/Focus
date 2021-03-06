package com.example.yunzhao.focus;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Yunzhao on 2018/3/14.
 */

public class MyListViewAdapter extends BaseAdapter {
    private List<TaskItem> taskItems;
    private LayoutInflater inflater;
    private int type;  // 0：今日待办； 1：收件箱； 2：已完成

    public MyListViewAdapter(Context context, List<TaskItem> taskItems, int type) {
        this.inflater = LayoutInflater.from(context);
        this.taskItems = taskItems;
        this.type = type;
    }

    @Override
    public int getCount() {//获取item的个数
        return taskItems.size();
    }

    @Override
    public Object getItem(int position) {//获得item
        return taskItems.get(position);
    }

    @Override
    public long getItemId(int position) {//获得item的id
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //获取每个item的样式并赋值
        final Viewholder vh;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_task, null);
            vh = new Viewholder();
            vh.checkbox = convertView.findViewById(R.id.checkbox);
            vh.taskname = convertView.findViewById(R.id.taskname);
            vh.istodaytask = convertView.findViewById(R.id.istodaytask);
            convertView.setTag(vh);
        } else {
            vh = (Viewholder) convertView.getTag();
        }

        vh.checkbox.setChecked(taskItems.get(position).isDone());
        vh.taskname.setText(taskItems.get(position).getTaskName());
        vh.istodaytask.setChecked(taskItems.get(position).isTodayTask());

        if (taskItems.get(position).isDone()) {
            // 设置删除线
            vh.taskname.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            vh.taskname.getPaint().setFlags(0);
        }

        // 已完成的列表中不显示置顶按钮
        if (type == 2) {
            vh.istodaytask.setVisibility(View.GONE);
        }

        vh.checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemDoneListener.onDoneClick(position);
            }
        });

        vh.istodaytask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemPutTodayListener.onPutTodayClick(position);
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

    private onItemDoneListener mOnItemDoneListener;

    public void setOnItemDoneClickListener(onItemDoneListener mOnItemDoneListener) {
        this.mOnItemDoneListener = mOnItemDoneListener;
    }

    /**
     * 点击置顶按钮的监听接口
     */
    public interface onItemPutTodayListener {
        void onPutTodayClick(int i);
    }

    private onItemPutTodayListener mOnItemPutTodayListener;

    public void setOnItemPutTodayClickListener(onItemPutTodayListener mOnItemPutTodayListener) {
        this.mOnItemPutTodayListener = mOnItemPutTodayListener;
    }



    /*我们新增了一个内部类ViewHolder，用于对控件的实例进行缓存。当convertView为空的时候，创建一个ViewHolder对象，并将控件的实例都存放在ViewHolder里，然后调用View的setTag()方法，将ViewHolder对象存储在View中。当convertView不为空的时候则调用View的getTag()方法，把ViewHolder重新取出。这样所有控件的实例都缓存在了ViewHolder里，就没有必要每次都通过findViewById()方法来获取控件实例了。*/
    public static class Viewholder {
        CheckBox checkbox;
        TextView taskname;
        CheckBox istodaytask;
    }
}
