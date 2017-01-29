package com.buzz.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.buzz.layout.MessageBean;
import com.buzz.layout.SlideViewLayout;

import java.util.ArrayList;
import java.util.List;


@Deprecated
public class EventsListingAdapter extends BaseAdapter implements SlideViewLayout.OnSlideListener {
	private static final String TAG = "SlideAdapter";

	private Context mContext;
	private LayoutInflater mInflater;

	private List<MessageBean> mMessageItems = new ArrayList<MessageBean>();
	private SlideViewLayout mLastSlideViewLayoutWithStatusOn;

	EventsListingAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}

	public void setmMessageItems(List<MessageBean> mMessageItems) {
		this.mMessageItems = mMessageItems;
	}

	@Override
	public int getCount() {
		if (mMessageItems == null) {
			mMessageItems = new ArrayList<MessageBean>();
		}
		return mMessageItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mMessageItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		SlideViewLayout slideViewLayout = (SlideViewLayout) convertView;
		if (slideViewLayout == null) {
			View itemView = mInflater.inflate(R.layout.activity_eventslistng_privatelisting_item,
					null);

			slideViewLayout = new SlideViewLayout(mContext);
			slideViewLayout.setContentView(itemView);

			holder = new ViewHolder(slideViewLayout);
			slideViewLayout.setOnSlideListener(this);
			slideViewLayout.setTag(holder);
		} else {
			holder = (ViewHolder) slideViewLayout.getTag();
		}
		MessageBean item = mMessageItems.get(position);
		item.slideViewLayout = slideViewLayout;
		item.slideViewLayout.shrink();

		holder.icon.setImageResource(item.iconRes);

		holder.msg.setText(item.msg);

		holder.deleteHolder.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMessageItems.remove(position);
				notifyDataSetChanged();
			}
		});

		return slideViewLayout;
	}

	@Override
	public void onSlide(View view, int status) {
		if (mLastSlideViewLayoutWithStatusOn != null
				&& mLastSlideViewLayoutWithStatusOn != view) {
			mLastSlideViewLayoutWithStatusOn.shrink();
		}

		if (status == SLIDE_STATUS_ON) {
			mLastSlideViewLayoutWithStatusOn = (SlideViewLayout) view;
		}
	}

	private static class ViewHolder {
		public ImageView icon;
//		public TextView title;
		public TextView msg;
//		public TextView time;
		public ViewGroup deleteHolder;

		ViewHolder(View view) {
			icon = (ImageView) view.findViewById(R.id.event_list_icon);

			msg = (TextView) view.findViewById(R.id.event_list_msg);

			deleteHolder = (ViewGroup) view.findViewById(R.id.event_list_holder);
		}
	}
}
