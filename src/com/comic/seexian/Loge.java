package com.comic.seexian;

import android.util.Log;

public class Loge {
    private static final String LOG_TAG = "SeeXian";
    public static final boolean LOG_STATUS = true;
    private static String tag = "SeeXian";

    private static Loge l = new Loge();

    private Loge() {
    }

    // tag
    static public void d(String tag, String info) {
	if (LOG_STATUS)
	    Log.d(LOG_TAG, "[" + tag + "]: " + info);
    }

    static public void e(String tag, String info) {
	if (LOG_STATUS)
	    Log.e(LOG_TAG, "[" + tag + "]: " + info);
    }

    static public void e(String tag, String info, Throwable err) {
	if (LOG_STATUS)
	    Log.e(LOG_TAG, "[" + tag + "]: " + info, err);
    }

    static public void i(String tag, String info) {
	if (LOG_STATUS)
	    Log.i(LOG_TAG, "[" + tag + "]: " + info);
    }

    static public void v(String tag, String info) {
	if (LOG_STATUS)
	    Log.v(LOG_TAG, "[" + tag + "]: " + info);
    }

    static public void v(String tag, String info, Throwable err) {
	if (LOG_STATUS)
	    Log.v(LOG_TAG, "[" + tag + "]: " + info, err);
    }

    static public void w(String tag, String info, Throwable err) {
	if (LOG_STATUS)
	    Log.w(LOG_TAG, "[" + tag + "]: " + info, err);
    }

    static public void w(String tag, String info) {
	if (LOG_STATUS)
	    Log.w(LOG_TAG, "[" + tag + "]: " + info);
    }

    // no tag
    public static void d(String info) {

	if (LOG_STATUS) {
	    tag = l.getFunctionName();
	    Log.d(LOG_TAG, "[" + tag + "]: " + info);
	}
    }
    
    public static void w(String info) {
	if (LOG_STATUS) {
	    tag = l.getFunctionName();
	    Log.w(LOG_TAG, "[" + tag + "]: " + info);
	}
    }

    public static void e(String info) {
	if (LOG_STATUS) {
	    tag = l.getFunctionName();
	    Log.e(LOG_TAG, "[" + tag + "]: " + info);
	}
    }

    public static void e(String info, Throwable err) {
	if (LOG_STATUS) {
	    tag = l.getFunctionName();
	    Log.e(LOG_TAG, "[" + tag + "]: " + info, err);
	}
    }

    public static void i(String info) {
	if (LOG_STATUS) {
	    tag = l.getFunctionName();
	    Log.i(LOG_TAG, "[" + tag + "]: " + info);
	}
    }

    public static void v(String info) {
	if (LOG_STATUS) {
	    tag = l.getFunctionName();
	    Log.v(LOG_TAG, "[" + tag + "]: " + info);
	}
    }

    public static void w(String info, Throwable err) {
	if (LOG_STATUS) {
	    tag = l.getFunctionName();
	    Log.w(LOG_TAG, "[" + tag + "]: " + info, err);
	}
    }

    // force show log
    static public void sd(String tag, String info) {
	Log.d(LOG_TAG, "[" + tag + "]: " + info);
    }

    static public void se(String tag, String info) {
	Log.e(LOG_TAG, "[" + tag + "]: " + info);
    }

    static public void se(String tag, String info, Throwable err) {
	Log.e(LOG_TAG, "[" + tag + "]: " + info, err);
    }

    static public void si(String tag, String info) {
	Log.i(LOG_TAG, "[" + tag + "]: " + info);
    }

    static public void sv(String tag, String info) {
	Log.v(LOG_TAG, "[" + tag + "]: " + info);
    }

    static public void sv(String tag, String info, Throwable err) {
	Log.v(LOG_TAG, "[" + tag + "]: " + info, err);
    }

    static public void sw(String tag, String info) {
	Log.w(LOG_TAG, "[" + tag + "]: " + info);
    }

    static public void sw(String tag, Throwable err) {
	Log.w(LOG_TAG, "[" + tag + "]: ", err);
    }

    static public void sw(String tag, String info, Throwable err) {
	Log.w(LOG_TAG, "[" + tag + "]: " + info, err);
    }

    private String getFunctionName() {
	StackTraceElement[] sts = Thread.currentThread().getStackTrace();
	if (sts == null) {
	    return null;
	}
	for (StackTraceElement st : sts) {
	    if (st.isNativeMethod()) {
		continue;
	    }
	    if (st.getClassName().equals(Thread.class.getName())) {
		continue;
	    }
	    if (st.getClassName().equals(this.getClass().getName())) {
		continue;
	    }
	    return "*" + st.getFileName() + ":" + st.getLineNumber() + "/"
		    + st.getMethodName() + "*";
	}
	return null;
    }
}
