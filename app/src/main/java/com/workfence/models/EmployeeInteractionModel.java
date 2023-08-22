package com.workfence.models;

public class EmployeeInteractionModel {
    private String name;
    private String startTime;
    private String stopTime;
    private String dist;

    public EmployeeInteractionModel(String name, String startTime, String stopTime, String dist) {
        this.name = name;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.dist = dist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public String getDist() {
        return dist;
    }

    public void setDist(String dist) {
        this.dist = dist;
    }
}
