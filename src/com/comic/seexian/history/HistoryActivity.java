package com.comic.seexian.history;

import java.util.ArrayList;

import com.comic.seexian.Loge;
import com.comic.seexian.R;
import com.comic.seexian.database.SeeXianProvider;
import com.comic.seexian.detail.DetailActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class HistoryActivity extends Activity implements OnItemClickListener {

	private GridView mGridView;

	private HistoryGridAdapter mGridAdapter;

	private TextView mEmptyText;

	private ContentResolver mContentResolver;

	private ArrayList<HistoryData> mListData = new ArrayList<HistoryData>();

	private int mScreenHeight;
	private int mSreenWidth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContentResolver = this.getContentResolver();

		setContentView(R.layout.history_layout);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mScreenHeight = dm.heightPixels;
		mSreenWidth = dm.widthPixels;
		Loge.d("ScreenHeight = " + mScreenHeight);
		Loge.d("SreenWidth = " + mSreenWidth);

		mGridView = (GridView) findViewById(R.id.history_grid);
		mGridView.setOnItemClickListener(this);

		mGridAdapter = new HistoryGridAdapter(this, null);
		mGridView.setAdapter(mGridAdapter);

		mEmptyText = (TextView) findViewById(R.id.empty_text);
		mGridView.setEmptyView(mEmptyText);

		new GetDataTask()
				.execute(SeeXianProvider.CONTENT_URI_SEE_XIAN_LANDSCAPE);

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Loge.d("Item postion = " + arg2);
		Cursor cursor = (Cursor) mGridAdapter.getItem(arg2);

		HistoryData itemData = new HistoryData();

		itemData.mName = cursor.getString(1);
		itemData.mDiscription = cursor.getString(2);
		itemData.mDetail = cursor.getString(3);
		itemData.mIconUrl = cursor.getString(4);
		itemData.mImageUrl = cursor.getString(5);
		itemData.mLinkUrl = cursor.getString(6);
		itemData.mTime = cursor.getString(7);

		Loge.d("Item name = " + itemData.mName);
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		Bundle extras = new Bundle();
		extras.putString(HistoryData.KEY_NAME, itemData.mName);
		extras.putString(HistoryData.KEY_DESCRIPTION, itemData.mDiscription);
		extras.putString(HistoryData.KEY_DETAIL, itemData.mDetail);
		extras.putString(HistoryData.KEY_ICON, itemData.mIconUrl);
		extras.putString(HistoryData.KEY_IMAGE, itemData.mImageUrl);
		extras.putString(HistoryData.KEY_LINK_URL, itemData.mLinkUrl);
		extras.putString(HistoryData.KEY_TIME, itemData.mTime);

		intent.putExtras(extras);
		intent.setClass(HistoryActivity.this, DetailActivity.class);
		startActivity(intent);

	}

	private class GetDataTask extends AsyncTask<Uri, Void, Cursor> {
		Uri mUri;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mEmptyText.setText(R.string.loading);
		}

		@Override
		protected Cursor doInBackground(Uri... urls) {
			if (mContentResolver == null) {
				return null;
			}

			mUri = urls[0];

			Loge.d("start get infos from database URI = ", mUri.toString());

			mListData.clear();
			mContentResolver.delete(mUri, null, null);

			ContentValues values = new ContentValues();
			values.put(
					SeeXianProvider.KEY_ICON,
					"http://a.hiphotos.baidu.com/baike/c%3DbaikeA1%2C10%2C95/sign=7daf9e4149540923be69342ffb33b448/f7246b600c3387441be64f1e510fd9f9d62a6059252d888d.jpg");
			values.put(SeeXianProvider.KEY_NAME, "西安钟楼");
			values.put(
					SeeXianProvider.KEY_DETAIL,
					"\r\r\r\r西安钟楼建成于明朝洪武十七年（1384年）。它最初位于西大街以北广济街口的迎祥观，与西安鼓楼对峙，距目前所在位置约1000米。当时此地与南北城门正对，是城中心之所在。\n\r\r\r\r随着明朝初期长安城的扩建，城市中心逐渐东移。\n\r\r\r\r过了两个世纪后，城门改建，新的东、南、西、北四条大街形成，位于迎祥观的钟楼便日益显得偏离城市中心。万历十年（1582年），在陕西监察御史龚懋贤的主持下，咸宁、长安二县县令奉命将其迁建于现址。移建工程除重新建造基座外，本质结构的楼体全是原件，所以耗资不多，工程迅速。\n\r\r\r\r但600多年前完成如此庞大建筑的整体迁移，在世界建筑史上十分罕见。");
			values.put(SeeXianProvider.KEY_LINK_URL,
					"http://baike.baidu.com/view/43575.htm");
			mContentResolver.insert(mUri, values);

			ContentValues values1 = new ContentValues();
			values1.put(
					SeeXianProvider.KEY_ICON,
					"http://g.hiphotos.baidu.com/baike/c%3DbaikeA2%2C10%2C95/sign=add597950823dd543573f039b862d69f/f7246b600c3387448d3ec48d500fd9f9d62a6059242d8874.jpg");
			values1.put(SeeXianProvider.KEY_NAME, "大雁塔");
			mContentResolver.insert(mUri, values1);

			ContentValues values2 = new ContentValues();
			values2.put(
					SeeXianProvider.KEY_ICON,
					"http://c.hiphotos.baidu.com/baike/c%3DbaikeA1%2C10%2C95/sign=13adf7f86709c93d13f259a6f6569d9d/b3119313b07eca80bf828411912397dda044ad345882ab7d.jpg");
			values2.put(SeeXianProvider.KEY_NAME, "西安博物院");
			mContentResolver.insert(mUri, values2);

			Cursor cursor = mContentResolver
					.query(mUri, null, null, null, null);
			return cursor;

		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(Cursor result) {
			if (result != null && mGridAdapter != null) {
				Loge.d("get cursor success count = " + result.getCount());
				if (result.getCount() == 0) {
					mEmptyText.setText(R.string.empty_text);
				}
				result.moveToFirst();
				mGridAdapter.swapCursor(result);
			} else {
				mEmptyText.setText(R.string.empty_text);
			}
		}
	}

}
