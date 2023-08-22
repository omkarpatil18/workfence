package com.workfence.models;

public class EmployeeAttendanceModel {
    String inTime;
    String outTime;
    int outsideTime;
    int cameraCount;
    int screenTime;
    int inStatus;
    int outStatus;

    public EmployeeAttendanceModel(String inTime, String outTime, int outsideTime, int cameraCount, int screenTime, int inStatus, int outStatus) {
        this.inTime = inTime;
        this.outTime = outTime;
        this.outsideTime = outsideTime;
        this.cameraCount = cameraCount;
        this.screenTime = screenTime;
        this.inStatus = inStatus;
        this.outStatus = outStatus;
    }

    public EmployeeAttendanceModel(String inTime, String outTime, int outsideTime) {
        this.inTime = inTime;
        this.outTime = outTime;
        this.outsideTime = outsideTime;
    }

    public EmployeeAttendanceModel(String inTime, String outTime, int outsideTime, int cameraCount, int screenTime) {
        this.inTime = inTime;
        this.outTime = outTime;
        this.outsideTime = outsideTime;
        this.cameraCount = cameraCount;
        this.screenTime = screenTime;
    }

    public EmployeeAttendanceModel(String inTime, String outTime) {

        this.inTime = inTime;
        this.outTime = outTime;
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

    public int getOutsideTime() {
        return outsideTime;
    }

    public void setOutsideTime(int outsideTime) {
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

    public int getCameraCount() {
        return cameraCount;
    }

    public void setCameraCount(int cameraCount) {
        this.cameraCount = cameraCount;
    }

    public int getScreenTime() {
        return screenTime;
    }

    public void setScreenTime(int screenTime) {
        this.screenTime = screenTime;
    }


}
