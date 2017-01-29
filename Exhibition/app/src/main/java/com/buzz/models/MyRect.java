package com.buzz.models;

import java.util.Date;

/**
 * Created by NickChung on 1/8/16.
 */
public class MyRect implements Comparable<MyRect> {
    public int RSSI;
    public String Tag;
    public String Title;
    public String ImagePath;
    public String BeaconId;
    public String AudioPath;
    public Long CreateTimeTicks;

    public MyRect(int rssi, String tag, String title, String imagePath, String audioPath, String beaconId, Long createTimeTicks) {
        this.RSSI = rssi;
        this.Tag = tag;
        this.Title = title;
        this.ImagePath = imagePath;
        this.AudioPath = audioPath;
        this.BeaconId = beaconId;
        this.CreateTimeTicks = createTimeTicks;

    }

    @Override
    public int compareTo(MyRect o) {
        return Math.abs(this.RSSI) - Math.abs(o.RSSI);
    }
}
