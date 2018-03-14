package com.example.yunzhao.focus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yunzhao on 2018/3/9.
 */

public class TodoFragment extends Fragment {

    private ExpandableListView expandableListView;

    // listview里面的数据
    private ArrayList<TaskItem> taskItemArrayList;
    private ArrayList<List> subtaskItemArrayList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.todo_layout, container, false);

        expandableListView = view.findViewById(R.id.todo_expandablelistview);
        initData();
        MyExpandableListViewAdapter adapter = new MyExpandableListViewAdapter(this.getContext(), R.layout.item_task, R.layout.item_subtask,
                taskItemArrayList, subtaskItemArrayList);
        expandableListView.setAdapter(adapter);
        // 去掉前面的指示器
        expandableListView.setGroupIndicator(null);
        // 默认展开所有任务
        for (int i = 0; i < adapter.getGroupCount(); i++) {
            expandableListView.expandGroup(i);
        }

        return view;
    }

    // 设置列表中的数据
    private void initData() {
        taskItemArrayList = new ArrayList<>();
        subtaskItemArrayList = new ArrayList<>();

        // 添加任务信息
        taskItemArrayList.add(new TaskItem(false, "写毕业论文", true));
        taskItemArrayList.add(new TaskItem(false, "准备分享会", true));
        taskItemArrayList.add(new TaskItem(true, "买洗衣液", true));

        // 添加“写毕业论文”的子任务信息
        ArrayList<SubtaskItem> subtasks1 = new ArrayList<>();
        subtasks1.add(new SubtaskItem(false, "参考知网论文，确定目录结构", true));
        subtasks1.add(new SubtaskItem(false, "写第一章“绪论", true));
        subtaskItemArrayList.add(subtasks1);

        // 添加“准备分享会”的子任务信息
        ArrayList<SubtaskItem> subtasks2 = new ArrayList<>();
        subtasks2.add(new SubtaskItem(false, "确定分享的内容", true));
        subtaskItemArrayList.add(subtasks2);

        // 添加“买洗衣液”的子任务信息
        ArrayList<SubtaskItem> subtasks3 = new ArrayList<>();
        subtaskItemArrayList.add(subtasks3);

    }
}
