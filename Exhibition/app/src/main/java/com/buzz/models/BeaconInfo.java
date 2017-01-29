package com.buzz.models;
import java.util.List;

/**
 * Created by NickChung on 2/14/15.
 * @deprecated
 */

public class BeaconInfo {
    private List<beacon> beacon ;

    private List<triggers> triggers ;

    private List<contents> contents ;

    public void setBeacon(List<beacon> beacon){
        this.beacon = beacon;
    }

    public List<beacon> getBeacon() {
        return this.beacon;
    }

    public void setTriggers(List<triggers> triggers){
        this.triggers = triggers;
    }

    public List<triggers> getTriggers(){
        return this.triggers;
    }

    public void setContents(List<contents> contents){
        this.contents = contents;
    }

    public List<contents> getContents(){
        return this.contents;
    }

}

