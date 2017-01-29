package com.buzz.activity;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.buzz.utils.ImageHelper;

import java.util.List;
import java.util.Map;


/**
 * Created by buzz on 2016/8/1.
 */
public class BeaconListAdapter extends BaseAdapter {

    private List<Map<String, String>> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public BeaconListAdapter(Context context, List<Map<String, String>> data) {
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
            convertView = layoutInflater.inflate(R.layout.beacon_list_item, null);
        } else {
            ListWidget = (ListWidget) convertView.getTag();
        }

        ListWidget.tvTag = (TextView) convertView.findViewById(R.id.beacon_list_item_tvTag);
        ListWidget.layout = (FrameLayout) convertView.findViewById(R.id.beacon_list_item_layout);
        ListWidget.tvTitle = (TextView) convertView.findViewById(R.id.beacon_list_item_tvTitle);
        ListWidget.image = (ImageView) convertView.findViewById(R.id.beacon_list_item_imgView);
        Drawable drawBackground = Drawable.createFromPath(data.get(position).get("imagePath").toString());
        if (drawBackground != null) {
            convertView.setVisibility(View.VISIBLE);
            //ListWidget.image.setBackground(drawBackground);
            try {
                ListWidget.image.setBackground(
                        new BitmapDrawable(
                                ImageHelper.cropCenterImage(
                                        data.get(position).get("imagePath").toString(), 320, 160)));
            }catch (Exception e){
                e.printStackTrace();
                ListWidget.image.setBackground(drawBackground);
            }
        } else {
            convertView.setVisibility(View.INVISIBLE);
        }
        convertView.setTag(ListWidget);

        //绑定数据
        ListWidget.tvTag.setText((String) data.get(position).get("tag"));
        ListWidget.tvTitle.setText((String) data.get(position).get("title"));
        ListWidget.tvTitle.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/futura.ttf"));

        //设置背景色
        if (position % 2 == 0) {
            ListWidget.layout.setBackgroundColor(context.getResources().getColor(R.color.odd_row_color));
        } else {
            ListWidget.layout.setBackgroundColor(context.getResources().getColor(R.color.even_row_color));
        }
        return convertView;
    }

    /**
     * 组件集合，对应list.xml中的控件
     *
     * @author Administrator
     */
    public final class ListWidget {
        public ImageView image;
        public TextView tvTag;
        public TextView tvTitle;
        public FrameLayout layout;
    }
}

