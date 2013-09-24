package com.comic.seexian.utils;

import java.util.Date;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateFormat;

import com.comic.seexian.Loge;
import com.comic.seexian.R;

public class SeeXianUtils {

	enum Month {
		Jan, Feb, Apr, May, Mar, Jun, Jul, Aug, Sep, Oct, Nov, Dec
	}

	enum Week {
		Mon, Tue, Web, Thu, Fri, Sat
	}

	static public boolean isLocationProvideOn(Context ctx) {
		final LocationManager locationMgr = (LocationManager) ctx
				.getSystemService(Context.LOCATION_SERVICE);
		boolean gps_enable = locationMgr
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean network_enable = locationMgr
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		return gps_enable || network_enable;
	};

	static public boolean isNetworkAvailable(Context ctx) {
		final ConnectivityManager connectivity = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			final NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null) {
				return true;
			}
		}
		return false;
	}

	static public String formatDateTime(Context ctx, String date) {
		/*
		 * Date as: Tue Sep 03 19:12:43 +0800 2013
		 */
		String[] times = date.split(" ");

		Loge.i("Origin Data : " + date);
		Loge.i("times.length : " + times.length);

		StringBuffer sbDate = new StringBuffer("");

		if (times.length < 6) {
			return null;
		}

		Date finalDate = new Date();

		Month mKey = Month.valueOf(times[1]);

		switch (mKey) {
		case Jan:
			finalDate.setMonth(1);
			break;
		case Feb:
			finalDate.setMonth(2);
			break;
		case Apr:
			finalDate.setMonth(3);
			break;
		case May:
			finalDate.setMonth(4);
			break;
		case Mar:
			finalDate.setMonth(5);
			break;
		case Jun:
			finalDate.setMonth(6);
			break;
		case Jul:
			finalDate.setMonth(7);
			break;
		case Aug:
			finalDate.setMonth(8);
			break;
		case Sep:
			finalDate.setMonth(9);
			break;
		case Oct:
			finalDate.setMonth(10);
			break;
		case Nov:
			finalDate.setMonth(11);
			break;
		case Dec:
			finalDate.setMonth(12);
			break;
		default:
			break;
		}

		String day = times[2];
		Loge.i("Data day : " + day);
		if (day.startsWith("0")) {
			day = day.substring(1);
		}

		finalDate.setDate(Integer.parseInt(day));

		String[] dayTimes = times[3].split(":");

		for (int i = 0; i < dayTimes.length; i++) {
			switch (i) {
			case 0:
				finalDate.setHours(Integer.parseInt(dayTimes[0]));
				break;
			case 1:
				finalDate.setMinutes(Integer.parseInt(dayTimes[1]));
				break;
			case 2:
				finalDate.setSeconds(Integer.parseInt(dayTimes[2]));
				break;
			default:
				break;
			}
		}

		finalDate.setYear(Integer.valueOf(times[5]));

		sbDate.append(finalDate.getMonth());
		sbDate.append(ctx.getResources().getString(R.string.month));
		sbDate.append(finalDate.getDate());
		sbDate.append(ctx.getResources().getString(R.string.day));
		sbDate.append(" ");
		sbDate.append(finalDate.getHours());
		sbDate.append(":");
		if(finalDate.getMinutes() == 0){
			sbDate.append("00");
		}else{
			sbDate.append(finalDate.getMinutes());
		}

		finalDate = null;

		Loge.i("Data String : " + sbDate.toString());

		return sbDate.toString();
	}

}
