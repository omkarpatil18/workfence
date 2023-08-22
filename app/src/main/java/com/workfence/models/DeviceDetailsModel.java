package com.workfence.models;

public class DeviceDetailsModel {
    String address;
    String name;
    int rssi;
    float distance;
    long startTime;
    long stopTime;
    int zone;

    String device_model = "generic";

    public DeviceDetailsModel(String address, String name, int rssi, float distance, long startTime, long stopTime, int zone, String device_model) {
        this.address = address;
        this.name = name;
        this.rssi = rssi;
        this.distance = distance;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.zone = zone;
        this.device_model = device_model;
    }

    public String getDevice_model() {
        return device_model;
    }

    public DeviceDetailsModel(String address, String name, int rssi, float distance, long startTime, long stopTime, int zone) {
        this.address = address;
        this.name = name;
        this.rssi = rssi;
        this.distance = distance;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.zone = zone;
    }

    public void setDevice_model(String device_model) {
        this.device_model = device_model;
    }

    public int getZone() {
        return zone;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }
}
