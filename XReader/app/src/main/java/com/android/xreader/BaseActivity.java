package com.android.xreader;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.android.xreader.utils.FusionField;


public class BaseActivity extends Activity {

	protected TextView tvHead;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		FusionField.baseActivity = this;
	}

	protected TextView getHeadTextView(){
		if(tvHead==null){
			tvHead = (TextView) findViewById(R.id.tv_head);
		}
		return tvHead;
	}
}
