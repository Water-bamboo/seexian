package com.comic.seexian.view;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.comic.seexian.Constants;
import com.comic.seexian.Loge;
import com.comic.seexian.R;
import com.comic.seexian.sinaauth.SinaPoisData;
import com.comic.seexian.utils.SeeXianNetUtils;
import com.comic.seexian.utils.SeeXianUtils;

public class ImageUploadDialog extends Dialog {

	private Context mCtx;

	private Button uploadButton;

	private ImageView imageView;

	private ImageView imageDivider;

	private ProgressBar progressBar;

	private Spinner locationSpinner;

	private View buttonsPanel;

	private Handler mOuterHandler;

	double latitude, longitude;

	private SinaPoisData mSelectedItem = null;

	private ArrayList<SinaPoisData> locationData = new ArrayList<SinaPoisData>();

	private Handler mDataHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.MESSAGE_GAT_LOCATION_SUCCESS: {
				if (mSpinnerAdapter != null && locationData.size() > 0) {
					locationSpinner.setVisibility(View.VISIBLE);
					mSpinnerAdapter.notifyDataSetChanged();
				} else {
					locationSpinner.setVisibility(View.GONE);
				}
			}
				break;
			case Constants.MESSAGE_SHOW_TOKEN_WARN: {
				if (locationSpinner != null) {
					locationSpinner.setVisibility(View.GONE);
				}
				Toast.makeText(mCtx, R.string.plz_login_sina, Toast.LENGTH_LONG)
						.show();
			}
				break;
			case Constants.MESSAGE_NETWORK_ERROR: {
				if (locationSpinner != null) {
					locationSpinner.setVisibility(View.GONE);
				}
				if (mOuterHandler != null) {
					mOuterHandler.sendEmptyMessage(msg.what);
				}
			}
				break;
			default:
				break;
			}
			return false;
		}
	});

	public ImageUploadDialog(Context context, int theme, Handler handler) {
		super(context, theme);
		mCtx = context;
		mOuterHandler = handler;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_dialog);

		imageView = (ImageView) findViewById(R.id.upload_image);
		progressBar = (ProgressBar) findViewById(R.id.upload_progress);

		imageDivider = (ImageView) findViewById(R.id.upload_image_divider);
		buttonsPanel = (View) findViewById(R.id.upload_button_panel);
		uploadButton = (Button) findViewById(R.id.upload_button);
		locationSpinner = (Spinner) findViewById(R.id.upload_location_spinner);

		locationSpinner.setAdapter(mSpinnerAdapter);
		locationSpinner.setVisibility(View.INVISIBLE);
		locationSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						Loge.i("onItemSelected arg2 = " + arg2 + " arg3 = "
								+ arg3);
						if (locationData.size() > arg2) {

						}
						mSelectedItem = locationData.get(arg2);
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						if (locationData.size() > 0) {

						}
						mSelectedItem = locationData.get(0);
					}
				});

		uploadButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Loge.i("Post Button Clicked");
				if (mOuterHandler != null) {
					Message msg = mOuterHandler.obtainMessage(
							Constants.MSG_DO_UPLOAD_PHOTO, mSelectedItem);
					msg.sendToTarget();
					dismiss();
				}
			}
		});

	}

	public void SetImage(Bitmap image) {
		if (imageView != null && image != null) {
			imageView.setImageBitmap(image);
		}
	}

	public void SetProgressVisiblilty(int visibility) {
		if (progressBar != null) {
			progressBar.setVisibility(visibility);
		}
	}

	public void SetButtonPanelVisiblilty(int visibility) {
		if (imageDivider != null && buttonsPanel != null) {
			buttonsPanel.setVisibility(visibility);
			imageDivider.setVisibility(visibility);
		}
	}

	public void SetLocationPos(double lat, double lng) {
		latitude = lat;
		longitude = lng;

		new Thread(new Runnable() {
			@Override
			public void run() {

				if (!SeeXianUtils.isNetworkAvailable(mCtx)) {
					if (mDataHandler != null) {
						mDataHandler
								.sendEmptyMessage(Constants.MESSAGE_NETWORK_ERROR);
					}
					return;
				}

				String acctoken = SeeXianNetUtils.getAccessToken(mCtx);

				if (acctoken == null || acctoken.isEmpty()) {
					if (mDataHandler != null) {
						mDataHandler
								.sendEmptyMessage(Constants.MESSAGE_SHOW_TOKEN_WARN);
					}
					return;
				}

				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("lat", latitude);
				params.put("long", longitude);
				Object locationInfo = SeeXianNetUtils.getResult(
						"https://api.weibo.com/2/place/nearby/pois.json",
						params, acctoken);
				if(locationInfo != null){
					locationData.addAll(SeeXianNetUtils
							.getLocationPoisData(locationInfo));
				}

				if (locationData.size() > 0) {
					mSelectedItem = locationData.get(0);
				}

				if (mDataHandler != null) {
					mDataHandler
							.sendEmptyMessage(Constants.MESSAGE_GAT_LOCATION_SUCCESS);
				}
			}
		}).start();
	}

	BaseAdapter mSpinnerAdapter = new BaseAdapter() {

		@Override
		public int getCount() {
			return locationData.size();
		}

		@Override
		public Object getItem(int position) {
			return locationData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(mCtx.getApplicationContext())
						.inflate(R.layout.spinner_item_layout, null);
				holder = new ViewHolder();

				holder.text = (TextView) convertView
						.findViewById(R.id.spinner_item_text);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			SinaPoisData itemData = locationData.get(position);

			holder.text.setText(itemData.mAddress);

			return convertView;
		}

		class ViewHolder {
			TextView text;
		}

	};
}
