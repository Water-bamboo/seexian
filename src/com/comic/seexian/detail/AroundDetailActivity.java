package com.comic.seexian.detail;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.comic.seexian.R;

public class AroundDetailActivity extends ListActivity {

	private AroundData mAroundData = new AroundData();

	private AroundDetailAdapter mAdapter;

	private ArrayList<AroundPairData> mAroundListPairData = new ArrayList<AroundPairData>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAdapter = new AroundDetailAdapter(this);
		setListAdapter(mAdapter);

		getDataFromExtra();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		AroundPairData itemData = (AroundPairData) mAdapter.getItem(position);
		if (itemData != null && itemData.type == R.string.link) {
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(itemData.url));
			startActivity(intent);
		}
		super.onListItemClick(l, v, position, id);
	}

	private void getDataFromExtra() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		mAroundData.mId = extras.getString(AroundData.KEY_ID);
		mAroundData.mName = extras.getString(AroundData.KEY_NAME);
		mAroundData.mPrice = extras.getString(AroundData.KEY_PRICE);
		mAroundData.mDescription = extras.getString(AroundData.KEY_DESCRIPTION);
		mAroundData.mIcon = extras.getString(AroundData.KEY_ICON);
		mAroundData.mTel = extras.getString(AroundData.KEY_TEL);
		mAroundData.mAddress = extras.getString(AroundData.KEY_ADDRESS);
		mAroundData.mLinkUrl = extras.getString(AroundData.KEY_LINK_URL);
		mAroundData.mProvider = extras.getString(AroundData.KEY_PROIVDER);
		mAroundData.mPostId = extras.getString(AroundData.KEY_POST_ID);

		new GenAroundListData().execute(null);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	class GenAroundListData extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			if (!checkNullorEmpty(mAroundData.mName)) {
				AroundPairData item = new AroundPairData();
				item.type = R.string.name;
				item.info = mAroundData.mName;
				mAroundListPairData.add(item);
			}

			if (!checkNullorEmpty(mAroundData.mPrice)) {
				AroundPairData item = new AroundPairData();
				item.type = R.string.price;
				item.info = mAroundData.mPrice;
				mAroundListPairData.add(item);
			}

			if (!checkNullorEmpty(mAroundData.mDescription)) {
				AroundPairData item = new AroundPairData();
				item.type = R.string.type;
				item.info = mAroundData.mDescription;
				mAroundListPairData.add(item);
			}

			if (!checkNullorEmpty(mAroundData.mTel)) {
				AroundPairData item = new AroundPairData();
				item.type = R.string.tel;
				item.info = mAroundData.mTel;
				mAroundListPairData.add(item);
			}

			if (!checkNullorEmpty(mAroundData.mAddress)) {
				AroundPairData item = new AroundPairData();
				item.type = R.string.addr;
				item.info = mAroundData.mAddress;
				mAroundListPairData.add(item);
			}

			if (!checkNullorEmpty(mAroundData.mLinkUrl)
					&& !checkNullorEmpty(mAroundData.mProvider)) {
				String[] urls = mAroundData.mLinkUrl.split("\0");
				String[] providers = mAroundData.mProvider.split("\0");

				if (urls.length == providers.length) {
					for (int i = 0; i < urls.length; i++) {
						AroundPairData pair = new AroundPairData();
						pair.type = R.string.link;
						pair.info = providers[i];
						pair.url = urls[i];
						mAroundListPairData.add(pair);
					}
				}

			}

			return "ok";
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				mAdapter.setListData(mAroundListPairData);
				mAdapter.notifyDataSetChanged();
			}
			super.onPostExecute(result);
		}

		boolean checkNullorEmpty(String s) {
			if (s == null || s.isEmpty() || s.equals("null"))
				return true;
			return false;
		}

	}
}
