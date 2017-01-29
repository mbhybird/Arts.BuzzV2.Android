package com.buzz.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CoreService extends Service implements BeaconConsumer {

    final static String TAG = CoreService.class.getSimpleName();
    public final static String ACTION_BEACON_SEND = "android.intent.action.MY_APP_BEACON_SEND";
    final static String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    final static Map<String, String> FILTER_UUID_LIST = new HashMap<String, String>();
    Map<String, List<Integer>> dic;
    Map<String, BeaconReader> beaconReaderList;
    private BeaconManager beaconManager;
    final static int RSSI_SMOOTH_STEP = 1;
    int total;

    public CoreService() {
        //FILTER_UUID_LIST.put("B9407F30-F5F8-466E-AFF9-25556B57FE6D".toLowerCase(), "estimote");
        //FILTER_UUID_LIST.put("DD3D9DE3-1AD5-45C2-AF35-CA9A7B92A4ED".toLowerCase(), "estimote");
        FILTER_UUID_LIST.put("23A01AF0-232A-4518-9C0E-323FB773F5EF".toLowerCase(), "sensoro");
        //FILTER_UUID_LIST.put("E2C56DB5-DFFB-48D2-B060-D0F5A71096E0".toLowerCase(), "wristband");
        //FILTER_UUID_LIST.put("FDA50693-A4E2-4FB1-AFCF-C6EB07647825".toLowerCase(), "wechat");

        dic = new HashMap<String, List<Integer>>();
        beaconReaderList = new HashMap<String, BeaconReader>();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));
        beaconManager.setForegroundScanPeriod(3000);
        beaconManager.setBackgroundScanPeriod(3000);
        beaconManager.bind(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "EnterRegion");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "ExitRegion");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "DetermineStateForRegion:" + state);
            }

        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                final StringBuilder sb = new StringBuilder();
                beaconReaderList.clear();
                for (Beacon b : beacons) {
                    BeaconReader beaconReader = null;
                    if (FILTER_UUID_LIST.containsKey(b.getId1().toString())) {
                        beaconReader = new BeaconReader();
                        beaconReader.setBeaconType(FILTER_UUID_LIST.get(b.getId1().toString()));
                        beaconReader.setDistance(b.getDistance());
                        beaconReader.setMajor(b.getId2().toString());
                        beaconReader.setMinor(b.getId3().toString());
                        beaconReader.setPower(b.getTxPower());
                        beaconReader.setUUID(b.getId1().toString());
                        beaconReader.setRssi(b.getRssi());
                        beaconReader.setBeaconId(String.format("%s-%s", b.getId2(), b.getId3()));

                        beaconReaderList.put(beaconReader.getBeaconId(), beaconReader);

                        sb.append(beaconReader.toString() + "\r\n");
                        //Log.i(TAG, sb.toString());
                    }

                    if (beaconReader != null) {
                        if (!dic.containsKey(beaconReader.getBeaconId())) {
                            List<Integer> rawList = new ArrayList<Integer>();
                            rawList.add(beaconReader.getRssi());
                            dic.put(beaconReader.getBeaconId(), rawList);
                        } else {
                            List<Integer> rawList = dic.get(beaconReader.getBeaconId());
                            rawList.add(beaconReader.getRssi());
                        }
                    }
                }

                Map<String, BeaconReader> seqMap = new HashMap<String, BeaconReader>();
                for (Map.Entry<String, List<Integer>> entry : dic.entrySet()) {
                    total = 0;
                    List<Integer> rawList = entry.getValue();

                    if (rawList.size() > RSSI_SMOOTH_STEP) {
                        List<Integer> rawSubList = rawList.subList(rawList.size() - (RSSI_SMOOTH_STEP + 1), rawList.size() - 1);
                        for (int r : rawSubList) {
                            total += r;
                        }

                        int avgRssi = (int) (total / RSSI_SMOOTH_STEP);
                        BeaconReader beaconReader = beaconReaderList.get(entry.getKey());
                        if (beaconReader != null) {
                            beaconReader.setRssi(avgRssi);
                            seqMap.put(entry.getKey(), beaconReader);
                        }
                    }
                }

                try {
                    Intent it = new Intent(ACTION_BEACON_SEND);
                    it.putExtra("map", (Serializable) seqMap);
                    if (seqMap.size() > 0) {
                        Log.i(TAG,String.valueOf(seqMap.size()));
                        sendBroadcast(it);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            //开始监视
            beaconManager.startMonitoringBeaconsInRegion(new Region("", null, null, null));
            //开始检测
            beaconManager.startRangingBeaconsInRegion(new Region("", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
