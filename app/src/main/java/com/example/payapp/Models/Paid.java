package com.example.payapp.Models;

public class Paid {
    String amt, desc, his_id, time,state,seen;
    public Paid(){}

    public Paid(String amt, String desc, String his_id, String time,String state,String seen) {
        this.amt = amt;
        this.desc = desc;
        this.his_id = his_id;
        this.time = time;
        this.state = state;
        this.seen = seen;
    }

    public String getAmt() {
        return amt;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getHis_id() {
        return his_id;
    }

    public void setHis_id(String his_id) {
        this.his_id = his_id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }
}
