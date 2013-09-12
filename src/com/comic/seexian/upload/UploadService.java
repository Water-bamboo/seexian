package com.comic.seexian.upload;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.comic.seexian.Loge;
import com.comic.seexian.R;
import com.comic.seexian.utils.SeeXianUtils;

public class UploadService extends Service {

	private static final String TEMP_IMAGE_FOLDER = Environment
			.getExternalStorageDirectory().getPath() + "/SeeXian/Temp";

	public static final int CODE_UPLOADE_PHOTO = 1005;

	public static final int NOTIFICATION_UPLOAD_ID = 75687;

	// Sets the amount of time an idle thread will wait for a task before
	// terminating
	private static final int KEEP_ALIVE_TIME = 1;

	// Sets the Time Unit to seconds
	private static TimeUnit KEEP_ALIVE_TIME_UNIT;

	// Sets the initial threadpool size to 8
	private static final int CORE_POOL_SIZE = 1;

	// Sets the maximum threadpool size to 8
	private static final int MAXIMUM_POOL_SIZE = 1;

	public static final int MESSAGE_SAVE_PHOTO_SUCCRESS = 2005;
	public static final int MESSAGE_UPLOAD_SUCCRESS = 2006;
	public static final int MESSAGE_SHOW_TOAST_LOGIN = 2007;
	public static final int MESSAGE_SHOW_TOAST_POST_SUCCESS = 2008;
	public static final int MESSAGE_NETWORK_ERROR = 2009;

	private Context mAppCtx = null;

	private String lat, lng;

	private String mFilePath;

	private Bitmap image = null;

	private NotificationManager mNotificationManager = null;
	private Notification mNotification = null;

	private final BlockingQueue<Runnable> mUploadWorkQueue;
	private final ThreadPoolExecutor mUploadThreadPool;
	private final Queue<UploadTask> mUploadTaskWorkQueue;

