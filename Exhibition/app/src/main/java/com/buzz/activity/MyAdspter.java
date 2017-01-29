package com.buzz.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.buzz.models.download;

import java.util.List;
import java.util.Map;


/**
 * Created by buzz on 2015/5/7.
 */
public class MyAdspter extends BaseAdapter {

    private List<Map<String, Object>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public MyAdspter(Context context, List<Map<String, Object>> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    /**
     * 获得某一位置的数据
     */
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    /**
     * 获得唯一标识
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListWidget ListWidget = null;
        if (convertView == null) {
            ListWidget = new ListWidget();
            //获得组件，实例化组件
            convertView = layoutInflater.inflate(R.layout.right_menu_list_item, null);
            ListWidget.text = (TextView) convertView.findViewById(R.id.right_menu_list_item_txtView);
            ListWidget.image = (ImageView) convertView.findViewById(R.id.right_menu_list_item_imgView);
            String mode = data.get(position).get("mode").toString();
            String type = "";
            if(data.get(position).containsKey("type")) {
                type = data.get(position).get("type").toString();
            }
            if (mode.equals("f")) {
                ListWidget.image.setBackgroundResource(R.drawable.love);
            } else if (mode.equals("d")) {
                download d = (download) data.get(position).get("download");
                if (!d.isFinished()) {
                    ListWidget.image.setBackgroundResource(R.drawable.icon_download);
                } else {
                    ListWidget.image.setBackgroundResource(R.drawable.preview);
                }
            } else if (mode.equals("p")) {
                if(type.equals("wechat")) {
                    ListWidget.image.setBackgroundResource(R.drawable.wechat);
                }
                else if(type.equals("facebook")) {
                    ListWidget.image.setBackgroundResource(R.drawable.facebook);
                }
                if (data.get(position).get("link").equals(Boolean.TRUE)) {
                    ListWidget.text.setTextColor(Color.WHITE);
                } else {
                    ListWidget.text.setTextColor(context.getResources().getColor(R.color.gray_color));
                }
            }
            convertView.setTag(ListWidget);
        } else {
            ListWidget = (ListWidget) convertView.getTag();
        }
        convertView.setBackgroundResource(R.drawable.list_menu_item_bg);
        //绑定数据
        ListWidget.text.setText((String) data.get(position).get("title"));
        return convertView;
    }

    /**
     * 组件集合，对应list.xml中的控件
     *
     * @author Administrator
     */
    public final class ListWidget {
        public ImageView image;
        public TextView text;
    }

}

