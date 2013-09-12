package com.comic.seexian.upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.ParcelFileDescriptor;

import com.comic.seexian.Loge;
import com.comic.seexian.utils.SeeXianNetUtils;
import com.comic.seexian.utils.SeeXianUtils;

public class UploadRunnable implements Runnable {

	static final int HTTP_STATE_FAILED = -1;
	static final int HTTP_STATE_STARTED = 0;
	static final int HTTP_STATE_COMPLETED = 1;
	static final int HTTP_PROGRESS = 3;
	static final int TOKEN_ERROR = -2;

	interface TaskRunnableUploadMethods {

		/**
		 * Sets the Thread that this instance is running on
		 */
		void setDownloadThread(Thread currentThread);

		/**
		 * Defines the actions for each state of the PhotoTask instance.
		 */
		void handleDownloadState(int state);

		/**
		 * Defines the progress of current upload task
		 */
		void handleDownloadProgress(int progress);

		String getFilePath();

		Context getContext();

		HashMap<String, Object> getParams();
	}

	final TaskRunnableUploadMethods mUploadTask;

	public UploadRunnable(TaskRunnableUploadMethods uploadTask) {
		mUploadTask = uploadTask;
	}

	@Override
	public void run() {

		mUploadTask.setDownloadThread(Thread.currentThread());

		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

		if (!SeeXianUtils.isNetworkAvailable(mUploadTask.getContext())) {
			mUploadTask.handleDownloadState(HTTP_STATE_FAILED);
			return;
		}

		try {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}

			mUploadTask.handleDownloadState(HTTP_STATE_STARTED);

			mUploadTask.handleDownloadProgress(0);

			String acctoken = SeeXianNetUtils.getAccessToken(mUploadTask
					.getContext().getApplicationContext());

			if (acctoken == null || acctoken.isEmpty()) {
				mUploadTask.handleDownloadState(TOKEN_ERROR);
			}

			URL url = new URL(
					"https://upload.api.weibo.com/2/statuses/upload.json");
			HttpURLConnection httpConn = (HttpURLConnection) url
					.openConnection();
			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			httpConn.setUseCaches(false);
			httpConn.setRequestMethod("POST");

			String BOUNDARY = "----------------------------8933e7b00565";

			// add Token
			StringBuffer stringBuffer = new StringBuffer();

			// Add access Token
			stringBuffer.append("--");
			stringBuffer.append(BOUNDARY);
			stringBuffer.append("\r\n");
			stringBuffer
					.append("Content-Disposition: form-data; name=\"access_token\"\r\n\r\n");
			stringBuffer.append(acctoken + "\r\n");

			HashMap<String, Object> params = mUploadTask.getParams();

			if (params != null) {
				Iterator<String> itrs = params.keySet().iterator();
				List<String> sortKeyList = new ArrayList<String>();
				while (itrs.hasNext()) {
					sortKeyList.add(itrs.next());
				}
				Collections.sort(sortKeyList);
				for (String key : sortKeyList) {
					String value = params.get(key).toString();
					Loge.i("key = " + key + " value = " + value);
					stringBuffer.append("--");
					stringBuffer.append(BOUNDARY);
					stringBuffer.append("\r\n");
					stringBuffer
							.append("Content-Disposition: form-data; name=\""
									+ key + "\"\r\n\r\n");
					stringBuffer.append(value + "\r\n");

				}
			}

			stringBuffer.append("--");
			stringBuffer.append(BOUNDARY);
			stringBuffer.append("\r\n");
			stringBuffer
					.append("Content-Disposition: form-data; name=\"pic\"; filename=\"pic.jpg\"\r\n");
			stringBuffer.append("Content-Type: image/jpeg\r\n\r\n");

			Loge.i("Content = " + stringBuffer.toString());

			byte[] data = stringBuffer.toString().getBytes("utf-8");
			byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();

			httpConn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + BOUNDARY);

			if (Thread.interrupted()) {
				throw new InterruptedException();
			}

			OutputStream os = httpConn.getOutputStream();
			os.write(data);

			mUploadTask.handleDownloadProgress(5);

			FileInputStream fileInputStream = null;
			int length = 0;
			Loge.d("uploadPhoto path = " + mUploadTask.getFilePath().toString());

			File file = null;
			try {
				file = new File(mUploadTask.getFilePath().toString());
				ParcelFileDescriptor is = ParcelFileDescriptor.open(file,
						ParcelFileDescriptor.MODE_READ_ONLY);
				fileInputStream = new FileInputStream(is.getFileDescriptor());
				length = fileInputStream.available();
			} catch (Exception ex) {
				Loge.e("file error");
				mUploadTask.handleDownloadState(HTTP_STATE_FAILED);
				return;
			}

			Loge.d("uploadPhoto length = " + length);

			int bufferLength = 512;
			byte buffer[] = new byte[bufferLength];
			int count = 0;
			try {
				while (fileInputStream.read(buffer) != -1) {
					count++;

					int endPos = count * bufferLength;

					if (endPos > length) {
						os.write(buffer, 0, bufferLength - endPos + length);
						mUploadTask.handleDownloadProgress(95);
					} else {
						os.write(buffer);
						int progress = (int) ((endPos / (float) length) * 90) + 5;
						mUploadTask.handleDownloadProgress(progress);
					}
				}
			} catch (IOException ex) {
				Loge.e("file error");
				mUploadTask.handleDownloadState(HTTP_STATE_FAILED);
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			try {
				fileInputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			mUploadTask.handleDownloadProgress(96);

			os.write(end_data);

			mUploadTask.handleDownloadProgress(97);

			os.flush();
			os.close();
			fileInputStream.close();

			mUploadTask.handleDownloadProgress(98);

			int code = httpConn.getResponseCode();
			InputStream is = null;

			mUploadTask.handleDownloadProgress(99);

			if (200 == code) {
				is = httpConn.getInputStream();
				mUploadTask.handleDownloadProgress(100);
			} else {
				is = httpConn.getErrorStream();
				mUploadTask.handleDownloadState(HTTP_STATE_FAILED);
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					"utf-8"));
			String str = null;
			StringBuffer strBuf = new StringBuffer("");
			while ((str = br.readLine()) != null) {
				strBuf.append(str);
			}
			File tmpFile = new File(mUploadTask.getFilePath());
			deleteFile(tmpFile);

			mUploadTask.handleDownloadState(HTTP_STATE_COMPLETED);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return;
	}

	public void deleteFile(File file) {
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					this.deleteFile(files[i]);
				}
				file.delete();
			}
		}
	}

}
