package com.android.xreader.tts;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.xreader.MainActivity;
import com.android.xreader.R;
import com.android.xreader.utils.SharedPreferencesUtils;


/**
 * 合成设置界面
 */
public class TtsSettings extends PreferenceActivity implements OnPreferenceChangeListener ,
						Preference.OnPreferenceClickListener,SeekBar.OnSeekBarChangeListener{

	private Preference mSpeedPreference;
	private Preference mPitchPreference;
	private Preference mVolumePreference;
	private ListPreference speekerPreference;

	private ProgressDialog progress;

	private View spd_pop, spc_pop, vol_pop;
	private PopupWindow mSpdPop, mSpcPop, mVolPop;
	TextView title_spd,title_spc,title_vol;
	SeekBar bar_spd,bar_spc,bar_vol;
	private boolean mIsnPopupWindowShowing = false;

	String speeker = "";
	int speed,pitch,volume;

	boolean isSettingChanged = false;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.tts_setting);

		speed = Integer.parseInt((String) SharedPreferencesUtils.getParam(this,"speed_preference","50"));
		pitch = Integer.parseInt((String) SharedPreferencesUtils.getParam(this,"pitch_preference","50"));
		volume = Integer.parseInt((String) SharedPreferencesUtils.getParam(this,"volume_preference","100"));
		speeker = (String) SharedPreferencesUtils.getParam(this,"speeker","xiaoyan");

		setPop();

		mSpeedPreference =findPreference("speed_preference");
		mSpeedPreference.setOnPreferenceClickListener(this);

		mPitchPreference = findPreference("pitch_preference");
		mPitchPreference.setOnPreferenceClickListener(this);

		mVolumePreference = findPreference("volume_preference");
		mVolumePreference.setOnPreferenceClickListener(this);

		mSpeedPreference.setSummary("当前值：" + speed);
		mPitchPreference.setSummary("当前值：" + pitch);
		mVolumePreference.setSummary("当前值：" + volume);

		speekerPreference = (ListPreference) findPreference("speeker_preference");
		CharSequence[] entries=speekerPreference.getEntries();
		int index=speekerPreference.findIndexOfValue((String)speeker);
		speekerPreference.setSummary(entries[index]);
		speekerPreference.setOnPreferenceChangeListener(this);
		speekerPreference.setOnPreferenceClickListener(this);

	}

	private void setPop() {
		int subPopUpWindowHeigt = getResources().getDimensionPixelSize(R.dimen.sub_popup_widow_height);

		spd_pop = this.getLayoutInflater().inflate(R.layout.speed_seekbar, null);// 语速
		title_spd = (TextView)spd_pop.findViewById(R.id.title);
		title_spd.setText("请输入语速:(0-100)");
		bar_spd = (SeekBar)spd_pop.findViewById(R.id.seekbar);
		bar_spd.setProgress(speed);
		bar_spd.setOnSeekBarChangeListener(this);
		mSpdPop = new PopupWindow(spd_pop, ViewGroup.LayoutParams.MATCH_PARENT, subPopUpWindowHeigt);

		spc_pop = this.getLayoutInflater().inflate(R.layout.speed_seekbar, null);// 语速
		title_spc = (TextView)spc_pop.findViewById(R.id.title);
		title_spc.setText("请输入音调(0-100)");
		bar_spc = (SeekBar)spc_pop.findViewById(R.id.seekbar);
		bar_spc.setProgress(pitch);
		bar_spc.setOnSeekBarChangeListener(this);
		mSpcPop = new PopupWindow(spc_pop, ViewGroup.LayoutParams.MATCH_PARENT, subPopUpWindowHeigt);

		vol_pop = this.getLayoutInflater().inflate(R.layout.speed_seekbar, null);// 语速
		title_vol = (TextView)vol_pop.findViewById(R.id.title);
		title_vol.setText("请输入音量(0-100)");
		bar_vol = (SeekBar)vol_pop.findViewById(R.id.seekbar);
		bar_vol.setProgress(volume);
		bar_vol.setOnSeekBarChangeListener(this);
		mVolPop = new PopupWindow(vol_pop, ViewGroup.LayoutParams.MATCH_PARENT, subPopUpWindowHeigt);

	}

	private void hideSeekbar() {
		if(mIsnPopupWindowShowing){
			mSpdPop.dismiss();
			mSpcPop.dismiss();
			mVolPop.dismiss();
			mIsnPopupWindowShowing = false;
		}
	}

	@Override
	public void onBackPressed() {
		if(mIsnPopupWindowShowing){
			hideSeekbar();
		}else{
			//super.onBackPressed();
			Intent intent = new Intent();
			intent.putExtra(MainActivity.SETTING_KEY, isSettingChanged);
			setResult(MainActivity.SETTING_CODE, intent);
			finish();
		}
	}

	private  void showSeekbar(int type){
		hideSeekbar();
		switch (type) {
			case 1:
				mSpdPop.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
				mIsnPopupWindowShowing = true;
				break;
			case 2:
				mSpcPop.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
				mIsnPopupWindowShowing = true;
				break;
			case 3:
				mVolPop.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
				mIsnPopupWindowShowing = true;
				break;
			default:
				break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		MainActivity.log("onStartTrackingTouch");
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		MainActivity.log("onStopTrackingTouch");
		if(seekBar==bar_spd){
			SharedPreferencesUtils.setParam(this,"speed_preference",String.valueOf(speed));
			mSpeedPreference.setSummary("当前值：" + speed);
		}else if(seekBar==bar_spc){
			SharedPreferencesUtils.setParam(this,"pitch_preference",String.valueOf(pitch));
			mPitchPreference.setSummary("当前值：" + pitch);
		}else if(seekBar==bar_vol){
			SharedPreferencesUtils.setParam(this,"volume_preference",String.valueOf(volume));
			mVolumePreference.setSummary("当前值：" + volume);
		}

	}
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(seekBar==bar_spd){
			MainActivity.log("speed bar progress:" + progress);
			speed = progress;
		}else if(seekBar==bar_spc){
			MainActivity.log("pitch bar");
			pitch = progress;
		}else if(seekBar==bar_vol){
			MainActivity.log("volum bar");
			volume = progress;
		}
		isSettingChanged=true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		final String key = preference.getKey();
		if("speeker_preference".equals(key)){
			ListPreference listPreference=(ListPreference)preference;
			int index=listPreference.findIndexOfValue(speeker);
			CharSequence[] entries=listPreference.getEntries();
			listPreference.setSummary(entries[index]);
		}
		if("speed_preference".equals(key)){
			showSeekbar(1);
		}
		if("pitch_preference".equals(key)){
			showSeekbar(2);
		}
		if("volume_preference".equals(key)){
			showSeekbar(3);
		}

		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		final String key = preference.getKey();
		if ("speeker_preference".equals(key)) {
			ListPreference listPreference=(ListPreference)preference;
			String value = (String) newValue;
			CharSequence[] entries=listPreference.getEntries();
			int index=listPreference.findIndexOfValue((String)newValue);
			listPreference.setSummary(entries[index]);

			SharedPreferencesUtils.setParam(this,"speeker", value);

			isSettingChanged = true;
		}

		 return true;
	}

}

