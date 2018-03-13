package com.example.yunzhao.focus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yunzhao.focus.widget.SubtaskItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Yunzhao on 2018/3/13.
 */

public class MyExpandableListViewAdapter extends BaseExpandableListAdapter {

    List<TaskItem> taskItemList;  // 任务列表的数据
    List<List> subtaskItemList;  // 子任务列表的数据
    Context context;

    int taskLayout;
    int subtaskLayout;

    public MyExpandableListViewAdapter(Context context, int taskLayoutID, int subtaskLayoutID,
                                       List<TaskItem> taskItemList, List<List> subtaskItemList) {
        this.context = context.getApplicationContext();
        this.taskLayout = taskLayoutID;
        this.subtaskLayout = subtaskLayoutID;
        this.taskItemList = taskItemList;
        this.subtaskItemList = subtaskItemList;
    }

    @Override
    public int getGroupCount() {
        return taskItemList.size();
    }

    @Override
    public int getChildrenCount(int i) {
        List<SubtaskItem> itemList = subtaskItemList.get(i);
        return itemList.size();
    }

    @Override
    public Object getGroup(int i) {
        return taskItemList.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        List<SubtaskItem> itemList = subtaskItemList.get(i);
        return itemList.get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean b, View convertView, ViewGroup parent) {
        // 加载Group布局
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(taskLayout, parent, false);
        } else {
            view = convertView;
        }

        // 读取任务项数据
        TaskItem taskItem = (TaskItem) getGroup(groupPosition);
        // 设置数据
        CheckBox checkbox = view.findViewById(R.id.checkbox);
        TextView taskname = view.findViewById(R.id.taskname);
        CheckBox istodaytask = view.findViewById(R.id.istodaytask);
        checkbox.setChecked(taskItem.isDone());
        taskname.setText(taskItem.getTaskName());
        istodaytask.setChecked(taskItem.isTodayTask());

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        // 加载Group布局
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(subtaskLayout, parent, false);
        } else {
            view = convertView;
        }

        // 读取子任务项数据
        SubtaskItem subtaskItem = (SubtaskItem) getChild(groupPosition, childPosition);
        // 设置数据
        CheckBox checkbox = view.findViewById(R.id.checkbox);
        TextView taskname = view.findViewById(R.id.subtaskname);
        CheckBox istodaytask = view.findViewById(R.id.istodaytask);
        checkbox.setChecked(subtaskItem.isDone());
        taskname.setText(subtaskItem.getTaskName());
        istodaytask.setChecked(subtaskItem.isTodayTask());

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
