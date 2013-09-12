package com.comic.seexian.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import com.comic.seexian.R;

public class NetworkDialog extends Dialog {

    private Context mCtx;

    private Button btn_cancel, btn_settings;

    public NetworkDialog(Context context, int theme) {
	super(context, theme);
	mCtx = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.net_dialog);

	btn_cancel = (Button) findViewById(R.id.net_cancel);
	btn_settings = (Button) findViewById(R.id.net_settings);

	btn_cancel.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		dismiss();
	    }
	});

	btn_settings.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		Intent intent = new Intent(Settings.ACTION_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mCtx.startActivity(intent);
	    }
	});
    }

}
