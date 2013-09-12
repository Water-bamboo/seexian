package com.comic.seexian.detail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.comic.seexian.Loge;
import com.comic.seexian.R;
import com.comic.seexian.SeeXianApplication;
import com.comic.seexian.history.UserHistoryData;
import com.comic.seexian.image.PhotoView;
import com.comic.seexian.sinaauth.AccessTokenKeeper;
import com.comic.seexian.sinaauth.UserInfo;

public class DetailActivity extends Activity implements OnClickListener {

	private static final int MESSAGE_GET_DATA_FINISHED = 102;

	private Context mCtx;

	private int mScreenHeight;
	private int mSreenWidth;

	private PhotoView landscapeImage = null;
	private PhotoView IconImage = null;
	private PhotoView mMapView = null;

	private TextView nameText = null, detailText = null, detailNameText = null;

	private ImageButton linkButton = null;

	private Drawable mLandscapeImage;

	private UserHistoryData mUserHistoryData = new UserHistoryData();

	private UserInfo mUserInfo = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCtx = this.getApplicationContext();

		setContentView(R.layout.detail_layout);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mScreenHeight = dm.heightPixels;
		mSreenWidth = dm.widthPixels;
		Loge.d("ScreenHeight = " + mScreenHeight);
		Loge.d("SreenWidth = " + mSreenWidth);

		landscapeImage = (PhotoView) findViewById(R.id.landscape_pic);
		IconImage = (PhotoView) findViewById(R.id.icon_pic);
		mMapView = (PhotoView) findViewById(R.id.bmapsView);
		nameText = (TextView) findViewById(R.id.name_text);
		detailText = (TextView) findViewById(R.id.detail_text);
		detailNameText = (TextView) findViewById(R.id.detail_name_text);
		linkButton = (ImageButton) findViewById(R.id.link_button);

		int iconSize = mSreenWidth / 4;
		LayoutParams params1 = new LayoutParams(iconSize, iconSize);
		params1.setMargins(iconSize / 2, mScreenHeight / 3 - iconSize, 0, 0);
		IconImage.setLayoutParams(params1);

		LayoutParams params2 = new LayoutParams(LayoutParams.FILL_PARENT,
				mScreenHeight / 3 - iconSize / 2);
		landscapeImage.setLayoutParams(params2);
		landscapeImage.setImageResource(R.drawable.empty_photo);

		LayoutParams params3 = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params3.setMargins(iconSize + iconSize / 2, mScreenHeight / 3
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

		getDataFromExtra();

		Loge.i("onCreate 4");
	}

	private void getDataFromExtra() {
		Intent dataIntent = getIntent();
		Bundle extras = dataIntent.getExtras();

		mUserHistoryData.mPostId = extras
				.getString(UserHistoryData.KEY_USER_POST_ID);
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

		// detailText.setText(mHistoryData.mDetail);

		linkButton.setOnClickListener(this);

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
		// if (mHistoryData.mLinkUrl == null
		// || mHistoryData.mLinkUrl.length() == 0) {
		// Intent intent = new Intent();
		// intent.setAction(Intent.ACTION_WEB_SEARCH);
		// intent.putExtra(SearchManager.QUERY, mHistoryData.mName);
		// startActivity(intent);
		// } else {
		// Intent intent = new Intent(Intent.ACTION_VIEW,
		// Uri.parse(mHistoryData.mLinkUrl));
		// startActivity(intent);
		// }
	}

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
			super.onPostExecute(result);
		}

	}

}
