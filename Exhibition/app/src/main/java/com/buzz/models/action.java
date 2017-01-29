package com.buzz.models;

/**
 * Created by NickChung on 3/17/15.
 * @deprecated
 */
public class action {
    private String beaconid;

    private String triggertype;

    private String triggercount;

    private String triggerfrequency;

    private String contenttype;

    private String serverpath;

    private String clientpath;

    private String filename;

    public String getTitle_cn() {
        return title_cn;
    }

    public void setTitle_cn(String title_cn) {
        this.title_cn = title_cn;
    }

    public String getTitle_en() {
        return title_en;
    }

    public void setTitle_en(String title_en) {
        this.title_en = title_en;
    }

    public String getTitle_tw() {
        return title_tw;
    }

    public void setTitle_tw(String title_tw) {
        this.title_tw = title_tw;
    }

    public String getTitle_pt() {
        return title_pt;
    }

    public void setTitle_pt(String title_pt) {
        this.title_pt = title_pt;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    private String title_cn;
    private String title_en;
    private String title_tw;
    private String title_pt;
    private int year;

    public String getArtist_pt() {
        return artist_pt;
    }

    public void setArtist_pt(String artist_pt) {
        this.artist_pt = artist_pt;
    }

    public String getArtist_tw() {
        return artist_tw;
    }

    public void setArtist_tw(String artist_tw) {
        this.artist_tw = artist_tw;
    }

    public String getArtist_en() {
        return artist_en;
    }

    public void setArtist_en(String artist_en) {
        this.artist_en = artist_en;
    }

    public String getArtist_cn() {
        return artist_cn;
    }

    public void setArtist_cn(String artist_cn) {
        this.artist_cn = artist_cn;
    }

    private String artist_cn;
    private String artist_en;
    private String artist_tw;
    private String artist_pt;

    public String getDescription_cn() {
        return description_cn;
    }

    public void setDescription_cn(String description_cn) {
        this.description_cn = description_cn;
    }

    public String getDescription_en() {
        return description_en;
    }

    public void setDescription_en(String description_en) {
        this.description_en = description_en;
    }

    public String getDescription_pt() {
        return description_pt;
    }

    public void setDescription_pt(String description_pt) {
        this.description_pt = description_pt;
    }

    public String getDescription_tw() {
        return description_tw;
    }

    public void setDescription_tw(String description_tw) {
        this.description_tw = description_tw;
    }

    private String description_cn;
    private String description_en;
    private String description_tw;
    private String description_pt;

    public void setBeaconid(String beaconid) {
        this.beaconid = beaconid;
    }

    public String getBeaconid() {
        return this.beaconid;
    }

    public void setTriggertype(String triggertype) {
        this.triggertype = triggertype;
    }

    public String getTriggertype() {
        return this.triggertype;
    }

    public void setTriggercount(String triggercount) {
        this.triggercount = triggercount;
    }

    public String getTriggercount() {
        return this.triggercount;
    }

    public void setTriggerfrequency(String triggerfrequency) {
        this.triggerfrequency = triggerfrequency;
    }

    public String getTriggerfrequency() {
        return this.triggerfrequency;
    }

    public void setContenttype(String contenttype) {
        this.contenttype = contenttype;
    }

    public String getContenttype() {
        return this.contenttype;
    }

    public void setServerpath(String serverpath) {
        this.serverpath = serverpath;
    }

    public String getServerpath() {
        return this.serverpath;
    }

    public void setClientpath(String clientpath) {
        this.clientpath = clientpath;
    }

    public String getClientpath() {
        return this.clientpath;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return this.filename;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;

    @Override
    public String toString() {
        return "action{" +
                "beaconid='" + beaconid + '\'' +
                ", triggertype='" + triggertype + '\'' +
                ", triggercount='" + triggercount + '\'' +
                ", triggerfrequency='" + triggerfrequency + '\'' +
                ", contenttype='" + contenttype + '\'' +
                ", serverpath='" + serverpath + '\'' +
                ", clientpath='" + clientpath + '\'' +
                ", filename='" + filename + '\'' +
                ", title_cn='" + title_cn + '\'' +
                ", title_en='" + title_en + '\'' +
                ", title_tw='" + title_tw + '\'' +
                ", title_pt='" + title_pt + '\'' +
                ", year=" + year +
                ", artist_cn='" + artist_cn + '\'' +
                ", artist_en='" + artist_en + '\'' +
                ", artist_tw='" + artist_tw + '\'' +
                ", artist_pt='" + artist_pt + '\'' +
                ", description_cn='" + description_cn + '\'' +
                ", description_en='" + description_en + '\'' +
                ", description_tw='" + description_tw + '\'' +
                ", description_pt='" + description_pt + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}