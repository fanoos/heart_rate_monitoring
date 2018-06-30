package com.monitorin.hero.doc.docheroheartratemonitoring;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;



public class HeartrateMonitorActivity extends AppCompatActivity {
    private static final String TAGOS = "TAGGG";
    static final int PICK_FIND_SENSOR_REQUEST = 5005;  // The request code
    static final int PICK_FIND_ANALYZ_REQUEST = 5006;  // The request code
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public String mDeviceName;
    public String mDeviceAddress;

    private String Param_childrenID="child";

    private ImageView mImgScan;
    private ImageView mImgRun;
    private ImageView mImgAnalayzes;


    private ImageView mBtnBack;

    public int restingHR;
    public int maximalHR;
    public String profileGender;
    public int profileAgeGroup;
    public int profileAthletecism;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public TextView lblRestingHR, lblMaximalHR,lblDeviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_heartrate_monitor);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);




        mImgScan = (ImageView) findViewById(R.id.ImgFindSensorHeartrateMonitor);
        mImgRun = (ImageView) findViewById(R.id.ImgRunHeartrateMonitor);
        mImgAnalayzes = (ImageView) findViewById(R.id.ImgAnalyzesHeartrateMonitor);
        lblRestingHR = (TextView) findViewById(R.id.lbl_restingHR);
        lblMaximalHR = (TextView) findViewById(R.id.lbl_maximalHR);
        lblDeviceName = (TextView) findViewById(R.id.lbl_device_name);


         mImgScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DeviceScanActivity.class);
                startActivityForResult(intent, PICK_FIND_SENSOR_REQUEST);

            }
        });
        mImgRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDeviceAddress!=null) {
                    Intent intent = new Intent(HeartrateMonitorActivity.this, DeviceControlActivity.class);
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, mDeviceName);
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                    intent.putExtra("ChildrenID", Param_childrenID);
                    startActivity(intent);
                }
                else
                {
                    AlertDeviceScan();
                }
            }
        });

        mImgAnalayzes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), ShowAnalyzesMonitorActivity.class);
                startActivity(intent);





            }
        });


      mBtnBack = (ImageView) findViewById(R.id.ImgBtnBackChildDashboard);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        readProfile();

    }
    public void AlertDeviceScan() {
        DialogInterface.OnClickListener mdialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent intent = new Intent(getApplicationContext(), DeviceScanActivity.class);
                        startActivityForResult(intent, PICK_FIND_SENSOR_REQUEST);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Select Your Device?")
                .setPositiveButton("OK", mdialogClickListener)
                .setNegativeButton("Cancel", mdialogClickListener).show();
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to Stop Heart Rate Monitoring?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user pressed "yes", then he is allowed to exit from application
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", "OK");
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app

                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_FIND_SENSOR_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mDeviceName = data.getStringExtra(EXTRAS_DEVICE_NAME);
                mDeviceAddress = data.getStringExtra(EXTRAS_DEVICE_ADDRESS);
                Log.i(TAGOS, "Device Name: " + mDeviceName + "   Device Address: " + mDeviceAddress);
                lblDeviceName.setText(mDeviceName);
            }
        }
    }

    private void calculateMaximalHR(String gender, int age) {
        /***************************
         MAX HR ==
         Male: mhr = 209.6-0.72*age
         Female: mhr = 207.2-0.65*age
         ***************************/
        if (gender.equals("Woman")) {
            maximalHR = Math.round(207.2f - 0.65f * age);
        } else {
            maximalHR = Math.round(209.6f - 0.72f * age);
        }
    }

    private void saveProfile(String _gender, int _age, int _athletecism) {
        if (_age == 0 || _gender == null) return;
        calculateMaximalHR(_gender, _age);

        sharedPref = getSharedPreferences("mypref", 0);
        editor = sharedPref.edit();
        editor.remove("gender");
        editor.remove("ageGroup");
        editor.remove("athletecism");
        editor.remove("maximalHR");
        editor.putString("gender", _gender);
        editor.putInt("ageGroup", _age);
        editor.putInt("athletecism", _athletecism);
        editor.putInt("maximalHR", maximalHR);
        editor.commit();
    }

    void readProfile() {
        sharedPref = getSharedPreferences("mypref", 0);
        maximalHR = sharedPref.getInt("maximalHR", 0);
        if (maximalHR != 0) {
            restingHR = sharedPref.getInt("restHR", 0);
            maximalHR = sharedPref.getInt("maximalHR", 0);
            profileGender = sharedPref.getString("gender", "");
            profileAgeGroup = sharedPref.getInt("ageGroup", 0);
            profileAthletecism = sharedPref.getInt("athletecism", 0);

            changeUI();
        } else {
            saveProfile("Woman", 8, 0);
        }

    }

    private void changeUI() {
        lblRestingHR.setText(Integer.toString(restingHR));
        lblMaximalHR.setText(Integer.toString(maximalHR));
        lblDeviceName.setText(mDeviceName);
    }



}
