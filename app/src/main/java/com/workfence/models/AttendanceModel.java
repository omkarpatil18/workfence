package com.workfence.models;

public class AttendanceModel {
    String name;
    String inTime;
    String outTime;
    String outsideTime;
    int screenTime;
    int cameraCount;
    int inStatus;
    int outStatus;

    public AttendanceModel(String name, String inTime, String outTime) {
        this.name = name;
        this.inTime = inTime;
        this.outTime = outTime;
    }

    public AttendanceModel(String name, String inTime, String outTime, String outsideTime) {
        this.name = name;
        this.inTime = inTime;
        this.outTime = outTime;
        this.outsideTime = outsideTime;
    }

    public AttendanceModel(String name, String inTime, String outTime, String outsideTime, int screenTime, int cameraCount) {
        this.name = name;
        this.inTime = inTime;
        this.outTime = outTime;
        this.outsideTime = outsideTime;
        this.screenTime = screenTime;
        this.cameraCount = cameraCount;
    }

    public AttendanceModel(String name, String inTime, String outTime, String outsideTime, int screenTime, int cameraCount, int inStatus, int outStatus) {
        this.name = name;
        this.inTime = inTime;
        this.outTime = outTime;
        this.outsideTime = outsideTime;
        this.screenTime = screenTime;
        this.cameraCount = cameraCount;
        this.inStatus = inStatus;
        this.outStatus = outStatus;
    }

    public String getOutsideTime() {
        return outsideTime;
    }

    public void setOutsideTime(String outsideTime) {
        this.outsideTime = outsideTime;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public int getScreenTime() {
        return screenTime;
    }

    public void setScreenTime(int screenTime) {
        this.screenTime = screenTime;
    }

    public int getInStatus() {
        return inStatus;
    }

    public void setInStatus(int inStatus) {
        this.inStatus = inStatus;
    }

    public int getOutStatus() {
        return outStatus;
    }

    public void setOutStatus(int outStatus) {
        this.outStatus = outStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScreenTIme() {
        return screenTime;
    }

    public void setScreenTIme(int screenTIme) {
        this.screenTime = screenTIme;
    }

    public int getCameraCount() {
        return cameraCount;
    }

    public void setCameraCount(int cameraCount) {
        this.cameraCount = cameraCount;
    }
}
