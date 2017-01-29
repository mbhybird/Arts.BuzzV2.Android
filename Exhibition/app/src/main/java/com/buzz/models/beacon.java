package com.buzz.models;

import java.util.List;

/**
 * Created by NickChung on 7/29/15.
 */
public class beacon {
    private String extag;

    private String beaconid;

    private String displayname;

    private int major;

    private int minor;

    private int priority;

    private int effectiverangein;

    private int effectiverangeout;

    private int throughrange;

    private int effectiverangein_back;

    private int effectiverangeout_back;

    private int throughrange_back;

    private String usage;

    private String rangedirection;

    private List<triggercontent> triggercontent;

    public String getBeaconid() {
        return beaconid;
    }

    public void setBeaconid(String beaconid) {
        this.beaconid = beaconid;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getEffectiverangein() {
        return effectiverangein;
    }

    public void setEffectiverangein(int effectiverangein) {
        this.effectiverangein = effectiverangein;
    }

    public int getEffectiverangeout() {
        return effectiverangeout;
    }

    public void setEffectiverangeout(int effectiverangeout) {
        this.effectiverangeout = effectiverangeout;
    }

    public int getThroughrange() {
        return throughrange;
    }

    public void setThroughrange(int throughrange) {
        this.throughrange = throughrange;
    }

    public int getEffectiverangein_back() {
        return effectiverangein_back;
    }

    public void setEffectiverangein_back(int effectiverangein_back) {
        this.effectiverangein_back = effectiverangein_back;
    }

    public int getEffectiverangeout_back() {
        return effectiverangeout_back;
    }

    public void setEffectiverangeout_back(int effectiverangeout_back) {
        this.effectiverangeout_back = effectiverangeout_back;
    }

    public int getThroughrange_back() {
        return throughrange_back;
    }

    public void setThroughrange_back(int throughrange_back) {
        this.throughrange_back = throughrange_back;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getRangedirection() {
        return rangedirection;
    }

    public void setRangedirection(String rangedirection) {
        this.rangedirection = rangedirection;
    }

    public List<triggercontent> getTriggercontent() {
        return triggercontent;
    }

    public void setTriggercontent(List<triggercontent> triggercontent) {
        this.triggercontent = triggercontent;
    }

    public String getExtag() {
        return extag;
    }

    public void setExtag(String extag) {
        this.extag = extag;
    }
}
