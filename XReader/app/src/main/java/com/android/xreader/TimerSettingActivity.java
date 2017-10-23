package com.android.xreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.xreader.utils.FusionField;


public class TimerSettingActivity extends Activity {

	TextView timer_desc;
	ListView timer_list;
	Button cancel_btn;
	private String[] mListStr = {"1分钟","5分钟","10分钟","15分钟","30分钟","1小时","1.5小时","2小时"};
	private int[] times = {1,5,10,15,30,60,90,120};
	int timer_setting = -1;
	int timer_minute = 0;

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
				timer_minute = times[timer_setting];
				Intent intent = new Intent();
				intent.putExtra(MainActivity.TIMER_KEY, timer_minute);
				setResult(MainActivity.TIMER_CODE, intent);
				finish();
			}
		});
		cancel_btn = (Button)findViewById(R.id.cancel_btn);
		cancel_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Toast.makeText(TimerSettingActivity.this,"您取消了定时", Toast.LENGTH_LONG).show();
				Intent intent = new Intent();
				intent.putExtra(MainActivity.TIMER_KEY, 0);
				setResult(MainActivity.TIMER_CODE, intent);
				finish();
			}
		});
	}

	@Override
	public void onBackPressed() {
			//super.onBackPressed();
			Intent intent = new Intent();
			//intent.putExtra(MainActivity.TIMER_KEY, timer_minute);
			setResult(MainActivity.TIMER_CODE, intent);
			finish();
	}



}
