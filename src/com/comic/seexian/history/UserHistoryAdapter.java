package com.comic.seexian.history;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.comic.seexian.Loge;
import com.comic.seexian.R;
import com.comic.seexian.database.SeeXianProvider;
import com.comic.seexian.image.PhotoView;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class UserHistoryAdapter extends CursorAdapter {

	static class ViewHolder {
		PhotoView icon;
		TextView text;
	}

	private Context mContext;

	private Cursor mCursor;

	UserHistoryAdapter(Context ctx, Cursor cursor) {
		super(ctx, cursor, 0);
		mContext = ctx;
		mCursor = cursor;
	}

	void setCursor(Cursor cursor) {
		setCursor(cursor);
	}

	@Override
	public Cursor swapCursor(Cursor newCursor) {
		mCursor = newCursor;
		return super.swapCursor(newCursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		Loge.d("Create new view");

		final View itemLayout = LayoutInflater.from(
				mContext.getApplicationContext()).inflate(
				R.layout.history_list_item, null);
		final ViewHolder holder = new ViewHolder();

		holder.icon = (PhotoView) itemLayout
				.findViewById(R.id.histroy_ori_image);
		holder.text = (TextView) itemLayout
				.findViewById(R.id.histroy_time_text);

		itemLayout.setTag(holder);

		return itemLayout;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Loge.d("Bind view");

		if (cursor != null) {

			final ViewHolder holder = (ViewHolder) view.getTag();

			UserHistoryData data = new UserHistoryData();

			data.mOriPic = cursor.getString(4);
			data.mThumbPic = cursor.getString(5);
			data.mTime = cursor.getString(6);

			holder.text.setText(data.mTime);

			if (data.mOriPic == null) {
				return;
			}

			// Handles invalid URLs
			try {
				// Converts the URL string to a valid URL
				URL localURL = new URL(data.mOriPic);
				/*
				 * setImageURL(url,false,null) attempts to download and decode
				 * the picture at at "url" without caching and without providing
				 * a Drawable. The result will be a BitMap stored in the
				 * PhotoView for this Fragment.
				 */
				holder.icon.setImageURL(localURL, true, null);

				// Catches an invalid URL format
			} catch (MalformedURLException localMalformedURLException) {
				localMalformedURLException.printStackTrace();
			}
		}
	}

	@Override
	public int getCount() {
		if (getCursor() == null) {
			return 0;
		}
		int count = getCursor().getCount();
		return count;
	}

	@Override
	public Object getItem(int position) {
		if (mCursor != null) {
			mCursor.moveToPosition(position);
			return mCursor;
		} else {
			return null;
		}
	}
}
