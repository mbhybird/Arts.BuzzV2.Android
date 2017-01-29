package com.buzz.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.buzz.layout.ListViewCompat;
import com.buzz.layout.MessageBean;

import java.util.ArrayList;



@Deprecated
public class EventsListingActivity extends Activity implements OnItemClickListener,OnClickListener {

	private static final String TAG = "MainActivity";
	private ListViewCompat mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eventslist);
		initView();
	}

	private void initView() {
		mListView = (ListViewCompat) findViewById(R.id.event_list);
		EventsListingAdapter mAdapter =new EventsListingAdapter(this);
		ArrayList<MessageBean> mMessageList = new ArrayList<MessageBean>();
		for (int i = 0; i < 5; i++) {
			MessageBean item = new MessageBean();
            item.iconRes=R.drawable.icon_blue_normal;
            item.msg=getString(R.string.lb_item);

            mMessageList.add(item);

		}
		mAdapter.setmMessageItems(mMessageList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.e(TAG, "onItemClick position=" + position);
	}

	@Override
	public void onClick(View v) {

	}
    public void activity_back(View v) {     // 返回页面
       finish();
    }
}
