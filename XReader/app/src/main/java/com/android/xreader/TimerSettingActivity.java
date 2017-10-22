package com.android.xreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.xreader.utils.FusionField;


public class TimerSettingActivity extends Activity {

	TextView timer_desc;
	ListView timer_list;
	private String[] mListStr = {"5分钟","10分钟","15分钟","30分钟","1小时","1.5小时","2小时"};
	int timer_setting = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_timer);
		initView();

	}

	private void initView(){
		timer_desc = (TextView)findViewById(R.id.timer_desc);
		timer_list = (ListView)findViewById(R.id.timer_list);
		timer_list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mListStr));
		timer_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				Toast.makeText(TimerSettingActivity.this,"您选择了" + mListStr[position], Toast.LENGTH_LONG).show();
				timer_setting = position;
			}
		});
	}

	@Override
	public void onBackPressed() {
			//super.onBackPressed();
			Intent intent = new Intent();
			intent.putExtra(MainActivity.TIMER_KEY, timer_setting);
			setResult(MainActivity.TIMER_CODE, intent);
			finish();
	}



}
