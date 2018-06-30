package com.monitorin.hero.doc.docheroheartratemonitoring;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ShowAnalyzesMonitorActivity extends AppCompatActivity implements
        ChartHeartRateMonitorFragment.OnChartHeartRateMonitorFragmentListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_show_analyzes_monitor);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void onChartHeartRateMonitorFragmentInteraction(String action) {

    }
}
