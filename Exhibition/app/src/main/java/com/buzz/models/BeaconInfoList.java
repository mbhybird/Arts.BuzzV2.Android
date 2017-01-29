package com.buzz.models;
import java.util.List;

/**
 * Created by NickChung on 3/17/15.
 * @deprecated
 */
public class BeaconInfoList {
    private List<beacon> beacons ;

    private List<action> actions ;

    public void setBeacon(List<beacon> beacons){
        this.beacons = beacons;
    }

    public List<beacon> getBeacon(){
        return this.beacons;
    }

    public void setAction(List<action> actions){
        this.actions = actions;
    }

    public List<action> getAction(){
        return this.actions;
    }

}