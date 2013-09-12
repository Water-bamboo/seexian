package com.comic.seexian.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.comic.seexian.Loge;
import com.comic.seexian.history.UserHistoryData;
import com.comic.seexian.sinaauth.AccessTokenKeeper;
import com.comic.seexian.sinaauth.ConstantsSina;
import com.comic.seexian.sinaauth.SinaPoisData;
import com.weibo.sdk.android.Oauth2AccessToken;

public class SeeXianNetUtils {

	private static HttpClient sHttpClient = null;

	public static final int HTTP_TIMEOUT = 60000;

	public static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android "
			+ android.os.Build.VERSION.RELEASE + ";"
			+ Locale.getDefault().toString() + "; " + android.os.Build.DEVICE
			+ "/" + android.os.Build.ID + ")";

	public static Object postResult(String api_url,
			HashMap<String, Object> params, String access_token) {
		final HttpPost post = new HttpPost(api_url);
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
		params.put("access_token", access_token);
		String queryParams = buildParams(params, "&");
		final String entityString = queryParams;
		Loge.d("postResult entityString: " + entityString);
		if (sHttpClient == null) {
			initHttpClient();
		}
		HttpEntity entity;
		try {
			entity = new StringEntity(entityString);
			post.setEntity(entity);
			final HttpResponse r = sHttpClient.execute(post);
			return getParseResult(r);
		} catch (UnknownHostException e) {
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		} catch (ConcurrentModificationException e) {
		} catch (JSONException e) {
		}
		return null;
	}

	public static String buildParams(HashMap<String, Object> params,
			String splitter) {
		StringBuffer buf = new StringBuffer();
		if (params == null) {
			params = new HashMap<String, Object>();
		}
		Iterator<String> itrs = params.keySet().iterator();
		List<String> sortKeyList = new ArrayList<String>();
		while (itrs.hasNext()) {
			sortKeyList.add(itrs.next());
		}
		Collections.sort(sortKeyList);
		for (String key : sortKeyList) {
			if (buf.length() != 0) {
				buf.append(splitter);
			}
			buf.append(key).append("=");
			buf.append(params.get(key));
		}
		return buf.toString();
	}

	private static void initHttpClient() {
		final HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUseExpectContinue(params, true);
		HttpProtocolParams.setUserAgent(params, USER_AGENT);

		HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);

		final SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
		sslSocketFactory.setHostnameVerifier(new AllowAllHostnameVerifier());
		registry.register(new Scheme("https", sslSocketFactory, 443));

		final ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(
				params, registry);

