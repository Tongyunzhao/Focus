package com.example.yunzhao.focus;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yunzhao.focus.helper.DatabaseHelper;
import com.example.yunzhao.focus.util.StatusBarUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lecho.lib.hellocharts.formatter.LineChartValueFormatter;
import lecho.lib.hellocharts.formatter.SimpleLineChartValueFormatter;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by Yunzhao on 2018/4/23.
 */

public class StatisticsActivity extends AppCompatActivity {

    private GridView gridView;
    private ArrayList<HashMap<String, Object>> items;

    // 图表
    private LineChartView pomodoroChartView;
    private LineChartView taskChartView;
    private ArrayList<String> dates = new ArrayList<>();  // X轴的标注
    private ArrayList<Float> pomodoroNums = new ArrayList<>();  // 图表1的数据点
    private ArrayList<Float> taskNums = new ArrayList<>();  // 图表2的数据点
    private List<PointValue> mPointValues1 = new ArrayList<PointValue>();  // 图表1
    private List<PointValue> mPointValues2 = new ArrayList<PointValue>();  // 图表2
    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();

    // 数据存储
    private DatabaseHelper db;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        setToolBar();  // 设置toolbar
        StatusBarUtil.setStatusBarColor(getWindow(), this);  // 设置状态栏颜色

        db = new DatabaseHelper(this);

