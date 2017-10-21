package com.android.xreader.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.android.xreader.tts.KqwSpeechCompound;
import com.android.xreader.utils.Tools;
import com.iflytek.cloud.SynthesizerListener;

public class TTSService extends Service {

    //tts
    private KqwSpeechCompound mKqwSpeechCompound;
    SynthesizerListener mTtsListener = null;

    public TTSService() {


    }

    @Override
    public IBinder onBind(Intent intent) {
        //throw new UnsupportedOperationException("Not yet implemented");

        return new TTSBinder();
    }

    //必须继承binder，才能作为中间人对象返回
    class TTSBinder extends Binder implements TTSControler{

        @Override
        public void startSpeeking(String page) {
            Tools.log("startSpeeking");
            TTSService.this.startSpeeking(page);
        }

        @Override
        public void stopSpeeking() {
            Tools.log("stopSpeeking");
            TTSService.this.stopSpeeking();
        }

        @Override
        public boolean isSpeeking() {
            Tools.log("isSpeeking");
            return TTSService.this.isSpeeking();
        }

        @Override
        public void setTTSListener(SynthesizerListener listener) {
            // 初始化语音合成对象
            mKqwSpeechCompound = new KqwSpeechCompound(TTSService.this);
            mTtsListener = listener;
            mKqwSpeechCompound.setTtsListener(mTtsListener);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startSpeeking(String page) {
        Tools.log("TTSService startSpeeking");
        mKqwSpeechCompound.speaking(page);
    }

    public void stopSpeeking() {
        Tools.log("TTSService stopSpeeking");
        mKqwSpeechCompound.stopSpeaking();
    }

    public boolean isSpeeking() {
        Tools.log("TTSService isSpeeking");
        return mKqwSpeechCompound.isSpeaking();
    }
}
