/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.monitorin.hero.doc.docheroheartratemonitoring;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;



import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class DeviceControlActivity extends Activity {

    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    private static final String TAGS = "MyActivity";
    private static final String TAG2 = "clickCheck";

    // BLE stuff
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String Param_childrenID="child";

    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;

    private TextView mConnectionState, mDataField, percentage;
    private TextView mTxtAvg, mTxtMax, mTxtMin;
    private String mDeviceAddress, mDeviceName;


    private BluetoothGattCharacteristic mNotifyCharacteristic;
    public ImageView startButton, stopButton;
    private int restingHR, maximalHR, agePref;
    private String genderPref;
    private int athleticismPref;

    DateFormat enddf = new SimpleDateFormat("HH:mm");


    public static List<Integer> hrBeatsFlow;


    private static int isaData;
    private static int minHB = 65;
    private static int maxHB = 0;
    private static float avgHB = 0;
    private static float sumHB = 0;
    private static float countHB = 0;
    // Display percentage
    private int percentageValue;
    SharedPreferences sharedPref;

    //Section Of Firebase
    private DatabaseReference mMonitorDatabaseReference;

    private MonitorNightModel mMonitorNightModel;

    private static String lastTime_of_store;

    private Boolean sendAlarm = false;

    private LinearLayout mLLConectionState, mLLMonitorState;
    // Code to manage Service lifecycle.




    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.i(TAGS, "BluetoothLeService: ACTION_GATT_CONNECTED \n");
                mConnected = true;
                updateConnectionState(R.string.connected);
                // invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.i(TAGS, "BluetoothLeService: ACTION_GATT_DISCONNECTED \n");
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                //  invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.i(TAGS, "BluetoothLeService: ACTION_GATT_SERVICES_DISCOVERED \n");
                bringHRCharacteristics(mBluetoothLeService.getSupportedGattServices());

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.i(TAGS, "BluetoothLeService: ACTION_DATA_AVAILABLE \n");


                //taking a heart rate value and assigning it to my isaData variable
                isaData = Integer.parseInt(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                displayData(isaData);


            }
        }
    };

    private void clearUI() {
        mDataField.setText(R.string.no_data);
    }

    //---------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_device);

        initializeComponents();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        Param_childrenID = intent.getStringExtra("ChildrenID");

        Log.i(TAG, "Device Name: " + mDeviceName + "   Device Address: " + mDeviceAddress);


        stopButton.setEnabled(false);

