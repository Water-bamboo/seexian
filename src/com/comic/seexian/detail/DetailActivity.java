package com.comic.seexian.detail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.comic.seexian.Constants;
import com.comic.seexian.Loge;
import com.comic.seexian.R;
import com.comic.seexian.database.SeeXianProvider;
import com.comic.seexian.history.UserHistoryData;
import com.comic.seexian.image.PhotoView;
import com.comic.seexian.sinaauth.AccessTokenKeeper;
import com.comic.seexian.sinaauth.UserInfo;
import com.comic.seexian.utils.SeeXianNetUtils;
import com.comic.seexian.utils.SeeXianUtils;

public class DetailActivity extends Activity implements OnClickListener {

	private static final String[] interestSpot = { "景点", "小吃", "住宿" };

	private static final int MESSAGE_GET_DATA_FINISHED = 102;

	private Context mCtx;

	private int mScreenHeight;
	private int mSreenWidth;

	private PhotoView landscapeImage = null;
	private PhotoView IconImage = null;
	private PhotoView mMapView = null;

	private TextView nameText = null;

	private View distancePanel;
	private TextView distanceText;

	private ImageButton linkButton = null;

	private UserHistoryData mUserHistoryData = new UserHistoryData();

	private UserInfo mUserInfo = null;

	private GridView mAroundGrid = null;
	private ImageButton mAroundRefreshButton = null;
	private AroundGridAdapter mAroundGridAdapter = null;
	private ProgressBar mAroundRefreshProgress = null;
	private View mAroundLoadingView = null;

	private ArrayList<AroundData> aroundListData = new ArrayList<AroundData>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCtx = this.getApplicationContext();

		setContentView(R.layout.detail_layout);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mScreenHeight = dm.heightPixels;
		mSreenWidth = dm.widthPixels;

		landscapeImage = (PhotoView) findViewById(R.id.landscape_pic);
		IconImage = (PhotoView) findViewById(R.id.icon_pic);
		mMapView = (PhotoView) findViewById(R.id.bmapsView);
		nameText = (TextView) findViewById(R.id.name_text);
		linkButton = (ImageButton) findViewById(R.id.link_button);

		distancePanel = (View) findViewById(R.id.distance_panel);
		distanceText = (TextView) findViewById(R.id.distance_text);

		mAroundLoadingView = (View) findViewById(R.id.around_refresh_view);
		mAroundGrid = (GridView) findViewById(R.id.detail_grid);
		mAroundRefreshButton = (ImageButton) findViewById(R.id.around_refresh_button);
		mAroundRefreshProgress = (ProgressBar) findViewById(R.id.around_refresh_progress);

		mAroundGridAdapter = new AroundGridAdapter(this);
		mAroundGrid.setAdapter(mAroundGridAdapter);

		int iconSize = mSreenWidth / 4;
		LayoutParams params1 = new LayoutParams(iconSize, iconSize);
		params1.setMargins(iconSize / 3, mScreenHeight / 3 - iconSize, 0, 0);
		IconImage.setLayoutParams(params1);

		LayoutParams params2 = new LayoutParams(LayoutParams.FILL_PARENT,
				mScreenHeight / 3 - iconSize / 2);
		landscapeImage.setLayoutParams(params2);

		LayoutParams params3 = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params3.setMargins(iconSize + iconSize / 3, mScreenHeight / 3
				- iconSize / 2, 0, 0);
		nameText.setLayoutParams(params3);

		int content_margin = this.getResources().getDimensionPixelSize(
				R.dimen.content_margin);

