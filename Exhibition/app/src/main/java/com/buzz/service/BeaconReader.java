package com.buzz.service;

import java.io.Serializable;

/**
 * Created by NickChung on 7/20/15.
 */
public class BeaconReader implements Serializable {
    private String UUID;
    private String BeaconId;
    private String Major;
    private String Minor;
    private int Rssi;
    private int Power;
    private double Distance;
    private String BeaconType;

    public String getBeaconType() {
        return BeaconType;
    }

    public void setBeaconType(String beaconType) {
        BeaconType = beaconType;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getMajor() {
        return Major;
    }

    public void setMajor(String major) {
        Major = major;
    }

    public String getMinor() {
        return Minor;
    }

    public void setMinor(String minor) {
        Minor = minor;
    }

    public int getRssi() {
        return Rssi;
    }

    public void setRssi(int rssi) {
        Rssi = rssi;
    }

    public int getPower() {
        return Power;
    }

    public void setPower(int power) {
        Power = power;
    }

    public double getDistance() {
        return Distance;
    }

    public void setDistance(double distance) {
        Distance = distance;
    }

    public String getBeaconId() {
        return BeaconId;
    }

    public void setBeaconId(String beaconId) {
        BeaconId = beaconId;
    }

    @Override
    public String toString() {
        return "BeaconReader{" +
                "UUID='" + UUID + '\'' +
                ", BeaconId='" + BeaconId + '\'' +
                ", Major='" + Major + '\'' +
                ", Minor='" + Minor + '\'' +
                ", Rssi=" + Rssi +
                ", Power=" + Power +
                ", Distance=" + Distance +
                ", BeaconType='" + BeaconType + '\'' +
                '}';
    }
}