	private Handler mMessageHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SAVE_PHOTO_SUCCRESS: {
				Loge.d("MESSAGE_SAVE_PHOTO_SUCCRESS");
				startDownload();
			}
				break;
			case MESSAGE_UPLOAD_SUCCRESS: {
				Loge.d("MESSAGE_UPLOAD_SUCCRESS");
			}
				break;
			case MESSAGE_SHOW_TOAST_LOGIN: {
				Loge.d("MESSAGE_SHOW_TOAST_LOGIN");
				Toast.makeText(mAppCtx, R.string.plz_login_sina,
						Toast.LENGTH_LONG).show();
			}
				break;
			case MESSAGE_SHOW_TOAST_POST_SUCCESS: {
				Loge.d("MESSAGE_SHOW_TOAST_POST_SUCCESS");
				Toast.makeText(mAppCtx, R.string.post_success,
						Toast.LENGTH_LONG).show();
			}
				break;
			case UploadRunnable.HTTP_STATE_STARTED: {
				Loge.d("HTTP_STATE_STARTED");
				showNotification(mAppCtx,
						mAppCtx.getResources().getText(R.string.upload)
								.toString(), 0);
			}
				break;
			case UploadRunnable.HTTP_PROGRESS: {
				Loge.d("HTTP_PROGRESS");
				int progress = msg.arg1;
				showNotification(mAppCtx,
						mAppCtx.getResources().getText(R.string.upload)
								.toString(), progress);
			}
				break;
			case UploadRunnable.HTTP_STATE_COMPLETED: {
				Loge.d("HTTP_STATE_COMPLETED");
				UploadTask uploadTask = (UploadTask) msg.obj;
				recycleTask(uploadTask);
				showNotification(mAppCtx,
						mAppCtx.getResources().getText(R.string.post_success)
								.toString(), 100);
			}
				break;
			case UploadRunnable.HTTP_STATE_FAILED: {
				Loge.d("HTTP_STATE_FAILED");
				UploadTask uploadTask = (UploadTask) msg.obj;
				recycleTask(uploadTask);
				showNotification(mAppCtx,
						mAppCtx.getResources().getText(R.string.post_fail)
								.toString(), -1);
			}
				break;
			case UploadRunnable.TOKEN_ERROR: {
				Loge.d("TOKEN_ERROR");
				UploadTask uploadTask = (UploadTask) msg.obj;
				recycleTask(uploadTask);
			}
				break;
			case MESSAGE_NETWORK_ERROR: {
				Loge.d("MESSAGE_NETWORK_ERROR");
				Toast.makeText(mAppCtx, R.string.network_warn_info,
						Toast.LENGTH_LONG).show();
			}
				break;
			default:
				break;
			}
			return false;
		}
	});

	public UploadService() {

		KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

		mUploadTaskWorkQueue = new LinkedBlockingQueue<UploadTask>();

		mUploadWorkQueue = new LinkedBlockingQueue<Runnable>();

		mUploadThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE,
				MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT,
				mUploadWorkQueue);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Loge.d("onCreate");
		mAppCtx = this.getBaseContext();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Loge.d("onBind");
		UploadBinder binder = new UploadBinder();
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Loge.d("onUnbind");
		return super.onUnbind(intent);
	}

	public UploadTask startDownload() {
		UploadTask uploadTask = mUploadTaskWorkQueue.poll();

		if (null == uploadTask) {
			uploadTask = new UploadTask();
		}
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("status", "From SeeXian");
		params.put("lat", lat);
		params.put("long", lng);

		uploadTask.initUploadTask(mAppCtx, mFilePath, params, mMessageHandler);

		mUploadThreadPool.execute(uploadTask.getHTTPUploadRunnable());

		return uploadTask;
	}

	void recycleTask(UploadTask uploadTask) {
		mUploadTaskWorkQueue.offer(uploadTask);
	}

	private void showNotification(Context context, String s, int progress) {
		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		if (mNotification == null) {
			long when = System.currentTimeMillis();
			mNotification = new Notification(R.drawable.ic_launcher, mAppCtx
					.getResources().getText(R.string.app_name), when);
		}

		if (0 < progress && progress < 100) {
			mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
			mNotification.setLatestEventInfo(context, mAppCtx.getResources()
					.getText(R.string.app_name),
					s + " " + String.valueOf(progress) + " %", null);
		} else {
			mNotification.flags = Notification.FLAG_AUTO_CANCEL;
			mNotification.setLatestEventInfo(context, mAppCtx.getResources()
					.getText(R.string.app_name), s, null);
		}

		mNotificationManager.notify(NOTIFICATION_UPLOAD_ID, mNotification);
	}

	public class UploadBinder extends Binder {

		public UploadService getService() {
			return UploadService.this;
		}

		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
				int flags) throws RemoteException {
			Loge.d("onTransact code = " + code);

			int photoSize = data.readInt();

			Loge.d("onTransact photoSize = " + photoSize);

			final byte[] val = new byte[photoSize];

			data.readByteArray(val);

			String[] location = new String[2];

			data.readStringArray(location);

			lat = location[0];
			lng = location[1];

			Loge.d("onTransact" + "Latitude = " + lat + "Longitude = " + lng);

			if (image != null) {
				image.recycle();
			}
			image = BitmapFactory.decodeByteArray(val, 0, photoSize);

			if (!SeeXianUtils.isNetworkAvailable(mAppCtx)) {
				if (mMessageHandler != null) {
					mMessageHandler.sendEmptyMessage(MESSAGE_NETWORK_ERROR);
				}
				super.onTransact(code, data, reply, flags);
			}

			new Thread(new Runnable() {

				@Override
				public void run() {

					File folder = new File(TEMP_IMAGE_FOLDER);
					if (!folder.exists()) {
						Loge.w("Folder not exsist, create new folder");
						folder.mkdirs();
					}

					Date date = new Date();
					String photoFileName = DateFormat.format(
							"MM_dd_yy_hh_mm_aa", date).toString();

					mFilePath = TEMP_IMAGE_FOLDER + "/" + photoFileName
							+ ".jpg";

					BufferedOutputStream baos = null;
					try {
						Loge.d("onTransact Storage path = " + mFilePath);
						FileOutputStream fos = new FileOutputStream(mFilePath);
						baos = new BufferedOutputStream(fos);
						baos.write(val);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							if (baos != null) {
								baos.close();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					if (mMessageHandler != null) {
						mMessageHandler
								.sendEmptyMessage(MESSAGE_SAVE_PHOTO_SUCCRESS);
					}
				}

			}).start();

			return super.onTransact(code, data, reply, flags);
		}
	}

}
