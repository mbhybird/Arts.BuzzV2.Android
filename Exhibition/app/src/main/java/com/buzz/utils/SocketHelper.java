package com.buzz.utils;

import android.content.Intent;
import android.os.*;
import android.util.JsonReader;
import android.util.Log;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

import java.io.*;
import java.net.*;

import com.buzz.activity.MyApplication;
import com.buzz.models.*;

/**
 * Created by NickChung on 3/17/15.
 */
public class SocketHelper {

    final String TAG = this.getClass().getSimpleName();
    Socket socket;
    Thread socketMsgThread;
    Handler inHandler;
    InputStream in;
    ObjectMapper objectMapper;
    String cmdType;
    FileHelper fileHelper;

    public SocketHelper(final Socket socket, final MyApplication myApp) {
        this.socket = socket;
        objectMapper = new ObjectMapper();
        fileHelper = new FileHelper();

        inHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        switch (cmdType) {
                            //get beaconinfo list
                            case "00":
                                try {
                                    //BeaconInfoList beaconInfoList = objectMapper.readValue(msg.obj.toString(), BeaconInfoList.class);
                                    /*
                                    Message message = new Message();
                                    message.what = 0;
                                    message.obj = beaconInfoList;
                                    resultHandler.sendMessage(message);*/
                                    fileHelper.writeToSDFromJson(GlobalConst.PATH_SAVE, GlobalConst.FILENAME_BEACON_INFO_LIST, msg.obj.toString());
                                } catch (Exception e) {
                                    return;
                                }
                                break;

                            //single beacon
                            case "01":
                                try {
                                    BeaconInfo beaconInfo = objectMapper.readValue(msg.obj.toString(), BeaconInfo.class);
                                    /*
                                    Message message = new Message();
                                    message.what = 1;
                                    message.obj = beaconInfo;
                                    resultHandler.sendMessage(message);*/
                                    //fileHelper.writeToSDFromJson(GlobalConst.PATH_SAVE, GlobalConst.FILENAME_BEACON_INFO, msg.obj.toString());
                                } catch (IOException ex) {
                                    return;
                                }
                                break;

                            //get sysparams
                            case "02":
                                try {
                                    Sysparams[] SysparamsList = objectMapper.readValue(msg.obj.toString(), Sysparams[].class);
                                    /*
                                    Message message = new Message();
                                    message.what = 2;
                                    message.obj = SysparamsList;
                                    resultHandler.sendMessage(message);*/
                                    //fileHelper.writeToSDFromJson(GlobalConst.PATH_SAVE, GlobalConst.FILENAME_SYSTEM_PARAMS, msg.obj.toString());
                                } catch (IOException ex) {
                                    return;
                                }
                                break;

                            //get user
                            case "03":
                                try {
                                    UserInfo[] userInfoList = objectMapper.readValue(msg.obj.toString(), UserInfo[].class);
                                    /*
                                    Message message = new Message();
                                    message.what = 3;
                                    message.obj = userInfoList;
                                    resultHandler.sendMessage(message);*/
                                    Intent it = new Intent(GlobalConst.ACTION_USER_LOGIN);
                                    if (userInfoList.length > 0) {
                                        myApp.logonUser.isLogon = true;
                                        myApp.sendBroadcast(it);
                                        if (!fileHelper.isFileExist(GlobalConst.PATH_SAVE + GlobalConst.FILENAME_USER_INFO)) {
                                            fileHelper.writeToSDFromJson(GlobalConst.PATH_SAVE, GlobalConst.FILENAME_USER_INFO, msg.obj.toString());
                                        }
                                    } else {
                                        myApp.logonUser.isLogon = false;
                                        myApp.sendBroadcast(it);
                                    }
                                } catch (IOException ex) {
                                    return;
                                }
                                break;

                            //add user
                            case "05":
                                try {
                                    JSONObject json = new JSONObject(msg.obj.toString());
                                    Intent it = new Intent(GlobalConst.ACTION_USER_SIGN_UP);
                                    it.putExtra("result", json.getString("affectedRows"));
                                    myApp.sendBroadcast(it);
                                }
                                catch (Exception e) {

                                }

                                break;

                            //find user
                            case "06":
                                try {
                                    JSONObject json = new JSONObject(msg.obj.toString());
                                    Intent it = new Intent(GlobalConst.ACTION_USER_FIND);
                                    it.putExtra("result", json.getString("count"));
                                    myApp.sendBroadcast(it);
                                }
                                catch (Exception e) {

                                }
                                break;

                            //get extag list
                            case "07":
                                try {
                                    //extag[] extagList = objectMapper.readValue(msg.obj.toString(), extag[].class);
                                    fileHelper.writeToSDFromJson(GlobalConst.PATH_SAVE, GlobalConst.FILENAME_EXTAG_LIST, msg.obj.toString());
                                }catch (Exception ex) {
                                    return;
                                }
                                break;

                            default:
                                /*
                                Message message = new Message();
                                message.what = 4;
                                resultHandler.sendMessage(message);*/
                                break;
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        try {
            in = socket.getInputStream();
        } catch (Exception e) {
            System.out.print(e.toString());
            return;
        }
        socketMsgThread = new Thread(runnable);
        socketMsgThread.start();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String line = "";
            while (true) {
                try {
                    byte[] temp = new byte[1024 * 10];// 默认最大为10240
                    int size = 0;
                    while ((size = in.read(temp)) > 0) {
                        // -1表示文件结尾
                        byte[] res = new byte[size];// 默认最大为10240
                        System.arraycopy(temp, 0, res, 0, size);
                        /*
                        for (int i = 0; i < size; i++) {
                            line += (char) res[i];
                        }*/
                        line += new String(res, "utf-8");

                        if (cmdType == "00") {
                            if (line.endsWith("}]}")) {
                                Message message = new Message();
                                message.what = 1;
                                message.obj = line;
                                inHandler.sendMessage(message);
                                line = "";
                            }
                        } else if (cmdType == "07") {
                            if (line.endsWith("}]")) {
                                Message message = new Message();
                                message.what = 1;
                                message.obj = line;
                                inHandler.sendMessage(message);
                                line = "";
                            }
                        } else {
                            Message message = new Message();
                            message.what = 1;
                            message.obj = line;
                            inHandler.sendMessage(message);
                            line = "";
                        }
                    }
                } catch (Exception e) {
                    System.out.print(e.toString());
                    return;
                }
            }
        }
    };

    public void Send(String commandArgs) {
        cmdType = commandArgs.substring(0, 2);
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(commandArgs);
            writer.flush();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.print(e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print(e.toString());
        }
    }

}
