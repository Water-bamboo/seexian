package com.comic.seexian.detail;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.comic.seexian.R;

public class AroundDetailAdapter extends BaseAdapter {

	private ArrayList<AroundPairData> mListData = new ArrayList<AroundPairData>();

	static class ViewHolder {
		TextView title;
		TextView text;
	}

	private Context mContext;

	public AroundDetailAdapter(Context context) {
		mContext = context;
	}

	@Override
	public int getCount() {
		return mListData.size();
	}

	@Override
	public Object getItem(int position) {
		if (position < mListData.size()) {
			return mListData.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext.getApplicationContext())
					.inflate(R.layout.around_detail_list_item, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView
					.findViewById(R.id.around_detail_title);
			holder.text = (TextView) convertView
					.findViewById(R.id.around_detail_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		AroundPairData item = mListData.get(position);

		StringBuilder itemSB = new StringBuilder();

		switch (item.type) {
		case R.string.name:
			holder.title.setVisibility(View.GONE);
			holder.text.setText(item.info);
			holder.text.setTextSize(25);
			break;
		case R.string.link:
			holder.title.setVisibility(View.GONE);
			holder.text.setText(item.info);
			holder.text.setTextColor(Color.BLUE);
			holder.text.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
			break;
		default:
			itemSB.append(mContext.getResources().getString(item.type));
			itemSB.append(":");
			holder.title.setVisibility(View.VISIBLE);
			holder.title.setText(itemSB.toString());
			holder.text.setText(item.info);
			break;
		}

		itemSB = null;

		return convertView;
	}

	void setListData(ArrayList<AroundPairData> listData) {
		mListData.clear();
		mListData.addAll(listData);
	}

}