//Obtain Instance for Firebase realtime database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mMonitorDatabaseReference = database.getReference("MonitorNightModel").child(Param_childrenID);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sets up UI references.
                Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothLeService.class);
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());







                stopButton.setEnabled(true);
                startButton.setEnabled(false);
                mLLConectionState.setBackgroundColor(getResources().getColor(R.color.green2));
                mConnectionState.setText(getResources().getString(R.string.connected));
                startButton.setImageResource(R.drawable.deactive_start_monitor_btn);
                stopButton.setImageResource(R.drawable.pause_monitor_btn);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertStopMonitoring();

            }
        });


    }

    public void AlertStopMonitoring() {
        DialogInterface.OnClickListener mdialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        mBluetoothLeService.disconnect();
                        unbindService(mServiceConnection);
                        unregisterReceiver(mGattUpdateReceiver);
                        mBluetoothLeService = null;
                        mLLConectionState.setBackgroundColor(getResources().getColor(R.color.deeppink));
                        mConnectionState.setText(getResources().getString(R.string.disconnected));

                        startButton.setEnabled(true);
                        stopButton.setEnabled(false);
                        startButton.setImageResource(R.drawable.start_monitor_btn);
                        stopButton.setImageResource(R.drawable.deactive_pause_monitor_btn);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure for Stop Monitoring?")
                .setPositiveButton("Yes", mdialogClickListener)
                .setNegativeButton("No", mdialogClickListener).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TODO: Do I need this at all?
        // Stops retrieving data from sensor !!! Use when the time of analyse is over.
        //  unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // useful change???
        try {
            unbindService(mServiceConnection);

            unregisterReceiver(mGattUpdateReceiver);
            mBluetoothLeService = null;
        } catch (Exception e) {
            Log.i(TAG, "Error when onDestroy " + e.getMessage());
        }
    }

    public void initializeComponents() {

        sharedPref = getSharedPreferences("mypref", 0);
        startButton = (ImageView) findViewById(R.id.startButton);
        stopButton = (ImageView) findViewById(R.id.stopButton);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        percentage = (TextView) findViewById(R.id.percentOfMaxHR);
        restingHR = sharedPref.getInt("restHR", 0);
        maximalHR = sharedPref.getInt("maximalHR", 0);
        genderPref = sharedPref.getString("gender", "");
        agePref = sharedPref.getInt("ageGroup", 0);
        athleticismPref = sharedPref.getInt("athletecism", 0);
        hrBeatsFlow = new ArrayList<Integer>();
        //-----------
        mTxtAvg = (TextView) findViewById(R.id.txt_data_average);
        mTxtMax = (TextView) findViewById(R.id.txt_data_max);
        mTxtMin = (TextView) findViewById(R.id.txt_data_min);
        //--------
        mLLConectionState = (LinearLayout) findViewById(R.id.LLConnectionState);
        //   mLLMonitorState=(LinearLayout) findViewById(R.id.LLMonitorState);

    }


    // takes HR and finds then stores the max, min and avg HR
    private void saveHBeats(final int hrData) {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        // If received number is not zero
                        if (hrData > 0) {

                            sumHB = sumHB + hrData;
                            countHB++;

                            if (hrData > maxHB) {
                                maxHB = hrData;
                                Log.i(TAGS, "New maxHB: " + maxHB);
                            } else if (hrData < minHB || minHB == 0) {
                                minHB = hrData;
                                Log.i(TAGS, "New minHB: " + minHB);
                            }
                            avgHB = sumHB / countHB;

                            Log.i(TAGS, "this is hrData: " + hrData + "\n");
                            Log.i(TAGS, "Sum: " + sumHB + "\n");
                            Log.i(TAGS, "Average HeartBeat: " + sumHB / countHB);
                        } else {
                            Log.i(TAGS, "0 hrData came: " + hrData);
                        }
                    } catch (Exception e) {
                        Log.i(TAGS, "Exception in 'saveHbeats' runnable");
                    }
                }
            }
        };

        Thread isaThread = new Thread(r);
        isaThread.start();
    }


    // clear all data after analyse is finished
    public static void clearData() {
        hrBeatsFlow.clear();

        minHB = 65;
        sumHB = 0;
        countHB = 0;
        maxHB = 0;
        avgHB = 0;
    }

 /*   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       // getMenuInflater().inflate(R.menu.menu_top_heartrate_monitor, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            mLLConectionState.setBackgroundColor(getResources().getColor(R.color.green2));
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            mLLConectionState.setBackgroundColor(getResources().getColor(R.color.deeppink));
        }
        return true;
    }*/

