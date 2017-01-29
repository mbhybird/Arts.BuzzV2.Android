package com.buzz.models;

import java.util.List;

/**
 * Created by NickChung on 7/29/15.
 */
public class excontent {
    private List<beacon> beacons;
    private List<content> contents;

    public List<beacon> getBeacons() {
        return beacons;
    }

    public void setBeacons(List<beacon> beacons) {
        this.beacons = beacons;
    }

    public List<content> getContents() {
        return contents;
    }

    public void setContents(List<content> contents) {
        this.contents = contents;
    }
}