        /*
         * 设置网格
         */
        getData();
        gridView = findViewById(R.id.gridview);
        SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.item_statistics,
                new String[]{"num", "itemname"}, new int[]{R.id.tv_num, R.id.tv_itemname});
        gridView.setAdapter(adapter);


        /*
         * 设置折线图
         */
        getLast7DayData();  // 读取数据
        getAxisXLables();  // 获取x轴的标注
        // 设置第一个折线图
        pomodoroChartView = findViewById(R.id.pomodoroChartView);
        getAxisPoints1();  // 获取坐标点
        initLineChart1();  // 初始化
        // 设置第二个折线图
        taskChartView = findViewById(R.id.taskChartView);
        getAxisPoints2();  // 获取坐标点
        initLineChart2();
    }

    /**
     * 设置X轴的显示
     */
    private void getAxisXLables() {
        for (int i = 0; i < dates.size(); i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(dates.get(i)));
        }
    }

    /**
     * 图表1的每个点的显示
     */
    private void getAxisPoints1() {
        for (int i = 0; i < pomodoroNums.size(); i++) {
            mPointValues1.add(new PointValue(i, pomodoroNums.get(i)));
        }
    }

    /**
     * 图表2的每个点的显示
     */
    private void getAxisPoints2() {
        for (int i = 0; i < taskNums.size(); i++) {
            mPointValues2.add(new PointValue(i, taskNums.get(i)));
        }
    }

    private void initLineChart1(){
        Line line = new Line(mPointValues1).setColor(Color.parseColor("#FFB300"));  //折线的颜色
        line.setShape(ValueShape.CIRCLE);  // 折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.setCubic(false);  //曲线是否平滑，即是曲线还是折线
        line.setFilled(true);  //是否填充曲线的面积
        line.setHasLabels(true);  //曲线的数据坐标是否加上备注
//      line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);  //是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);  //是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）

        LineChartValueFormatter chartValueFormatter = new SimpleLineChartValueFormatter(1);
        line.setFormatter(chartValueFormatter);//显示小数点

        List<Line> lines = new ArrayList<Line>();
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(true);  //  X坐标轴字体是斜的显示还是直的，true是斜的显示
        //axisX.setTextColor(Color.BLACK);  // 设置字体颜色
        //axisX.setName("date");  // 表格名称
        axisX.setTextSize(10);  // 设置字体大小
        axisX.setMaxLabelChars(8); // 最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(mAxisXValues);  // 填充X轴的坐标名称
        data.setAxisXBottom(axisX);  // x 轴在底部
        //data.setAxisXTop(axisX);  // x 轴在顶部
        axisX.setHasLines(true);  // x 轴分割线

        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
        Axis axisY = new Axis();  //Y轴
        axisY.setName("");//y轴标注
        axisY.setTextSize(10);//设置字体大小
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边


        //设置行为属性，支持缩放、滑动以及平移
        pomodoroChartView.setInteractive(true);
        pomodoroChartView.setZoomType(ZoomType.HORIZONTAL);
        pomodoroChartView.setMaxZoom((float) 2);//最大方法比例
        pomodoroChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        pomodoroChartView.setLineChartData(data);
        pomodoroChartView.setVisibility(View.VISIBLE);
        /**注：下面的7，10只是代表一个数字去类比而已
         * 当时是为了解决X轴固定数据个数。见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
         */
        Viewport v = new Viewport(pomodoroChartView.getMaximumViewport());
        v.left = 0;
        v.right= 7;
        pomodoroChartView.setCurrentViewport(v);
    }

    private void initLineChart2(){
        Line line = new Line(mPointValues2).setColor(Color.parseColor("#FFB300"));  //折线的颜色
        line.setShape(ValueShape.CIRCLE);  // 折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.setCubic(false);  //曲线是否平滑，即是曲线还是折线
        line.setFilled(true);  //是否填充曲线的面积
        line.setHasLabels(true);  //曲线的数据坐标是否加上备注
//      line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);  //是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);  //是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）

        List<Line> lines = new ArrayList<Line>();
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(true);  //  X坐标轴字体是斜的显示还是直的，true是斜的显示
        //axisX.setTextColor(Color.BLACK);  // 设置字体颜色
        //axisX.setName("date");  // 表格名称
        axisX.setTextSize(10);  // 设置字体大小
        axisX.setMaxLabelChars(8); // 最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(mAxisXValues);  // 填充X轴的坐标名称
        data.setAxisXBottom(axisX);  // x 轴在底部
        //data.setAxisXTop(axisX);  // x 轴在顶部
        axisX.setHasLines(true);  // x 轴分割线

        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
        Axis axisY = new Axis();  //Y轴
        axisY.setName("");//y轴标注
        axisY.setTextSize(10);//设置字体大小
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边


        //设置行为属性，支持缩放、滑动以及平移
        taskChartView.setInteractive(true);
        taskChartView.setZoomType(ZoomType.HORIZONTAL);
        taskChartView.setMaxZoom((float) 2);//最大方法比例
        taskChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        taskChartView.setLineChartData(data);
        taskChartView.setVisibility(View.VISIBLE);
        /**注：下面的7，10只是代表一个数字去类比而已
         * 当时是为了解决X轴固定数据个数。见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
         */
        Viewport v = new Viewport(taskChartView.getMaximumViewport());
        v.left = 0;
        v.right= 7;
        taskChartView.setCurrentViewport(v);
    }


    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setTypeface(Typeface.SERIF);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    private void getData() {
        items = new ArrayList<HashMap<String, Object>>();

        List<Float> allRecordNum = db.getAllRecordNum();
        String[] itemNames0 = new String[]{"总专注次数", "总专注时长（h）", "总完成任务"};

        for (int i = 0; i < 3; ++i) {
            HashMap<String, Object> item = new HashMap<String, Object>();
            if (i == 1) {
                item.put("num", allRecordNum.get(i));
            } else {
                item.put("num", allRecordNum.get(i).intValue());
            }
            item.put("itemname", itemNames0[i]);
            items.add(item);
        }

        List<Float> todayRecordNum = db.getTodayRecordNum();
        String[] itemNames1 = new String[]{"今日专注次数", "今日专注时长（h）", "今日完成任务"};

        for (int i = 0; i < 3; ++i) {
            HashMap<String, Object> item = new HashMap<String, Object>();
            if (i == 1) {
                item.put("num", todayRecordNum.get(i));
                //Toast.makeText(this, ""+todayRecordNum.get(i), Toast.LENGTH_SHORT).show();
            } else {
                item.put("num", todayRecordNum.get(i).intValue());
            }
            item.put("itemname", itemNames1[i]);
            items.add(item);
        }
    }

    private void getLast7DayData() {
        ArrayList<HashMap<String, Object>> data = db.getLast7DayRecordNum();

        for (int i = 0; i < 7; ++i) {
            HashMap<String, Object> item = data.get(i);
            dates.add((String) item.get("date"));
            pomodoroNums.add((Float) item.get("pomodoroHour"));
            taskNums.add((Float) item.get("doneTaskNum"));
        }
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
