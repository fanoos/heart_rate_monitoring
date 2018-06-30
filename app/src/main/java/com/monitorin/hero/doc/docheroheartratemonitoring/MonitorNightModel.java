package com.monitorin.hero.doc.docheroheartratemonitoring;

import java.io.Serializable;

/**
 * Created by CrystalSoft on 5/27/2018.
 */

public class MonitorNightModel implements Serializable  {


    private String MonitorNightModelID;
    private String Date;
    private String Time;
    private int HeartRate;
    private int MAXHR;

    public MonitorNightModel() {

    }

    public MonitorNightModel(String monitorNightModelID, String date, String time, int heartRate, int MAXHR) {
        MonitorNightModelID = monitorNightModelID;
        Date = date;
        Time = time;
        HeartRate = heartRate;
        this.MAXHR = MAXHR;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public int getHeartRate() {
        return HeartRate;
    }

    public void setHeartRate(int heartRate) {
        HeartRate = heartRate;
    }

    public int getMAXHR() {
        return MAXHR;
    }

    public void setMAXHR(int MAXHR) {
        this.MAXHR = MAXHR;
    }
    public String getMonitorNightModelID() {
        return MonitorNightModelID;
    }

    public void setMonitorNightModelID(String monitorNightModelID) {
        MonitorNightModelID = monitorNightModelID;
    }

}