/*    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                //new one
                registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                if (mBluetoothLeService != null) {
                    final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                    Log.d(TAG, "Connect request result=" + result);
                }
                // mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
                if (resourceId == R.string.connected) {
                    mLLConectionState.setBackgroundColor(getResources().getColor(R.color.green2));
                    mConnectionState.setText(getResources().getString(R.string.connected));
                } else {
                    mLLConectionState.setBackgroundColor(getResources().getColor(R.color.deeppink));
                    mConnectionState.setText(getResources().getString(R.string.disconnected));
                }
            }
        });
    }

    // Displays HR
    private void displayData(int data) {
        if (data > 0) {
            String sdata = Integer.toString(data);
            mDataField.setText(sdata);

            if (maximalHR > 0) {
                percentageValue = data * 100 / maximalHR;
                percentage.setText(percentageValue + "%");

                if (percentageValue > 65) {
                    if (!sendAlarm) {
                        RunAlarm(data);
                        sendAlarm = true;
                    }
                }

            } else {
                if (data > 130) {
                    if (!sendAlarm) {
                        RunAlarm(data);
                        sendAlarm = true;
                    }
                }
            }

            saveHBeats(data);
            mTxtAvg.setText(Math.round(avgHB) + "");
            mTxtMax.setText(maxHB + "");
            mTxtMin.setText(minHB + "");


            try {
                if (lastTime_of_store == null) {
                    lastTime_of_store = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
                    StoreHeartRate(data, percentageValue);
                } else {
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                    String newDate = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
                    Date date1 = format.parse(lastTime_of_store);
                    Date date2 = format.parse(newDate);
                    long mills = date2.getTime() - date1.getTime();
                    int mins = (int) (mills / (1000 * 60)) % 60;
                    if (mins >= 1) {
                        lastTime_of_store = newDate;
                        StoreHeartRate(data, percentageValue);

                        //Alarm Activate again
                        sendAlarm = false;
                    }

                }
            } catch (Exception e) {
                Log.d(TAG, "Error in check time store data=" + e.getMessage());
            }


        }


    }

    private void StoreHeartRate(int heartrate, int maxhr) {

        mMonitorNightModel = new MonitorNightModel();
        mMonitorNightModel.setHeartRate(heartrate);
        mMonitorNightModel.setMAXHR(maxhr);
        String id;
        id = mMonitorDatabaseReference.push().getKey();
        mMonitorNightModel.setMonitorNightModelID(id);
        String date = new SimpleDateFormat("yyyy/MM/dd").format(Calendar.getInstance().getTime());
        mMonitorNightModel.setDate(date);
        String time = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
        mMonitorNightModel.setTime(time);
        mMonitorDatabaseReference.child(id).setValue(mMonitorNightModel);


    }

    // Filters through all characteristics and brings me back the only needed one
    private void bringHRCharacteristics(List<BluetoothGattService> gattServices) {

        if (gattServices == null) Log.i(TAGS, " No Gatt Services visible ");
        String HRServiceUUID = "0000180d-0000-1000-8000-00805f9b34fb";
        String HRMeasurementUUID = "00002a37-0000-1000-8000-00805f9b34fb";

        for (BluetoothGattService gattService : gattServices) {
            if (gattService.getUuid().toString().equals(HRServiceUUID)) {
                Log.i(TAGS, " Came into gatt Service: " + gattService.getUuid().toString());

                final List<BluetoothGattCharacteristic> gattSeveralCharacteristic = gattService.getCharacteristics();

                for (BluetoothGattCharacteristic bleGatt : gattSeveralCharacteristic) {
                    if (bleGatt.getUuid().toString().equals(HRMeasurementUUID)) {

                        Log.i(TAGS, " BLeGatt: " + bleGatt.getUuid().toString());


                        final int charaProp = bleGatt.getProperties();

                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(bleGatt);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = bleGatt;
                            mBluetoothLeService.setCharacteristicNotification(
                                    bleGatt, true);
                        }
                    }
                }
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void SoundAlarm() {
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 1);

            //Create a new PendingIntent and add it to the AlarmManager
            Intent intent = new Intent(getApplicationContext(), AlarmSoundActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                    12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am =
                    (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                    pendingIntent);
        } catch (Exception e) {
            Log.i(TAG, " SoundAlarm Error: " + e.getMessage());
        }


    }

    private void CallAlarm(String phonNumber) {
        try {
            final String _phonNumber=phonNumber;
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    String _uri = "tel:"+ _phonNumber;
                    callIntent.setData(Uri.parse(_uri));
                    startActivity(callIntent);
                }
            }, 1000);


        } catch (Exception e) {
            Log.i(TAG, " CallAlarm Error: " + e.getMessage());
        }
    }

    private void SMSAlarm(String phonNumber,int HR) {
        //Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNo));
        // smsIntent.putExtra("sms_body", message);
        //startActivity(smsIntent);
        try {
                   String date = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(Calendar.getInstance().getTime());
                    String message = "Emergency Help Need,Your Children Blood Sugar is Low. Heart Rate is "+HR +".Time is "+date;
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phonNumber, null, message, null, null);


        } catch (Exception e) {
            Log.i(TAG, " SMSAlarm Error: " + e.getMessage());
        }
    }

    private void RunAlarm(int HR) {

        boolean calling = false;
      /*  for (AttendantModel model : mListAttendantModel) {

            if (model.isSMSActive())
                SMSAlarm(model.getPhone(),HR);


            if (model.isCallActive() && !calling) {
                CallAlarm(model.getPhone());
                calling = true;
            }

        }*/
    SoundAlarm();
    }


}