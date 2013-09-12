package com.comic.seexian.about;

import com.comic.seexian.R;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity {

    private TextView versionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.help_layout);
	versionText = (TextView) findViewById(R.id.version_text);

	String pkName = this.getPackageName();
	String versionName = "";
	try {
	    versionName = this.getPackageManager().getPackageInfo(pkName, 0).versionName;
	} catch (NameNotFoundException e) {
	    e.printStackTrace();
	}

	versionText.setText(versionName);

    }

    @Override
    protected void onResume() {
	super.onResume();
    }

}
