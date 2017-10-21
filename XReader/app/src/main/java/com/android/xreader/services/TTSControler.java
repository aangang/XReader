package com.android.xreader.services;

import com.iflytek.cloud.SynthesizerListener;

/**
 * Created by gang.an on 2017/10/21.
 */

public interface TTSControler {
    void startSpeeking(String page);

    void stopSpeeking();

    boolean isSpeeking();

    void setTTSListener(SynthesizerListener listener);
}
