package com.comic.seexian;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.comic.seexian.about.AboutActivity;
import com.comic.seexian.clean.SeexianCleanService;
import com.comic.seexian.history.UserHistoryActivity;
import com.comic.seexian.sinaauth.AccessTokenKeeper;
import com.comic.seexian.sinaauth.ConstantsSina;
import com.comic.seexian.sinaauth.SinaPoisData;
import com.comic.seexian.sinaauth.UserInfo;
import com.comic.seexian.upload.UploadService;
import com.comic.seexian.upload.UploadService.UploadBinder;
import com.comic.seexian.utils.BitmapUtils;
import com.comic.seexian.utils.SeeXianNetUtils;
import com.comic.seexian.utils.SeeXianUtils;
import com.comic.seexian.view.ImageUploadDialog;
import com.comic.seexian.view.NetworkDialog;
import com.comic.seexian.view.Panel;
import com.comic.seexian.view.ProfileDialog;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.sso.SsoHandler;

public class MainActivity extends Activity {

	private Context mCtx;

	private ImageButton photoIButton, historyIButton, helpIButton,
			albumIButton, sinaAuthButton;
	private Button locationSetIButton, skipIButton;

	private Panel mPanel = null;

	private NetworkDialog mNetworkDialog = null;

	private ImageUploadDialog mImageUploadDialog = null;

	private ProfileDialog mProfileDialog = null;

	private Bitmap mPhoto = null;

	private Weibo mWeibo;
	public static Oauth2AccessToken accessToken;
	private SsoHandler mSsoHandler;

	private UploadBinder mBinder = null;
	private UploadService mUploadService = null;

	double lat = -1, lng = -1;

	private View.OnClickListener locationOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.skip_button: {
				if (mPanel != null) {
					mPanel.hidePanel();
				}
			}
				break;
			case R.id.setting_button: {
				// start location setting page
				Intent intent = new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);

