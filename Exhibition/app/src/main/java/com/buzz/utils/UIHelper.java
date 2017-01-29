package com.buzz.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.buzz.activity.MainActivity;
import com.buzz.activity.MyApplication;

/**
 * Created by NickChung on 3/17/15.
 */
public class UIHelper {

    public static void showDialog(Context context, String msg) {
        new AlertDialog
                .Builder(context)
                .setTitle("提示")
                .setMessage(msg)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    public static void showDialog(Context context, View view) {
        new AlertDialog
                .Builder(context)
                .setTitle("展品信息")
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
}
