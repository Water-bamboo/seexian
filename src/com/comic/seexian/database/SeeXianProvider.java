package com.comic.seexian.database;

import com.comic.seexian.Loge;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class SeeXianProvider extends ContentProvider {

	private SeeXianDBHelper mOpenHelper;

	/** Database filename */
	private static final String DB_NAME = "seexian.db";
	/** Current database version */
	private static final int DB_VERSION = 101;

	/** Name of table in the database */
	private static final String DB_TABLE_LANDSCAPE = "landscape";

	public static final Uri CONTENT_URI_SEE_XIAN_LANDSCAPE = Uri
			.parse("content://com.comic.seexian/seexian");

	public static final String KEY_ID = "_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_PRICE = "price";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_ICON = "icon";
	public static final String KEY_TEL = "telepnone";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_LINK_URL = "link";
	public static final String KEY_PROIVDER = "provider";
	public static final String KEY_POST_ID = "postid";

	/** Name of table in the database */
	private static final String DB_TABLE_USER_POST = "userdata";

	public static final Uri CONTENT_URI_SEE_XIAN_USER_POST = Uri
			.parse("content://com.comic.seexian/userdata");

	public static final String KEY_USER_ID = "_id";
	public static final String KEY_USER_POST_ID = "postid";
	public static final String KEY_USER_POS_NAME = "posname";
	public static final String KEY_USER_TEXT = "text";
	public static final String KEY_USER_SOURCE = "source";
	public static final String KEY_USER_ORIGIN_PIC = "image";
	public static final String KEY_USER_THUMB_PIC = "thumbnail";
	public static final String KEY_USER_TIME = "time";
	public static final String KEY_USER_LATITUDE = "latitude";
	public static final String KEY_USER_LONGITUDE = "longitude";
	public static final String KEY_USER_LANDSCAPE_ID = "landscapeid";
	public static final String KEY_USER_DISATACE_TO_XIAN = "distance";

	@Override
	public boolean onCreate() {
		Loge.d("onCreate SeeXianProvider");
		Context context = getContext();
		mOpenHelper = new SeeXianDBHelper(context);
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(getTable(uri));

		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = KEY_ID;
		} else {
			orderBy = sortOrder;
		}

		Cursor c = null;

		try {
			c = qb.query(db, projection, selection, selectionArgs, null, null,
					orderBy);
			c.setNotificationUri(getContext().getContentResolver(), uri);
		} catch (SQLiteDiskIOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return c;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		String table_name = getTable(uri);

		long rowID = 0;
		try {
			rowID = db.insert(table_name, KEY_ICON, values);
		} catch (SQLiteDiskIOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return uri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		String table_name = getTable(uri);

		int count = 0;
		try {
			count = db.delete(table_name, selection, selectionArgs);
		} catch (SQLiteDiskIOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		String table_name = getTable(uri);

		int count = 0;
		try {
			count = db.update(table_name, values, selection, selectionArgs);
		} catch (SQLiteDiskIOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	@Override
	public String getType(Uri uri) {
		return "vnd.android.cursor.dir/vnd.comic.seexian";
	}

	private String getTable(Uri uri) {
		String sUri = uri.toString();
		if (sUri.equals(CONTENT_URI_SEE_XIAN_LANDSCAPE.toString())) {
			return DB_TABLE_LANDSCAPE;
		} else {
			return DB_TABLE_USER_POST;
		}
	}

	public class SeeXianDBHelper extends SQLiteOpenHelper {

		public SeeXianDBHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String commandLandscape = "create table " + DB_TABLE_LANDSCAPE
					+ " (" + KEY_ID + " integer primary key autoincrement, "
					+ KEY_NAME + " TEXT," + KEY_PRICE + " TEXT,"
					+ KEY_DESCRIPTION + " TEXT," + KEY_ICON + " TEXT, "
					+ KEY_TEL + " TEXT, " + KEY_ADDRESS + " TEXT, "
					+ KEY_LINK_URL + " TEXT, " + KEY_PROIVDER + " TEXT, "
					+ KEY_POST_ID + " TEXT );";
			Loge.v("create database Landscape = " + commandLandscape);
			db.execSQL(commandLandscape);

			String commandUser = "create table " + DB_TABLE_USER_POST + " ("
					+ KEY_USER_ID + " integer primary key autoincrement, "
					+ KEY_USER_POST_ID + " TEXT," + KEY_USER_POS_NAME
					+ " TEXT," + KEY_USER_TEXT + " TEXT," + KEY_USER_SOURCE
					+ " TEXT," + KEY_USER_ORIGIN_PIC + " TEXT, "
					+ KEY_USER_THUMB_PIC + " TEXT, " + KEY_USER_TIME
					+ " TEXT, " + KEY_USER_LATITUDE + " TEXT, "
					+ KEY_USER_LONGITUDE + " TEXT, "
					+ KEY_USER_DISATACE_TO_XIAN + " TEXT, "
					+ KEY_USER_LANDSCAPE_ID + " INTEGER );";
			Loge.v("create database User = " + commandUser);
			db.execSQL(commandUser);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Loge.v("upgrade database");
			db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_LANDSCAPE);
			db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_USER_POST);
		}

	}

}