				if (mPanel != null) {
					mPanel.hidePanel();
				}
			}
				break;
			default:
				break;
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCtx = this;
		setContentView(R.layout.activity_main);

		Intent cleanService = new Intent();
		cleanService.setClass(MainActivity.this, SeexianCleanService.class);
		startService(cleanService);

		mWeibo = Weibo.getInstance(ConstantsSina.APP_KEY,
				ConstantsSina.REDIRECT_URL, ConstantsSina.SCOPE);
		MainActivity.accessToken = AccessTokenKeeper.readAccessToken(this);

		sinaAuthButton = (ImageButton) findViewById(R.id.sina);
		photoIButton = (ImageButton) findViewById(R.id.photo);
		albumIButton = (ImageButton) findViewById(R.id.open);
		historyIButton = (ImageButton) findViewById(R.id.history);
		helpIButton = (ImageButton) findViewById(R.id.help);
		locationSetIButton = (Button) findViewById(R.id.setting_button);
		skipIButton = (Button) findViewById(R.id.skip_button);
		mPanel = (Panel) findViewById(R.id.location_warn_panel);
		mPanel.hideHandle();

		sinaAuthButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				accessToken = AccessTokenKeeper
						.readAccessToken(getApplicationContext());
				String sToken = accessToken.getToken();
				String sCode = AccessTokenKeeper
						.readAccessCode(getApplicationContext());
				if ((sToken == null || sToken.isEmpty())
						&& (sCode == null || sCode.isEmpty())) {
					mSsoHandler = new SsoHandler(MainActivity.this, mWeibo);
					mSsoHandler.authorize(new AuthDialogListener(), null);
				} else {
					if (mProfileDialog == null) {
						mProfileDialog = new ProfileDialog(mCtx,
								R.style.Theme_dialog);
					}
					mProfileDialog.show();
				}

			}
		});

		photoIButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, Constants.RESULT_CAMERA);
			}
		});

		albumIButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_PICK, null);
				intent.setDataAndType(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						"image/jpeg");
				startActivityForResult(intent, Constants.RESULT_ALBUM);
			}
		});

		historyIButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, UserHistoryActivity.class);
				startActivity(intent);
			}
		});

		helpIButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, AboutActivity.class);
				startActivity(intent);
			}
		});

		locationSetIButton.setOnClickListener(locationOnClickListener);
		skipIButton.setOnClickListener(locationOnClickListener);

		Intent intent = new Intent();
		intent.setClass(MainActivity.this, UploadService.class);
		bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Loge.d("onServiceConnected");
			if (name.getShortClassName().endsWith("UploadService")) {
				Loge.d("class match UploadService");
				mBinder = (UploadBinder) service;
				mUploadService = mBinder.getService();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Loge.d("onServiceDisconnected");
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		if (!SeeXianUtils.isNetworkAvailable(this)) {
			Loge.d("Net work off show warning dialog");
			if (mNetworkDialog == null) {
				mNetworkDialog = new NetworkDialog(this, R.style.Theme_dialog);
			}
			mNetworkDialog.show();
		}

		if (!SeeXianUtils.isLocationProvideOn(this)) {
			Loge.d("All Location provider is off show warning panel");
			mPanel.showPanel();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
	    SeeXianApplication app = (SeeXianApplication)this.getApplication();
		if (app.mBMapManager != null) {
			app.mBMapManager.destroy();
			app.mBMapManager = null;
		}
		unbindService(mServiceConnection);
		super.onDestroy();

	}

	@Override
	synchronized protected void onActivityResult(int requestCode,
			int resultCode, Intent data) {
		if (data == null) {
			return;
		}
		switch (requestCode) {
		case Constants.RESULT_ALBUM:
			Loge.i("RESULT_ALBUM");
			if (resultCode == Activity.RESULT_OK) {
				// clipPhoto(data.getData());
				if (mPhoto != null) {
					mPhoto.recycle();
					mPhoto = null;
				}

				Uri photoUri = data.getData();
				Bitmap bmp = BitmapUtils.getBitmapFromUri(
						getApplicationContext(), photoUri);
				if (bmp == null) {
					return;
				}

				mPhoto = BitmapUtils.compressBitmap(bmp);
				bmp.recycle();

				getLocation();

				if (mPhoto != null) {
					if (mImageUploadDialog == null) {
						mImageUploadDialog = new ImageUploadDialog(this,
								R.style.Theme_dialog, mHandler);
					}
					mImageUploadDialog.show();
					mImageUploadDialog.SetImage(mPhoto);
					mImageUploadDialog.SetLocationPos(lat, lng);
				}
			}
			break;
		case Constants.RESULT_CAMERA:
			Loge.i("RESULT_CAMERA");
			if (resultCode == Activity.RESULT_OK) {
				// clipPhoto(data.getData());
				if (mPhoto != null) {
					mPhoto.recycle();
					mPhoto = null;
				}

				Bitmap bmp = (Bitmap) data.getExtras().get("data");
				if (bmp == null) {
					return;
				}

				mPhoto = BitmapUtils.compressBitmap(bmp);
				bmp.recycle();

				getLocation();

				if (mPhoto != null) {
					if (mImageUploadDialog == null) {
						mImageUploadDialog = new ImageUploadDialog(this,
								R.style.Theme_dialog, mHandler);
					}
					mImageUploadDialog.show();
					mImageUploadDialog.SetImage(mPhoto);
					mImageUploadDialog.SetLocationPos(lat, lng);
				}
			}
			break;
		case Constants.RESULT_CLIP:
			Loge.i("RESULT_CLIP");
			Bundle extras = data.getExtras();
			if (extras != null) {
				if (mPhoto != null) {
					mPhoto.recycle();
					mPhoto = null;
				}
				mPhoto = extras.getParcelable("data");

				getLocation();

				if (mPhoto != null) {
					if (mImageUploadDialog == null) {
						mImageUploadDialog = new ImageUploadDialog(this,
								R.style.Theme_dialog, mHandler);
					}
					mImageUploadDialog.show();
					mImageUploadDialog.SetImage(mPhoto);
					mImageUploadDialog.SetLocationPos(lat, lng);
				}

			}
			break;
		}

		super.onActivityResult(requestCode, resultCode, data);

		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	public void clipPhoto(Uri uri) {
		Loge.d("clipPhoto uri = " + uri.toString());
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 300);
		intent.putExtra("aspectY", 300);
		intent.putExtra("outputX", 300);
		intent.putExtra("outputY", 300);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, Constants.RESULT_CLIP);
	}

	void getLocation() {
		LocationManager locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (locationMgr == null) {
			return;
		}

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		String provider = locationMgr.getBestProvider(criteria, true);
		Location location = locationMgr.getLastKnownLocation(provider);

		if (location == null) {
			return;
		}

		lat = location.getLatitude();
		lng = location.getLongitude();

		Loge.d("Latitude = " + lat + "Longitude = " + lng);
	}

	public void getUserInfo() {

		new Thread(new Runnable() {
			@Override
			public void run() {

				String acctoken = SeeXianNetUtils.getAccessToken(mCtx);

				if (acctoken == null || acctoken.isEmpty()) {
					return;
				}

				String sUeserId = null;

				Object oUserId = SeeXianNetUtils.getResult(
						"https://api.weibo.com/2/account/get_uid.json", null,
						acctoken);
				if (oUserId == null) {
					return;
				}

				try {
					JSONObject jUserId = new JSONObject(oUserId.toString());
					sUeserId = jUserId.getString("uid");
				} catch (JSONException e) {
					e.printStackTrace();
				}

				Loge.d("sUeserId = " + sUeserId);

				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("uid", sUeserId);
				Object oUserInfo = SeeXianNetUtils.getResult(
						"https://api.weibo.com/2/users/show.json", params,
						acctoken);

				if (oUserInfo == null) {
					return;
				}
				UserInfo userInfo = new UserInfo();
				try {
					JSONObject jUserInfo = new JSONObject(oUserInfo.toString());
					userInfo.mScreenName = jUserInfo.getString("screen_name");
					userInfo.mAvatar = jUserInfo.getString("avatar_large");
				} catch (JSONException e) {
					e.printStackTrace();
				}

				Loge.d("mScreenName = " + userInfo.mScreenName);
				Loge.d("mAvatar = " + userInfo.mAvatar);

				if ((userInfo.mScreenName != null && !userInfo.mScreenName
						.isEmpty())
						&& (userInfo.mAvatar != null && !userInfo.mAvatar
								.isEmpty())) {
					AccessTokenKeeper.keepUserInfo(mCtx, userInfo);
				}

			}
		}).start();
	}

	class AuthDialogListener implements WeiboAuthListener {

		@Override
		public void onCancel() {
			Toast.makeText(getApplicationContext(), "Auth cancel",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onComplete(Bundle values) {
			String code = values.getString("code");
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");

			Loge.d("code = " + code);
			Loge.d("token = " + token);
			Loge.d("expires_in = " + expires_in);

			if (token != null && expires_in != null) {
				MainActivity.accessToken = new Oauth2AccessToken(token,
						expires_in);
				AccessTokenKeeper.clear(getApplicationContext());
				AccessTokenKeeper.keepAccessToken(getApplicationContext(),
						MainActivity.accessToken);
			}

			if (token == null && expires_in == null && code != null) {
				AccessTokenKeeper.keepAccessCode(getApplicationContext(), code);
			}

			getUserInfo();

			Toast.makeText(getApplicationContext(), "Auth Success",
					Toast.LENGTH_LONG).show();

		}

		@Override
		public void onError(WeiboDialogError arg0) {
			Toast.makeText(getApplicationContext(),
					"Auth error : " + arg0.getMessage(), Toast.LENGTH_LONG)
					.show();
		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			Toast.makeText(getApplicationContext(),
					"Auth exception : " + arg0.getMessage(), Toast.LENGTH_LONG)
					.show();
		}

	}

	private Handler mHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.MSG_DO_UPLOAD_PHOTO: {
				Loge.d("handleMessage MSG_DO_UPLOAD_PHOTO");
				if (mUploadService != null && mBinder != null) {
					synchronized (mPhoto) {
						try {
							ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
							mPhoto.compress(Bitmap.CompressFormat.JPEG, 100,
									byteStream);

							Parcel pdata = Parcel.obtain();
							pdata.writeInt(byteStream.toByteArray().length);
							pdata.writeByteArray(byteStream.toByteArray());

							SinaPoisData poisItem = (SinaPoisData) msg.obj;

							if (poisItem != null) {
								String[] loaction = new String[2];
								loaction[0] = poisItem.mLat;
								loaction[1] = poisItem.mLong;
								pdata.writeStringArray(loaction);
							} else {
								if (lat != -1 && lng != -1) {
									String[] loaction = new String[2];
									loaction[0] = String.valueOf(lat);
									loaction[1] = String.valueOf(lng);
									pdata.writeStringArray(loaction);
								}
							}

							mBinder.transact(UploadService.CODE_UPLOADE_PHOTO,
									pdata, null, IBinder.FLAG_ONEWAY);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
			}
				break;
			case Constants.MESSAGE_NETWORK_ERROR: {
				if (mNetworkDialog == null) {
					mNetworkDialog = new NetworkDialog(mCtx,
							R.style.Theme_dialog);
				}
				mNetworkDialog.show();
			}
				break;
			default:
				break;
			}
			return false;
		}
	});

}