		android.widget.LinearLayout.LayoutParams params4 = new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
				mScreenHeight / 2);
		params4.setMargins(content_margin, content_margin, content_margin,
				content_margin);
		mMapView.setLayoutParams(params4);

		mAroundRefreshButton.setOnClickListener(refreshClicked);

		getDataFromExtra();
	}

	private View.OnClickListener refreshClicked = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

		}
	};

	private void getDataFromExtra() {
		Intent dataIntent = getIntent();
		Bundle extras = dataIntent.getExtras();

		mUserHistoryData.mPostId = extras
				.getString(UserHistoryData.KEY_USER_POST_ID);
		mUserHistoryData.mPosName = extras
				.getString(UserHistoryData.KEY_USER_POS_NAME);
		mUserHistoryData.mText = extras
				.getString(UserHistoryData.KEY_USER_TEXT);
		mUserHistoryData.mSource = extras
				.getString(UserHistoryData.KEY_USER_SOURCE);
		mUserHistoryData.mOriPic = extras
				.getString(UserHistoryData.KEY_USER_ORIGIN_PIC);
		mUserHistoryData.mThumbPic = extras
				.getString(UserHistoryData.KEY_USER_THUMB_PIC);
		mUserHistoryData.mTime = extras
				.getString(UserHistoryData.KEY_USER_TIME);
		mUserHistoryData.mLat = extras
				.getString(UserHistoryData.KEY_USER_LATITUDE);
		mUserHistoryData.mLng = extras
				.getString(UserHistoryData.KEY_USER_LONGITUDE);
		mUserHistoryData.mDistance = extras
				.getString(UserHistoryData.KEY_USER_DISATACE_TO_XIAN);
		mUserHistoryData.mLandscapeId = extras
				.getLong(UserHistoryData.KEY_USER_LANDSCAPE_ID);

		new GetDetailDataTask().execute(null);
	}

	private void setDetailData() {

		try {
			StringBuilder staticMap = new StringBuilder(
					"http://api.map.baidu.com/staticimage?");
			staticMap.append("center=");
			staticMap.append(mUserHistoryData.mLng);
			staticMap.append(",");
			staticMap.append(mUserHistoryData.mLat);
			staticMap.append("&width=");
			staticMap.append(mSreenWidth / 1.8);
			staticMap.append("&height=");
			staticMap.append(mSreenWidth / 1.8);
			staticMap.append("&zoom=17");
			staticMap.append("&markers=");
			staticMap.append(mUserHistoryData.mLng);
			staticMap.append(",");
			staticMap.append(mUserHistoryData.mLat);
			staticMap.append("&markerStyles=l,");
			staticMap.append("&scale=2");

			Loge.i("staticMap URL = " + staticMap.toString());

			URL localURL = new URL(staticMap.toString());
			mMapView.setImageURL(localURL, true, null);
		} catch (MalformedURLException localMalformedURLException) {
			localMalformedURLException.printStackTrace();
		}

		try {
			URL localURL = new URL(mUserHistoryData.mOriPic);
			landscapeImage.setImageURL(localURL, true, null);
		} catch (MalformedURLException localMalformedURLException) {
			localMalformedURLException.printStackTrace();
		}

		try {
			if (mUserInfo != null) {
				URL localURL = new URL(mUserInfo.mAvatar);
				IconImage.setImageURL(localURL, true, null);
			}
		} catch (MalformedURLException localMalformedURLException) {
			localMalformedURLException.printStackTrace();
		}

		linkButton.setOnClickListener(this);

		if (mUserHistoryData.mPosName != null
				&& !mUserHistoryData.mPosName.isEmpty()) {
			nameText.setText(mUserHistoryData.mPosName);
		} else {
			nameText.setText("...");
		}

		if (mUserHistoryData.mDistance == null
				|| mUserHistoryData.mDistance.isEmpty()) {
			distancePanel.setVisibility(View.GONE);
		} else {
			float iDistance = Float.valueOf(mUserHistoryData.mDistance);
			if (iDistance > Constants.MINI_DISTANCE) {
				StringBuilder distanseSB = new StringBuilder();
				distanseSB.append(this.getResources().getString(
						R.string.distance_to));
				distanseSB.append(this.getResources().getString(R.string.xian));
				distanseSB.append(": ");
				int distance = (int) iDistance / 1000;
				distanseSB.append(distance);
				distanseSB.append(this.getResources().getString(
						R.string.kilometer));

				distanceText.setText(distanseSB.toString());
				distancePanel.setVisibility(View.VISIBLE);
			} else {
				distancePanel.setVisibility(View.GONE);
			}
		}

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

	public Bitmap getBitmapFromStorage(String path)
			throws FileNotFoundException {
		BitmapFactory.Options options = new BitmapFactory.Options();

		options.inJustDecodeBounds = true;

		Bitmap bitmap = BitmapFactory.decodeFile(path, options);

		return null;
	}

	public Drawable compressBitmap(Bitmap origin) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		origin.compress(Bitmap.CompressFormat.JPEG, 20, baos);

		while (baos.toByteArray().length / 1024 > 1024) {
			baos.reset();
			origin.compress(Bitmap.CompressFormat.JPEG, 50, baos);
		}

		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());

		BitmapFactory.Options newOpts = new BitmapFactory.Options();

		newOpts.inJustDecodeBounds = true;

		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);

		newOpts.inJustDecodeBounds = false;

		int w = newOpts.outWidth;
		int h = newOpts.outHeight;

		float hh = mScreenHeight / 3 - mSreenWidth / 4;
		float ww = mSreenWidth;

		int be = 1;

		if (w > h && w > ww) {
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {
			be = (int) (newOpts.outHeight / hh);
		}

		if (be <= 0)
			be = 1;

		newOpts.inSampleSize = be;

		isBm = new ByteArrayInputStream(baos.toByteArray());

		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);

		return new BitmapDrawable(bitmap);
	}

	@Override
	public void onClick(View v) {
		if (mUserHistoryData.mPosName != null
				|| mUserHistoryData.mPosName.length() != 0) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_WEB_SEARCH);
			intent.putExtra(SearchManager.QUERY, mUserHistoryData.mPosName);
			startActivity(intent);
		} else {
			Intent intent = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("http://baike.baidu.com/link?url=FnhQSDf4y12tWvWmbSTJtPwjo0f_dQIB166_O9bpTgpvdArwabeDB8ur3oSXPssz"));
			startActivity(intent);
		}
	}

	// -----------------------------------------------------------------------------------------------------------------------------

	class GetDetailDataTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... paramArrayOfParams) {
			mUserInfo = AccessTokenKeeper.readUserInfo(mCtx);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			setDetailData();
			new GetDetailDataNetTask().execute(null);
			new GetAroundNetTask().execute(mUserHistoryData.mLat,
					mUserHistoryData.mLng);
			super.onPostExecute(result);
		}

	}

	class GetDetailDataNetTask extends AsyncTask<Void, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... paramArrayOfParams) {
			if (!SeeXianUtils.isNetworkAvailable(mCtx)) {
				return null;
			}

			ContentResolver resolver = mCtx.getContentResolver();

			if (mUserHistoryData.mPosName == null
					|| mUserHistoryData.mPosName.isEmpty()) {
				HashMap<String, Object> queryParams = new HashMap<String, Object>();
				queryParams.put("ak", Constants.BAIDU_MAP_SERVICE_KEY);
				StringBuilder location = new StringBuilder();
				location.append(mUserHistoryData.mLng);
				location.append(",");
				location.append(mUserHistoryData.mLat);
				queryParams.put("location", location.toString());
				queryParams.put("output", "json");
				Object oRGEO = SeeXianNetUtils
						.getResult(
								"http://api.map.baidu.com/telematics/v3/reverseGeocoding",
								queryParams, null);
				if (oRGEO == null) {
					return null;
				}
				try {
					JSONObject jRGEO = new JSONObject(oRGEO.toString());
					String description = jRGEO.getString("description");
					String province = jRGEO.getString("province");
					String city = jRGEO.getString("city");

					mUserHistoryData.mPosName = description.replaceFirst(
							province, "");

					if (province.equals(city)) {
						mUserHistoryData.mPosName = city
								+ mUserHistoryData.mPosName;
					}

					Loge.i("mUserHistoryData.mPosName = "
							+ mUserHistoryData.mPosName);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				ContentValues updatedValues = new ContentValues();
				updatedValues.put(SeeXianProvider.KEY_USER_POS_NAME,
						mUserHistoryData.mPosName);

				String where = SeeXianProvider.KEY_USER_POST_ID + "='"
						+ mUserHistoryData.mPostId + "'";
				resolver.update(SeeXianProvider.CONTENT_URI_SEE_XIAN_USER_POST,
						updatedValues, where, null);
			}

			if (mUserHistoryData.mDistance == null
					|| mUserHistoryData.mDistance.isEmpty()) {
				HashMap<String, Object> queryParams = new HashMap<String, Object>();
				queryParams.put("ak", Constants.BAIDU_MAP_SERVICE_KEY);
				StringBuilder waypoints = new StringBuilder();
				waypoints.append(mUserHistoryData.mLng);
				waypoints.append(",");
				waypoints.append(mUserHistoryData.mLat);
				waypoints.append(";");
				waypoints.append(Constants.LNG_OF_BELL_TOWER);
				waypoints.append(",");
				waypoints.append(Constants.LAT_OF_BELL_TOWER);
				queryParams.put("waypoints", waypoints.toString());
				queryParams.put("output", "json");
				Object oDistance = SeeXianNetUtils.getResult(
						"http://api.map.baidu.com/telematics/v3/distance",
						queryParams, null);

				String distance = null;

				if (oDistance == null) {
					return null;
				}
				try {
					JSONObject jDistance = new JSONObject(oDistance.toString());
					JSONArray resultJArray = jDistance.getJSONArray("results");

					if (resultJArray.length() > 0) {
						distance = resultJArray.get(0).toString();
					}

					Loge.i("Distance to Xian = " + distance);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				mUserHistoryData.mDistance = distance;

				ContentValues updatedValues = new ContentValues();
				updatedValues.put(SeeXianProvider.KEY_USER_DISATACE_TO_XIAN,
						distance);

				String where = SeeXianProvider.KEY_USER_POST_ID + "='"
						+ mUserHistoryData.mPostId + "'";
				resolver.update(SeeXianProvider.CONTENT_URI_SEE_XIAN_USER_POST,
						updatedValues, where, null);
			}

			return "ok";
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				setDetailData();
			}
			super.onPostExecute(result);
		}

	}

	class GetAroundNetTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			mAroundGrid.setVisibility(View.GONE);
			mAroundRefreshButton.setVisibility(View.GONE);
			mAroundRefreshProgress.setVisibility(View.VISIBLE);
			aroundListData.clear();
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {

			String lat = null;
			String lng = null;
			if (params.length >= 2) {
				lat = params[0];
				lng = params[1];
			}
			Loge.i("GetAroundNetTask lat = " + lat + " lng = " + lng);

			ContentResolver resolver = mCtx.getContentResolver();

			String where = SeeXianProvider.KEY_POST_ID + "='"
					+ mUserHistoryData.mPostId + "'";
			Cursor cursor = resolver.query(
					SeeXianProvider.CONTENT_URI_SEE_XIAN_LANDSCAPE, null,
					where, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						AroundData aData = new AroundData();
						aData.mName = cursor.getString(1);
						aData.mPrice = cursor.getString(2);
						aData.mDescription = cursor.getString(3);
						aData.mIcon = cursor.getString(4);
						aData.mTel = cursor.getString(5);
						aData.mAddress = cursor.getString(6);
						aData.mLinkUrl = cursor.getString(7);
						aData.mProvider = cursor.getString(8);
						aroundListData.add(aData);
					} while (cursor.moveToNext());
				}
				cursor.close();
			}

			if (aroundListData.size() == 25) {
				return "ok";
			} else {
				aroundListData.clear();
			}

			for (String item : interestSpot) {
				if (!SeeXianUtils.isNetworkAvailable(mCtx)) {
					return null;
				}

				HashMap<String, Object> queryParams = new HashMap<String, Object>();
				queryParams.put("ak", Constants.BAIDU_MAP_SERVICE_KEY);
				StringBuilder location = new StringBuilder();
				location.append(lng);
				location.append(",");
				location.append(lat);
				queryParams.put("location", location.toString());
				queryParams.put("keyWord", item);
				queryParams.put("output", "json");
				Object oLocal = SeeXianNetUtils.getResult(
						"http://api.map.baidu.com/telematics/v3/local",
						queryParams, null);
				if (oLocal == null) {
					break;
				}
				phaseInterestInfo(oLocal);

			}

			Loge.i("GetAroundNetTask aroundListData size = "
					+ aroundListData.size());
			if (aroundListData.size() < 25) {
				int addSize = 25 - aroundListData.size();
				for (int i = 0; i < addSize; i++) {
					AroundData aData = new AroundData();
					aData.mName = "";
					aroundListData.add(aData);
				}
			}
			Loge.i("GetAroundNetTask aroundListData size = "
					+ aroundListData.size());

			// random sort the aroundListData
			ArrayList<AroundData> tempAroundListData = new ArrayList<AroundData>();
			for (int i = 0; i < 25; i++) {
				int num = (int) (Math.round(Math.random() * (24 - i)));
				if (num < aroundListData.size()) {
					tempAroundListData.add(aroundListData.get(num));
					aroundListData.remove(num);
				}
			}

			aroundListData.addAll(tempAroundListData);
			tempAroundListData.clear();

			ArrayList<ContentProviderOperation> opertions = new ArrayList<ContentProviderOperation>();

			for (AroundData item : aroundListData) {
				ContentProviderOperation.Builder builder = ContentProviderOperation
						.newInsert(
								SeeXianProvider.CONTENT_URI_SEE_XIAN_LANDSCAPE)
						.withValue(AroundData.KEY_NAME, item.mName)
						.withValue(AroundData.KEY_PRICE, item.mPrice)
						.withValue(AroundData.KEY_DESCRIPTION,
								item.mDescription)
						.withValue(AroundData.KEY_ICON, item.mIcon)
						.withValue(AroundData.KEY_TEL, item.mTel)
						.withValue(AroundData.KEY_ADDRESS, item.mAddress)
						.withValue(AroundData.KEY_LINK_URL, item.mLinkUrl)
						.withValue(AroundData.KEY_PROIVDER, item.mProvider)
						.withValue(AroundData.KEY_POST_ID,
								mUserHistoryData.mPostId);
				opertions.add(builder.build());
			}

			try {
				resolver.applyBatch("com.comic.seexian", opertions);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			}

			if (aroundListData.size() > 0) {
				return "ok";
			} else {
				return null;
			}

		}

		@Override
		protected void onPostExecute(String result) {
			mAroundRefreshProgress.setVisibility(View.GONE);

			if (result != null) {
				mAroundLoadingView.setVisibility(View.GONE);
				mAroundRefreshButton.setVisibility(View.GONE);
				mAroundGrid.setVisibility(View.VISIBLE);
				mAroundGridAdapter.setListData(aroundListData);
			} else {
				mAroundRefreshButton.setVisibility(View.VISIBLE);
				mAroundGrid.setVisibility(View.GONE);
			}
			super.onPostExecute(result);
		}

		void phaseInterestInfo(Object oInfo) {
			try {
				JSONObject jInfo = new JSONObject(oInfo.toString());
				JSONArray aPointList = jInfo.getJSONArray("pointList");

				for (int i = 0; i < aPointList.length(); i++) {
					JSONObject jItem = aPointList.getJSONObject(i);
					JSONObject jAddInfo = jItem
							.getJSONObject("additionalInformation");

					AroundData aData = new AroundData();

					aData.mName = jAddInfo.getString("name");
					aData.mPrice = jAddInfo.getString("price");
					aData.mTel = jAddInfo.getString("telepnone");
					aData.mAddress = jAddInfo.getString("address");
					aData.mDescription = jAddInfo.getString("tag");

					StringBuilder linkNameSB = new StringBuilder();
					StringBuilder linkAddressSB = new StringBuilder();
					try {
						JSONArray aLink = jAddInfo.getJSONArray("link");
						for (int j = 0; j < aLink.length(); j++) {
							if (j != 0) {
								linkNameSB.append("\0");
								linkAddressSB.append("\0");
							}

							JSONObject jLinkItem = aLink.getJSONObject(j);
							String linkName = jLinkItem.getString("name");
							String linkAddress = jLinkItem.getString("url");

							linkNameSB.append(linkName);
							linkAddressSB.append(linkAddress);
						}
						aData.mProvider = linkNameSB.toString();
						aData.mLinkUrl = linkAddressSB.toString();
					} catch (JSONException e) {
						Loge.e(e.getMessage());
					}

					aroundListData.add(aData);
				}

			} catch (JSONException e) {
				Loge.e(e.getMessage());
			}
		}
	}

}
