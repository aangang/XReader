package com.android.xreader.tts;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.Window;

import com.android.xreader.R;
import com.android.xreader.utils.SharedPreferencesUtils;


/**
 * 合成设置界面
 */
public class TtsSettings extends PreferenceActivity implements OnPreferenceChangeListener ,Preference.OnPreferenceClickListener{
	
	public static final String PREFER_NAME = "com.iflytek.setting";
	private EditTextPreference mSpeedPreference;
	private EditTextPreference mPitchPreference;
	private EditTextPreference mVolumePreference;
	private ListPreference speekerPreference;

	String speeker = "";
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		// 指定保存文件名字
		getPreferenceManager().setSharedPreferencesName(PREFER_NAME);
		addPreferencesFromResource(R.xml.tts_setting);
/*
		mSpeedPreference = (EditTextPreference)findPreference("speed_preference");
		mSpeedPreference.getEditText().addTextChangedListener(new SettingTextWatcher(TtsSettings.this,mSpeedPreference,0,200));

		mPitchPreference = (EditTextPreference)findPreference("pitch_preference");
		mPitchPreference.getEditText().addTextChangedListener(new SettingTextWatcher(TtsSettings.this,mPitchPreference,0,100));

		mVolumePreference = (EditTextPreference)findPreference("volume_preference");
		mVolumePreference.getEditText().addTextChangedListener(new SettingTextWatcher(TtsSettings.this,mVolumePreference,0,100));
*/

		speeker = (String) SharedPreferencesUtils.getParam(this,"speeker","xiaoyan");

		speekerPreference = (ListPreference) findPreference("speeker_preference");
		CharSequence[] entries=speekerPreference.getEntries();
		int index=speekerPreference.findIndexOfValue((String)speeker);
		speekerPreference.setSummary(entries[index]);
		speekerPreference.setOnPreferenceChangeListener(this);

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
		}

		 return true;
	}

	
	
}