package com.buzz.models;

/**
 * Created by NickChung on 7/29/15.
 */
public class trigger {

    private int triggertype;

    private int triggercount;

    private int triggerfrequency;

    private String triggerid;

    public int getTriggertype() {
        return triggertype;
    }

    public void setTriggertype(int triggertype) {
        this.triggertype = triggertype;
    }

    public int getTriggercount() {
        return triggercount;
    }

    public void setTriggercount(int triggercount) {
        this.triggercount = triggercount;
    }

    public int getTriggerfrequency() {
        return triggerfrequency;
    }

    public void setTriggerfrequency(int triggerfrequency) {
        this.triggerfrequency = triggerfrequency;
    }

    public String getTriggerid() {
        return triggerid;
    }

    public void setTriggerid(String triggerid) {
        this.triggerid = triggerid;
    }
}