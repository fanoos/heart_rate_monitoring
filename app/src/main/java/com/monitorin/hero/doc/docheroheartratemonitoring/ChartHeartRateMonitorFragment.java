package com.monitorin.hero.doc.docheroheartratemonitoring;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ChartHeartRateMonitorFragment extends Fragment implements OnChartGestureListener,
        OnChartValueSelectedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnChartHeartRateMonitorFragmentListener mListener;
    private LineChart mChart;
    private View root;
    private List<MonitorNightModel> list_monitorNightModel;
    private ArrayList<String> labels;
    private int child_age = 0;
    private String child_name = "";
    private String child_sex = "";
    private String child_id ="";

    private ImageView mLastHourBtn,mLastNightBtn,mLastWeekBtn;

    public ChartHeartRateMonitorFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ChartHeartRateMonitorFragment newInstance(String param1, String param2) {
        ChartHeartRateMonitorFragment fragment = new ChartHeartRateMonitorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_chart_heart_rate_monitor, container, false);
        mChart = (LineChart) root.findViewById(R.id.MPlineChartHeartRate);
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);

        mLastHourBtn=(ImageView) root.findViewById(R.id.ImgLastHour);
        mLastNightBtn=(ImageView) root.findViewById(R.id.ImgLastNight);
        mLastWeekBtn=(ImageView) root.findViewById(R.id.ImgLastWeek);

     //   mCheckChildren();

        mLastHourBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!child_id.equals(""))
                {
                    getHeartRateData_Last_Hour(child_id);
                }
                else
                {
               //     mCheckChildren();
                }

            }
        });

        mLastNightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!child_id.equals(""))
                {
                    getHeartRateData_Last_Night(child_id);
                }
                else
                {
                //    mCheckChildren();
                }
            }
        });

        mLastWeekBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!child_id.equals(""))
                {
                    getHeartRateData_Last_Week(child_id);
                }
                else
                {
                 //   mCheckChildren();
                }
            }
        });
        return root;
    }

    private void getHeartRateData_Last_Hour(String Children_id) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        DatabaseReference _ref = FirebaseDatabase.getInstance().getReference("MonitorNightModel").child(firebaseUser.getUid()).child(Children_id);
        _ref.keepSynced(true);
        Query myquery = _ref.limitToLast(60);
        myquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    list_monitorNightModel = new ArrayList<MonitorNightModel>();
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        MonitorNightModel actModel = data.getValue(MonitorNightModel.class);
                        list_monitorNightModel.add(actModel);
                    }
                    makeLineChart();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Log.w("Hello", "Failed to read value.", databaseError.toException());
            }
        });

    }
    private void getHeartRateData_Last_Night(String Children_id) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        DatabaseReference _ref = FirebaseDatabase.getInstance().getReference("MonitorNightModel").child(firebaseUser.getUid()).child(Children_id);
        _ref.keepSynced(true);

        Date myDate=Calendar.getInstance().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(myDate);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date newDate = calendar.getTime();
        String Target_Date  = new SimpleDateFormat("yyyy/MM/dd").format(newDate);

        Query myquery = _ref.orderByChild("date").startAt(Target_Date);
        myquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    list_monitorNightModel = new ArrayList<MonitorNightModel>();
                    int counter=0;
                    int sum=0;
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        MonitorNightModel actModel = data.getValue(MonitorNightModel.class);
                        if(counter<5)
                        {
                            sum+=actModel.getHeartRate();
                            counter++;
                        }
                        else
                        {
                            actModel.setHeartRate(sum/counter);
                            list_monitorNightModel.add(actModel);
                            counter=0;
                            sum=0;
                        }
                    }
                    makeLineChart();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Log.w("Hello", "Failed to read value.", databaseError.toException());
            }
        });

    }
    private void getHeartRateData_Last_Week(String Children_id) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        DatabaseReference _ref = FirebaseDatabase.getInstance().getReference("MonitorNightModel").child(firebaseUser.getUid()).child(Children_id);
        _ref.keepSynced(true);

        Date myDate=Calendar.getInstance().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(myDate);
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date newDate = calendar.getTime();
        String Target_Date  = new SimpleDateFormat("yyyy/MM/dd").format(newDate);

        Query myquery = _ref.orderByChild("date").startAt(Target_Date);
        myquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    list_monitorNightModel = new ArrayList<MonitorNightModel>();
                    int counter=0;
                    int sum=0;
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        MonitorNightModel actModel = data.getValue(MonitorNightModel.class);
                        if(counter<30)
                        {
                            sum+=actModel.getHeartRate();
                            counter++;
                        }
                        else
                        {
                            actModel.setHeartRate(sum/counter);
                            list_monitorNightModel.add(actModel);
                            counter=0;
                            sum=0;
                        }
                    }
                    makeLineChart();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Log.w("Hello", "Failed to read value.", databaseError.toException());
            }
        });

    }
    private void makeLineChart() {
        // add data
        setData();

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
        // no description text
        Description ds = new Description();
        ds.setText("Heart Rate Line Chart");
        mChart.setDescription(ds);
        mChart.setNoDataText("You need to provide data for the chart.");
        // enable touch gestures
        mChart.setTouchEnabled(true);
        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        //todo change upper and lower base
        float upper =130f;
        float lower = 50f;

        LimitLine upper_limit = new LimitLine(upper, "Upper Limit");
        upper_limit.setLineWidth(4f);
        upper_limit.enableDashedLine(10f, 10f, 0f);
        upper_limit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        upper_limit.setTextSize(10f);

        LimitLine lower_limit = new LimitLine(lower, "Lower Limit");
        lower_limit.setLineWidth(4f);
        lower_limit.enableDashedLine(10f, 10f, 0f);
        lower_limit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        lower_limit.setTextSize(10f);

        YAxis leftAxis = mChart.getAxisLeft();
// reset all limit lines to avoid overlapping lines
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(upper_limit);
        leftAxis.addLimitLine(lower_limit);
        leftAxis.setAxisMaximum(220f);
        leftAxis.setAxisMinimum(0f);
//leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);

// limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);


        //Animate
        mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String action) {
        if (mListener != null) {
            mListener.onChartHeartRateMonitorFragmentInteraction(action);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChartHeartRateMonitorFragmentListener) {
            mListener = (OnChartHeartRateMonitorFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnChartHeartRateMonitorFragmentListener {
        // TODO: Update argument type and name
        void onChartHeartRateMonitorFragmentInteraction(String action);
    }

    //Line Chart
    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            // or highlightTouch(null) for callback to onNothingSelected(...)
            mChart.highlightValues(null);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: "
                + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOWHIGH", "low: " + mChart.getLowestVisibleX()
                + ", high: " + mChart.getHighestVisibleX());

        Log.i("MIN MAX", "xmin: " + mChart.getXChartMin()
                + ", xmax: " + mChart.getXChartMax()
                + ", ymin: " + mChart.getYChartMin()
                + ", ymax: " + mChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    // This is used to store x-axis values
    private ArrayList<String> setXAxisValues() {
        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("10");
        xVals.add("20");
        xVals.add("30");
        xVals.add("30.5");
        xVals.add("40");

        return xVals;
    }

    // This is used to store Y-axis values
    private ArrayList<Entry> setYAxisValues() {
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        labels = new ArrayList<String>();
        labels.add("Start");
        for (int i = 0; i < list_monitorNightModel.size(); i++) {
            yVals.add(new Entry(i + 1, list_monitorNightModel.get(i).getHeartRate()));
            labels.add(list_monitorNightModel.get(i).getDate().toString() + " " + list_monitorNightModel.get(i).getTime().toString());
        }
        labels.add("End");

        return yVals;
    }


    private void setData() {
        ArrayList<String> xVals = setXAxisValues();

        ArrayList<Entry> yVals = setYAxisValues();

        LineDataSet set1;

        // create a dataset and give it a type
        set1 = new LineDataSet(yVals, "Check Heart Rate of " + child_name);
        set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        // set the line to be drawn like this "- - - - - -"
        // set1.enableDashedLine(10f, 5f, 0f);
        // set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setDrawFilled(true);


        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        // LineData data = new LineData(xVals, dataSets);
        LineData data = new LineData(dataSets);


        //final String[] quarters = new String[] { "Q1", "Q2", "Q3", "Q4" , "Q5"};
        final String[] mStringArray = new String[labels.size()];
        // mStringArray = labels.toArray(mStringArray);
        for (int i = 0; i < labels.size(); i++) {
            mStringArray[i] = labels.get(i);
        }
        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mStringArray[(int) value];
            }

            // we don't draw numbers, so no decimal digits needed
            //@Override
            //public int getDecimalDigits() {  return 0; }
        };

        XAxis xAxis = mChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);
        // set data
        mChart.setData(data);
        mChart.invalidate(); // refresh

    }
    private int calculateMaximalHR(String gender, int age) {
        /***************************
         MAX HR ==
         Male: mhr = 209.6-0.72*age
         Female: mhr = 207.2-0.65*age
         ***************************/
        int maximalHR=130;
        if (gender.equals("Woman")) {
            maximalHR = Math.round(207.2f - 0.65f * age);
        } else {
            maximalHR = Math.round(209.6f - 0.72f * age);
        }
        return maximalHR;
    }

   /* public void mgetChildrenOnce( final AutomaticPlanActivity.OnGetDataListener listener) {
        listener.onStart();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        FirebaseDatabase _database = FirebaseDatabase.getInstance();
        DatabaseReference _refchild = _database.getReference("ChildrenModel").child(firebaseUser.getUid());
        _refchild.keepSynced(true);

        Query myqueryChild = _refchild.limitToFirst(1);
        myqueryChild.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    listener.onSuccess(dataSnapshot);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailed(databaseError);
            }
        });



    }

    private void mCheckChildren() {
        mgetChildrenOnce( new AutomaticPlanActivity.OnGetDataListener() {
            @Override
            public void onStart() {

                //DO SOME THING WHEN START GET DATA HERE
            }

            @Override
            public void onSuccess(DataSnapshot data) {
                if (!data.exists()) {
                   // Toast.makeText(getApplicationContext(), "There is not Children", Toast.LENGTH_LONG).show();
                } else {


                    Map<String, Object> value = (Map<String, Object>) data.getValue();
                    String sex = String.valueOf(value.get("sex"));
                    String age = String.valueOf(value.get("age"));
                    for (DataSnapshot _data : data.getChildren()) {
                        ChildrenModel actModel = _data.getValue(ChildrenModel.class);
                        child_age = actModel.getAge();
                        child_name = actModel.getName();
                        child_sex=actModel.getSex();
                        child_id=actModel.getChildrenID();
                    }

                }

            }

            @Override
            public void onFailed(DatabaseError databaseError) {
                //DO SOME THING WHEN GET DATA FAILED HERE
            }
        });

    }*/
}
