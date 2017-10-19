package com.android.xreader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.xreader.tts.KqwSpeechCompound;

public class SpeechTest extends AppCompatActivity {
    private EditText mEtText;
    private KqwSpeechCompound mKqwSpeechCompound;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_test);

        mEtText = (EditText) findViewById(R.id.et_text);

        // 初始化语音合成对象
        mKqwSpeechCompound = new KqwSpeechCompound(this);
    }

    /**
     * 开始合成
     *
     * @param view
     */
    public void start(View view) {
        Toast.makeText(this, "开始合成 : " + mEtText.getText().toString().trim(), Toast.LENGTH_SHORT).show();
        mKqwSpeechCompound.speaking(mEtText.getText().toString().trim());
    }
}