		sHttpClient = new DefaultHttpClient(manager, params);
	}

	public static Object getParseResult(HttpResponse r) throws JSONException,
			IOException {
		Header header = r.getFirstHeader("WWW-Authenticate");
		if (header != null) {
			String value = header.getValue();
			Loge.e("header value=" + value);
			if (value.contains("expired_token")) {
				Loge.e("expired_token");
			}
			if (value.contains("invalid_token")) {
				Loge.e("invalid_token");
			}
		}
		final int status = r.getStatusLine().getStatusCode();
		Loge.d("status=" + status);

		if (status != HttpURLConnection.HTTP_OK
				&& status != HttpURLConnection.HTTP_CREATED) {
			String content = getResponse(r.getEntity());
			if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
				if (isRefreshTokenExpired(content)) {
					Loge.e("SESSION_ESPIRED");
					return null;
				}
			}
			Loge.e("SERVER_ERROR");
			return null;
		}
		String content = getResponse(r.getEntity());

		Loge.d("getParseResult " + content);

		if ("{}".equals(content)) {
			return null;
		} else if (content.startsWith("{") && content.endsWith("}")) {
			Loge.d("return JSONObject");
			final JSONObject obj = new JSONObject(content);
			return obj;
		} else if (content.startsWith("[") && content.endsWith("]")) {
			Loge.d("return JSONArray");
			return new JSONArray(content);
		} else if (content.startsWith("<") && content.endsWith(">"))
			Loge.e("service not available");
		else if (content.startsWith("\"") && content.endsWith("\"")) {
			Loge.d("return String");
			content = content.substring(1, content.length() - 1);
			return content;
		} else {
			return content;
		}
		return null;
	}

	private static String getResponse(HttpEntity entity)
			throws UnsupportedEncodingException, IllegalStateException,
			IOException {
		String response = "";

		int length = (int) entity.getContentLength();

		if (length < 1) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			entity.writeTo(out);
			return out.toString();
		}
		StringBuffer sb = new StringBuffer(length);
		InputStreamReader isr;

		if (entity.getContentEncoding() != null
				&& entity.getContentEncoding().getValue().equals("gzip")) {
			isr = new InputStreamReader(
					new GZIPInputStream(entity.getContent()), "UTF-8");
		} else {
			isr = new InputStreamReader(entity.getContent(), "UTF-8");
		}

		char buff[] = new char[length];
		int cnt;
		while ((cnt = isr.read(buff, 0, length - 1)) > 0) {
			sb.append(buff, 0, cnt);
		}

		response = sb.toString();
		isr.close();
		sb = null;
		buff = null;

		return response;
	}

	private static boolean isRefreshTokenExpired(String content)
			throws JSONException {
		if (content.startsWith("{") && content.endsWith("}")) {
			final JSONObject obj = new JSONObject(content);
			if (!obj.isNull("error")) {
				String error = obj.getString("error");
				if ("invalid_grant".equals(error)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Object uploadPhoto(Context context, String api_url,
			Uri filePath, String access_token) {
		FileInputStream fileInputStream = null;
		int length = 0;
		Loge.d("uploadPhoto path = " + filePath.toString());
		File file = null;
		try {
			file = new File(filePath.toString());
			ParcelFileDescriptor is = ParcelFileDescriptor.open(file,
					ParcelFileDescriptor.MODE_READ_ONLY);
			fileInputStream = new FileInputStream(is.getFileDescriptor());
			length = fileInputStream.available();
		} catch (Exception ex) {
			Loge.e("file error");
			return null;
		}

		byte buffer[] = new byte[length];
		try {
			fileInputStream.read(buffer);
		} catch (IOException ex) {
			Loge.e("file error");
			try {
				fileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		try {
			fileInputStream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Loge.d("photo size = " + length);

		final HttpPost post = new HttpPost(api_url);

		post.setHeader("Content-Type", "multipart/form-data");

		if (sHttpClient == null) {
			initHttpClient();
		}

		ByteArrayEntity entity = null;

		try {
			entity = new ByteArrayEntity(buffer);
			post.setEntity(entity);
			final HttpResponse r = sHttpClient.execute(post);
			return getParseResult(r);
		} catch (UnknownHostException e) {
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		} catch (ConcurrentModificationException e) {
		} catch (JSONException e) {
		}

		return null;
	}

	public static String uploadPhoto(Context context, String api_url,
			HashMap<String, Object> params, Uri filePath, String access_token) {

		try {
			URL url = new URL(api_url);
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
			stringBuffer.append(access_token + "\r\n");

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

			OutputStream os = httpConn.getOutputStream();
			os.write(data);

			FileInputStream fileInputStream = null;
			int length = 0;
			Loge.d("uploadPhoto path = " + filePath.toString());
			File file = null;
			try {
				file = new File(filePath.toString());
				ParcelFileDescriptor is = ParcelFileDescriptor.open(file,
						ParcelFileDescriptor.MODE_READ_ONLY);
				fileInputStream = new FileInputStream(is.getFileDescriptor());
				length = fileInputStream.available();
			} catch (Exception ex) {
				Loge.e("file error");
				return null;
			}

			Loge.d("uploadPhoto length = " + length);

			byte buffer[] = new byte[length];
			try {
				fileInputStream.read(buffer);
			} catch (IOException ex) {
				Loge.e("file error");
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
			try {
				fileInputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			os.write(buffer, 0, length);

			os.write(end_data);
			os.flush();
			os.close();
			fileInputStream.close();

			int code = httpConn.getResponseCode();
			InputStream is = null;

			if (200 == code) {
				is = httpConn.getInputStream();
			} else {
				is = httpConn.getErrorStream();
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					"utf-8"));
			String str = null;
			StringBuffer strBuf = new StringBuffer("");
			while ((str = br.readLine()) != null) {
				strBuf.append(str);
			}
			return strBuf.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static Object getResult(String api_url,
			HashMap<String, Object> params, String access_token) {

		Loge.d("api_url = " + api_url);

		if (params == null) {
			params = new HashMap<String, Object>();
		}

		params.put("access_token", access_token);
		String queryParams = buildParams(params, "&");
		final String entityString = queryParams;

		StringBuffer requestUrl = new StringBuffer(api_url);
		requestUrl.append("?");
		requestUrl.append(entityString);

		Loge.d("requestUrl = " + requestUrl.toString());

		HttpGet get = new HttpGet(requestUrl.toString());

		if (sHttpClient == null) {
			initHttpClient();
		}
		try {
			Loge.d("Do get weibo");
			final HttpResponse r = sHttpClient.execute(get);
			return getParseResult(r);
		} catch (UnknownHostException e) {
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		} catch (ConcurrentModificationException e) {
		} catch (JSONException e) {
		}
		return null;
	}

	public static String reissueAccessToken(Context context, String code) {
		Loge.d("reissueAccessToken");
		Loge.d("code = " + code);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("client_id", ConstantsSina.APP_KEY);
		params.put("client_secret", ConstantsSina.APP_SECRET);
		params.put("grant_type", "authorization_code");
		params.put("code", code);
		params.put("redirect_uri", ConstantsSina.REDIRECT_URL);

		Object obj = postResult("https://api.weibo.com/oauth2/access_token",
				params, null);

		String token = null;
		String expireTime = null;

		if (obj instanceof JSONObject) {
			JSONObject jsonObj = (JSONObject) obj;

			try {
				if (!jsonObj.isNull("access_token")) {
					token = jsonObj.getString("access_token");
				}
				if (!jsonObj.isNull("expires_in")) {
					expireTime = jsonObj.getString("expires_in");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if (token != null && expireTime != null) {
			Loge.d("token = " + token);
			Loge.d("expires_in = " + expireTime);
			Oauth2AccessToken oToken = new Oauth2AccessToken(token, expireTime);
			AccessTokenKeeper.keepAccessToken(context, oToken);
		}

		return token;
	}

	public static Object getLocationPoint() {
		return null;
	}

	public static String getAccessToken(Context context) {
		Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(context
				.getApplicationContext());
		Loge.d("getAccessToken Token = " + token.getToken());
		Loge.d("getAccessToken ExpiresTime = " + token.getExpiresTime());

		String acctoken = token.getToken();

		if (acctoken == null || acctoken.isEmpty()) {
			String code = AccessTokenKeeper.readAccessCode(context
					.getApplicationContext());

			Loge.d("getAccessToken code = " + code);
			if (code != null && !code.isEmpty()) {
				acctoken = SeeXianNetUtils.reissueAccessToken(
						context.getApplicationContext(), code);
			}

		}

		if (acctoken == null || acctoken.isEmpty()) {
			return null;
		}
		return acctoken;
	}

	public static ArrayList<SinaPoisData> getLocationPoisData(Object oPoisData) {
		ArrayList<SinaPoisData> locationListData = new ArrayList<SinaPoisData>();

		try {
			JSONObject jPoisData = new JSONObject(oPoisData.toString());
			JSONArray jPois = jPoisData.getJSONArray("pois");

			Loge.i("JSONArray length = " + jPois.length());
			for (int i = 0; i < jPois.length(); i++) {
				JSONObject jPoisItem = jPois.getJSONObject(i);

				SinaPoisData item = new SinaPoisData();
				item.mPoiid = jPoisItem.getString("poiid");
				item.mAddress = jPoisItem.getString("address");
				item.mTitle = jPoisItem.getString("title");
				item.mLat = jPoisItem.getString("lat");
				item.mLong = jPoisItem.getString("lon");

				Loge.i("Item Address = " + item.mAddress);
				Loge.i("Item Lat = " + item.mLat + " Lon = " + item.mLong);

				locationListData.add(item);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return locationListData;
	}

	public static ArrayList<UserHistoryData> getUserHistoryData(Object oData) {
		ArrayList<UserHistoryData> userHistoryDataList = new ArrayList<UserHistoryData>();

		try {
			JSONObject jUserData = new JSONObject(oData.toString());
			JSONArray jPosts = jUserData.getJSONArray("statuses");
			Loge.i("JSONArray length = " + jPosts.length());
			for (int i = 0; i < jPosts.length(); i++) {
				JSONObject jPostItem = jPosts.getJSONObject(i);

				UserHistoryData item = new UserHistoryData();
				item.mTime = jPostItem.getString("created_at");
				item.mPostId = jPostItem.getString("idstr");
				if (jPostItem.has("text"))
					item.mText = jPostItem.getString("text");
				if (jPostItem.has("source"))
					item.mSource = jPostItem.getString("source");
				if (jPostItem.has("thumbnail_pic"))
					item.mThumbPic = jPostItem.getString("thumbnail_pic");
				if (jPostItem.has("original_pic"))
					item.mOriPic = jPostItem.getString("original_pic");

				try {
					JSONObject jGeoItem = jPostItem.getJSONObject("geo");
					String geoPo = jGeoItem.getString("coordinates");
					/*
					 * GEO data format: [31.1999323,121.6044558]
					 */
					geoPo = geoPo.substring(1, geoPo.length() - 1);
					String[] pos = geoPo.split(",");
					item.mLat = pos[0];
					item.mLng = pos[1];

				} catch (JSONException e) {
					Loge.w("NOT GEO DATA");
				}
				userHistoryDataList.add(item);

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return userHistoryDataList;
	}

}
