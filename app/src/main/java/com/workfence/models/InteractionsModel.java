package com.workfence.models;

public class InteractionsModel {
    String uuid1;
    String uuid2;
    String name1;
    String name2;
    String startTime;
    String stopTime;
    String avg_dist;
    String min_dist;

    public InteractionsModel(String uuid1, String uuid2, String name1, String name2, String startTime, String stopTime, String avg_dist) {
        this.uuid1 = uuid1;
        this.uuid2 = uuid2;
        this.name1 = name1;
        this.name2 = name2;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.avg_dist = avg_dist;
    }

    public InteractionsModel(String name1, String name2, String startTime, String avg_dist) {
        this.name1 = name1;
        this.name2 = name2;
        this.startTime = startTime;
        this.avg_dist = avg_dist;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public String getMin_dist() {
        return min_dist;
    }

    public void setMin_dist(String min_dist) {
        this.min_dist = min_dist;
    }

    public String getString() {
        return name1 + " and " + name2 + " from " + startTime + " to " + stopTime;
    }

    public String getUuid1() {
        return uuid1;
    }

    public void setUuid1(String uuid1) {
        this.uuid1 = uuid1;
    }

    public String getUuid2() {
        return uuid2;
    }

    public void setUuid2(String uuid2) {
        this.uuid2 = uuid2;
    }

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getAvg_dist() {
        return avg_dist;
    }

    public void setAvg_dist(String avg_dist) {
        this.avg_dist = avg_dist;
    }
}
