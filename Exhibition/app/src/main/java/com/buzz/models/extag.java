package com.buzz.models;

/**
 * Created by NickChung on 7/28/15.
 */
public class extag implements Comparable<extag> {

    private String extag;
    private String description_cn;
    private String description_en;
    private String description_pt;
    private String description_tw;
    private String title_cn;
    private String title_en;
    private String title_tw;
    private String title_pt;
    private String datefrom;
    private String dateto;
    private String website;
    private String location;
    private String creator;
    private String publish;
    private content content;
    private int fileCount;

    public content getContent() {
        return content;
    }

    public void setContent(content content) {
        this.content = content;
    }

    public String getExtag() {
        return extag;
    }

    public void setExtag(String extag) {
        this.extag = extag;
    }

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

    public String getDatefrom() {
        return datefrom;
    }

    public void setDatefrom(String datefrom) {
        this.datefrom = datefrom;
    }

    public String getDateto() {
        return dateto;
    }

    public void setDateto(String dateto) {
        this.dateto = dateto;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getPublish() {
        return publish;
    }

    public void setPublish(String publish) {
        this.publish = publish;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    @Override
    public int compareTo(extag o) {
        int ex1Df = Integer.parseInt(this.datefrom.replace("-", "").replace("T00:00:00", ""));
        int ex2Df = Integer.parseInt(o.datefrom.replace("-", "").replace("T00:00:00", ""));

        return ex2Df - ex1Df;
    }
}
