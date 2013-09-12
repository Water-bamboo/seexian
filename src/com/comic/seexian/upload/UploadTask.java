package com.comic.seexian.upload;

import java.util.HashMap;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.comic.seexian.Loge;
import com.comic.seexian.upload.UploadRunnable.TaskRunnableUploadMethods;

public class UploadTask implements TaskRunnableUploadMethods {

    private String mFilePath;

    private HashMap<String, Object> mParams;

    private Thread mCurrentThread;

    private Runnable mUploadRunnable;

    private Handler mHandler;

    private Context mCtx;

    private int mProgress = -1;

    public UploadTask() {
	mUploadRunnable = new UploadRunnable(this);
    }

    public void initUploadTask(Context ctx, String filePath,
	    HashMap<String, Object> params, Handler outHandler) {
	mParams = new HashMap<String, Object>();
	if (params != null) {
	    mParams.putAll(params);
	}
	mFilePath = filePath;
	mHandler = outHandler;
	mCtx = ctx;
    }

    public Thread getCurrentThread() {
	return mCurrentThread;
    }

    public void setCurrentThread(Thread thread) {
	mCurrentThread = thread;
    }

    Runnable getHTTPUploadRunnable() {
	return mUploadRunnable;
    }

    @Override
    public void setDownloadThread(Thread currentThread) {
	setCurrentThread(currentThread);
    }

    @Override
    public void handleDownloadState(int state) {
	Loge.d("State = " + state);
	if (mHandler == null) {
	    return;
	}
	switch (state) {
	case UploadRunnable.HTTP_STATE_COMPLETED:
	    Message completeMessage = mHandler
		    .obtainMessage(UploadRunnable.HTTP_STATE_COMPLETED, this);
	    completeMessage.sendToTarget();
	    break;
	case UploadRunnable.HTTP_STATE_FAILED:
	    Message errorMessage = mHandler
		    .obtainMessage(UploadRunnable.HTTP_STATE_FAILED, this);
	    errorMessage.sendToTarget();
	    break;
	case UploadRunnable.TOKEN_ERROR:
	    Message tokenErrorMessage = mHandler
		    .obtainMessage(UploadRunnable.TOKEN_ERROR, this);
	    tokenErrorMessage.sendToTarget();
	    break;
	default:
	    mHandler.sendEmptyMessage(UploadRunnable.HTTP_STATE_STARTED);
	    break;
	}
    }

    @Override
    public void handleDownloadProgress(int progress) {
	if (mProgress == progress) {
	    return;
	}
	Loge.d("Progress = " + progress);
	if (mHandler == null) {
	    return;
	}
	mProgress = progress;
	Message msg = new Message();
	msg.what = UploadRunnable.HTTP_PROGRESS;
	msg.arg1 = progress;
	mHandler.sendMessage(msg);
    }

    @Override
    public String getFilePath() {
	return mFilePath;
    }

    @Override
    public HashMap<String, Object> getParams() {
	return mParams;
    }

    @Override
    public Context getContext() {
	return mCtx;
    }

}
