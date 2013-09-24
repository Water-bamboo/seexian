package com.comic.seexian.detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.comic.seexian.R;
import com.comic.seexian.image.PhotoView;

public class AroundGridAdapter extends BaseAdapter {

	static class ViewHolder {
		PhotoView icon;
	}

	private Context mContext;

	public AroundGridAdapter(Context context) {
		mContext = context;
	}

	@Override
	public int getCount() {
		return 25;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext.getApplicationContext())
					.inflate(R.layout.around_grid_item, null);
			holder = new ViewHolder();
			holder.icon = (PhotoView) convertView
					.findViewById(R.id.around_image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.icon.setImageResource(R.drawable.ic_launcher);
		return convertView;
	}

}
