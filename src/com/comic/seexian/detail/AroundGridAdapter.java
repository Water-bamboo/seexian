package com.comic.seexian.detail;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.comic.seexian.Loge;
import com.comic.seexian.R;
import com.comic.seexian.image.PhotoView;

public class AroundGridAdapter extends BaseAdapter {

	private static final int[] colorList = { 0xFF31c2d4, 0xFF554087, 0xFFae4ea5 };

	private ArrayList<AroundData> aroundListData = new ArrayList<AroundData>();

	static class ViewHolder {
		PhotoView icon;
	}

	private Context mContext;

	public AroundGridAdapter(Context context) {
		mContext = context;
	}

	@Override
	public int getCount() {
		return aroundListData.size();
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

		int size = mContext.getResources().getDimensionPixelSize(
				R.dimen.grid_item_height);

		AroundData itemData = aroundListData.get(position);

		TextView tempTextView = new TextView(mContext);
		tempTextView.setHeight(size);
		tempTextView.setWidth(size);
		tempTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		tempTextView.setText(itemData.mName);
		tempTextView.setDrawingCacheEnabled(true);
		tempTextView.measure(size, size);
		tempTextView.layout(0, 0, size, size);
		tempTextView.setTextColor(Color.WHITE);
		tempTextView.setGravity(Gravity.CENTER);
		tempTextView.setMaxLines(2);

		int num = (int) (Math.round(Math.random() * (colorList.length - 1)));
		if (num < colorList.length) {
			tempTextView.setBackgroundColor(colorList[num]);
		} else {
			tempTextView.setBackgroundColor(0xFF81d8d0);
		}

		Bitmap bm = tempTextView.getDrawingCache();
		bm = Bitmap.createBitmap(bm, 0, 0, size, size);
		tempTextView.setDrawingCacheEnabled(false);

		holder.icon.setImageBitmap(bm);
		return convertView;
	}

	void setListData(ArrayList<AroundData> listData) {
		aroundListData.clear();
		aroundListData.addAll(listData);
	}

}
