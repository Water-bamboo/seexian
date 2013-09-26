package com.comic.seexian.history;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.comic.seexian.Constants;
import com.comic.seexian.Loge;
import com.comic.seexian.R;
import com.comic.seexian.database.SeeXianProvider;
import com.comic.seexian.detail.DetailActivity;
import com.comic.seexian.utils.SeeXianNetUtils;
import com.comic.seexian.utils.SeeXianUtils;
import com.comic.seexian.view.NetworkDialog;
import com.comic.seexian.view.PullDownRefreashListView;

public class UserHistoryActivity extends Activity implements
		OnItemClickListener, OnItemLongClickListener,
		OnCreateContextMenuListener {

	private Context mCtx;

	private PullDownRefreashListView mListView;
	private UserHistoryAdapter mListAdapter;

	private View mLoadingView;
	private ProgressBar mLoadingProgress;
	private TextView mLoadingText;

	private ContentResolver mContentResolver;

	private ArrayList<UserHistoryData> mListData = new ArrayList<UserHistoryData>();

	private NetworkDialog mNetworkDialog = null;

	// ------------------------------------------------------------------

	private View mRefreshViewInside;
	private View mRefreshHorizontalImage;
	private View mRefreshHorizontalProgress;

	// ------------------------------------------------------------------

	private String mLatestId;

	private int mSelectedItem;

	private Handler mMessageHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.MESSAGE_NETWORK_ERROR: {
				if (mNetworkDialog == null) {
					mNetworkDialog = new NetworkDialog(mCtx,
							R.style.Theme_dialog);
				}
				mNetworkDialog.show();
			}
				break;
			case Constants.MESSAGE_SHOW_TOKEN_WARN: {
				Toast.makeText(mCtx, R.string.plz_login_sina, Toast.LENGTH_LONG)
						.show();
			}
				break;
			default:
				break;
			}
			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCtx = this;

		setContentView(R.layout.user_history_layout);

		mListView = (PullDownRefreashListView) findViewById(R.id.user_history_list);

		mLoadingView = (View) findViewById(R.id.user_empty_view);
		mLoadingProgress = (ProgressBar) findViewById(R.id.user_loading_progress);
		mLoadingText = (TextView) findViewById(R.id.user_empty_text);

		mLoadingText.setText(R.string.loading);

		mRefreshViewInside = (View) findViewById(R.id.pull_to_refresh_a);
		mRefreshHorizontalImage = (View) findViewById(R.id.refreash_header_image_a);
		mRefreshHorizontalProgress = (View) findViewById(R.id.refreash_header_progress_a);

		mListView.setDivider(null);
		mListAdapter = new UserHistoryAdapter(this, null);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(this);
		mListView.addCustomView(mRefreshViewInside, mRefreshHorizontalImage,
				mRefreshHorizontalProgress);
		mListView.setOnCreateContextMenuListener(this);
		mListView.setOnItemLongClickListener(this);

		mListView
				.setOnRefreshListener(new PullDownRefreashListView.OnRefreshListener() {
					@Override
					public void onRefresh() {
						new GetDataFromNetTask().execute(null);
					}
				});

		new GetDataTask()
				.execute(SeeXianProvider.CONTENT_URI_SEE_XIAN_USER_POST);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Loge.d("onItemClick postion = " + arg2);

		Cursor cursor = (Cursor) mListAdapter.getItem(arg2);

		UserHistoryData itemData = new UserHistoryData();

		itemData.mPostId = cursor.getString(1);
		itemData.mPosName = cursor.getString(2);
		itemData.mText = cursor.getString(3);
		itemData.mSource = cursor.getString(4);
		itemData.mOriPic = cursor.getString(5);
		itemData.mThumbPic = cursor.getString(6);
		itemData.mTime = cursor.getString(7);
		itemData.mLat = cursor.getString(8);
		itemData.mLng = cursor.getString(9);
		itemData.mDistance = cursor.getString(10);
		itemData.mLandscapeId = cursor.getLong(11);

		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		Bundle extras = new Bundle();
		extras.putString(UserHistoryData.KEY_USER_POST_ID, itemData.mPostId);
		extras.putString(UserHistoryData.KEY_USER_POS_NAME, itemData.mPosName);
		extras.putString(UserHistoryData.KEY_USER_TEXT, itemData.mText);
		extras.putString(UserHistoryData.KEY_USER_SOURCE, itemData.mSource);
		extras.putString(UserHistoryData.KEY_USER_ORIGIN_PIC, itemData.mOriPic);
		extras.putString(UserHistoryData.KEY_USER_THUMB_PIC, itemData.mThumbPic);
		extras.putString(UserHistoryData.KEY_USER_TIME, itemData.mTime);
		extras.putString(UserHistoryData.KEY_USER_LATITUDE, itemData.mLat);
		extras.putString(UserHistoryData.KEY_USER_LONGITUDE, itemData.mLng);
		extras.putString(UserHistoryData.KEY_USER_DISATACE_TO_XIAN,
				itemData.mDistance);
		extras.putLong(UserHistoryData.KEY_USER_LANDSCAPE_ID,
				itemData.mLandscapeId);

		intent.putExtras(extras);
		intent.setClass(UserHistoryActivity.this, DetailActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		mSelectedItem = arg2;
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(R.string.delete_item);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Loge.i("onContextItemSelected delete weibo item = " + mSelectedItem);

		Cursor cursor = (Cursor) mListAdapter.getItem(mSelectedItem);
		UserHistoryData itemData = new UserHistoryData();
		itemData.mPostId = cursor.getString(1);

		new DeleteWeiboTask().execute(itemData.mPostId);
		return super.onContextItemSelected(item);
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

	private class GetDataTask extends AsyncTask<Uri, Void, Cursor> {
		Uri mUri;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (mLatestId == null) {
				mListView.setVisibility(View.INVISIBLE);
				mLoadingView.setVisibility(View.VISIBLE);
				mLoadingText.setText(R.string.loading);
				mLoadingProgress.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected Cursor doInBackground(Uri... urls) {
			if (mContentResolver == null) {
				mContentResolver = mCtx.getContentResolver();
			}

			mUri = urls[0];

			Loge.d("start get infos from database URI = ", mUri.toString());

			mListData.clear();

			Cursor cursor = mContentResolver.query(mUri, null, null, null,
					UserHistoryData.KEY_USER_POST_ID + " DESC");
			return cursor;

		}

		@Override
		protected void onPostExecute(Cursor result) {
			if (result != null) {
				Loge.d("get cursor success count = " + result.getCount());
				if (result.getCount() == 0) {
					new GetDataFromNetTask().execute(null);
					result.close();
					return;
				}
				if (result.moveToFirst()) {
					mLatestId = result.getString(result
							.getColumnIndex(UserHistoryData.KEY_USER_POST_ID));
					Loge.i("mLatestId = " + mLatestId);

					SharedPreferences pref = mCtx.getSharedPreferences(
							"seexian", Context.MODE_APPEND);
					String sTime = pref.getString("lastupdate", "");
					mListView.setLastUpdatedText(mCtx.getResources().getString(
							R.string.last_update)
							+ ": " + sTime);
					mListAdapter.swapCursor(result);
				}

				mListView.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
			} else {
				mLoadingText.setText(R.string.empty_text);
				mLoadingProgress.setVisibility(View.GONE);
			}
		}
	}

	private class GetDataFromNetTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			Loge.d("Get user post data from net");

			if (!SeeXianUtils.isNetworkAvailable(mCtx)) {
				if (mMessageHandler != null) {
					mMessageHandler
							.sendEmptyMessage(Constants.MESSAGE_NETWORK_ERROR);
				}
				return null;
			}

			String acctoken = SeeXianNetUtils.getAccessToken(mCtx);

			if (acctoken == null || acctoken.isEmpty()) {
				if (mMessageHandler != null) {
					mMessageHandler
							.sendEmptyMessage(Constants.MESSAGE_SHOW_TOKEN_WARN);
				}
				return null;
			}

			HashMap<String, Object> queryParams = new HashMap<String, Object>();
			queryParams.put("count", 30);
			queryParams.put("feature", 2);
			queryParams.put("trim_user", 1);
			if (mLatestId != null && !mLatestId.isEmpty()) {
				queryParams.put("since_id", mLatestId);
			}
			Object userPostInfo = SeeXianNetUtils.getResult(
					"https://api.weibo.com/2/statuses/user_timeline.json",
					queryParams, acctoken);
			if (userPostInfo == null) {
				return null;
			}
			ArrayList<UserHistoryData> tempListData = new ArrayList<UserHistoryData>();
			tempListData.addAll(SeeXianNetUtils
					.getUserHistoryData(userPostInfo));

			if (mContentResolver == null) {
				mContentResolver = mCtx.getContentResolver();
			}

			ArrayList<ContentProviderOperation> opertions = new ArrayList<ContentProviderOperation>();

			for (UserHistoryData item : tempListData) {
				if (!item.mText.contains("SeeXian")) {
					continue;
				}

				item.mTime = SeeXianUtils.formatDateTime(mCtx, item.mTime);

				ContentProviderOperation.Builder builder = ContentProviderOperation
						.newInsert(
								SeeXianProvider.CONTENT_URI_SEE_XIAN_USER_POST)
						.withValue(UserHistoryData.KEY_USER_POST_ID,
								item.mPostId)
						.withValue(UserHistoryData.KEY_USER_TEXT, item.mText)
						.withValue(UserHistoryData.KEY_USER_SOURCE,
								item.mSource)
						.withValue(UserHistoryData.KEY_USER_ORIGIN_PIC,
								item.mOriPic)
						.withValue(UserHistoryData.KEY_USER_THUMB_PIC,
								item.mThumbPic)
						.withValue(UserHistoryData.KEY_USER_TIME, item.mTime)
						.withValue(UserHistoryData.KEY_USER_LATITUDE, item.mLat)
						.withValue(UserHistoryData.KEY_USER_LONGITUDE,
								item.mLng);
				opertions.add(builder.build());
			}
			Loge.i("opertions count = " + opertions.size());

			try {
				mContentResolver.applyBatch("com.comic.seexian", opertions);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			}

			int size = tempListData.size();
			tempListData.clear();

			return String.valueOf(size);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result == null) {
				mLoadingText.setText(R.string.empty_text);
				mLoadingProgress.setVisibility(View.GONE);
			} else {
				int count = Integer.parseInt(result);
				Loge.d("data count = " + count);
				mListView.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
				new GetDataTask()
						.execute(SeeXianProvider.CONTENT_URI_SEE_XIAN_USER_POST);

				// update latest update time:
				SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss",
						Locale.CHINA);
				SharedPreferences pref = mCtx.getSharedPreferences("seexian",
						Context.MODE_APPEND);
				Editor editor = pref.edit();
				editor.putString("lastupdate", sdf.format(new Date()));
				editor.commit();

				mListView.onRefreshComplete();
			}
		}

	}

	class DeleteWeiboTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String postId;
			if (params.length == 0) {
				return null;
			}

			postId = params[0];

			Loge.i("DeleteWeiboTask post id = " + postId);

			if (!SeeXianUtils.isNetworkAvailable(mCtx)) {
				if (mMessageHandler != null) {
					mMessageHandler
							.sendEmptyMessage(Constants.MESSAGE_NETWORK_ERROR);
				}
				return null;
			}

			String acctoken = SeeXianNetUtils.getAccessToken(mCtx);

			if (acctoken == null || acctoken.isEmpty()) {
				if (mMessageHandler != null) {
					mMessageHandler
							.sendEmptyMessage(Constants.MESSAGE_SHOW_TOKEN_WARN);
				}
				return null;
			}

			HashMap<String, Object> queryParams = new HashMap<String, Object>();
			queryParams.put("id", postId);
			SeeXianNetUtils.postResult(
					"https://api.weibo.com/2/statuses/destroy.json",
					queryParams, acctoken);

			if (mContentResolver == null) {
				mContentResolver = mCtx.getContentResolver();
			}

			mContentResolver.delete(
					SeeXianProvider.CONTENT_URI_SEE_XIAN_USER_POST,
					"postid = '" + postId + "'", null);

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			new GetDataTask()
					.execute(SeeXianProvider.CONTENT_URI_SEE_XIAN_USER_POST);
			super.onPostExecute(result);
		}

	}

}
