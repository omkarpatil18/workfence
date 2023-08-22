package com.workfence.models;

public class EmployeeModel {
    String name;
    String uuid;
    String phone;
    long screenTime;
    long workTime;
    float rating;
    String todo;
    Boolean status;

    public EmployeeModel(String name, String uuid, String phone, long screenTime, long workTime, long intTime, String todo, Boolean status) {
        this.name = name;
        this.uuid = uuid;
        this.phone = phone;
        this.screenTime = screenTime;
        this.workTime = workTime;
        this.rating = (float) (100 - ((double) intTime) / (60 * 1000 * 5));
        this.todo = todo;
        this.status = status;
    }

    public EmployeeModel(String name, String uuid, String phone, long screenTime, long workTime, float rating, String todo) {
        this.name = name;
        this.uuid = uuid;
        this.phone = phone;
        this.screenTime = screenTime;
        this.workTime = workTime;
        this.rating = rating;
        this.todo = todo;
    }

    public EmployeeModel(String name, String uuid, String phone) {
        this.name = name;
        this.uuid = uuid;
        this.phone = phone;
    }

    public EmployeeModel(String name, String uuid, String phone, long screenTime, long workTime, long interactionTime) {
        this.name = name;
        this.uuid = uuid;
        this.phone = phone;
        this.screenTime = screenTime;
        this.workTime = workTime;
        this.rating = 100 - ((float) interactionTime) / (60 * 1000 * 5);
    }

    public String getTodo() {
        return todo;
    }

    public void setTodo(String todo) {
        this.todo = todo;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getScreenTime() {
        return screenTime;
    }

    public void setScreenTime(int screenTime) {
        this.screenTime = screenTime;
    }

    public long getWorkTime() {
        return workTime;
    }

    public void setWorkTime(int workTime) {
        this.workTime = workTime;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
