package com.android.xreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.android.xreader.utils.FusionField;


public class LoadBookActivity extends Activity {

	public static final int OK = 0;
	protected TextView tvHead;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_loadbook);

	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtra("result","file");
		setResult(OK,intent);
		finish();
	}
}
