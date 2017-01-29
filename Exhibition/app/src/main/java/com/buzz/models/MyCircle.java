package com.buzz.models;

import android.content.Context;

import com.buzz.activity.MyApplication;
import com.buzz.activity.R;

/**
 * Created by NickChung on 5/12/15.
 */
public class MyCircle implements Comparable<MyCircle> {
    public int ColorRes;
    public int Size;
    protected int Level;
    public String Tag;
    public String Title;
    public String ImagePath;
    public String BeaconId;
    MyApplication myApp;

    public MyCircle(int colorRes, String tag, String title, String imagePath, String beaconId, Context context) {
        this.ColorRes = colorRes;

        myApp = (MyApplication) context.getApplicationContext();
        switch (colorRes) {
            case R.color.level01:
                this.Level = 1;
                this.Size = (int) (230 * myApp.scaleVector);
                this.Tag = tag;
                this.Title = title;
                this.ImagePath = imagePath;
                this.BeaconId = beaconId;
                break;

            case R.color.level02:
                this.Level = 2;
                this.Size = (int) (200 * myApp.scaleVector);
                this.Tag = tag;
                this.Title = title;
                this.ImagePath = imagePath;
                this.BeaconId = beaconId;
                break;

            case R.color.level03:
                this.Level = 3;
                this.Size = (int) (180 * myApp.scaleVector);
                this.Tag = tag;
                this.Title = title;
                this.ImagePath = imagePath;
                this.BeaconId = beaconId;
                break;

            case R.color.level04:
                this.Level = 4;
                this.Size = (int) (100 * myApp.scaleVector);
                this.Tag = tag;
                this.Title = title;
                this.ImagePath = imagePath;
                this.BeaconId = beaconId;
                break;
        }
    }

    @Override
    public String toString() {
        return "MyCircle{" +
                "ColorRes=" + ColorRes +
                ", Size=" + Size +
                ", Level=" + Level +
                ", Tag='" + Tag + '\'' +
                ", Title='" + Title + '\'' +
                ", ImagePath='" + ImagePath + '\'' +
                ", BeaconId='" + BeaconId + '\'' +
                ", myApp=" + myApp +
                '}';
    }

    @Override
    public int compareTo(MyCircle o) {
        return this.Level - o.Level;
    }
}
