package com.android.xreader.services;

import android.graphics.Bitmap;

import com.iflytek.cloud.SynthesizerListener;

/**
 * Created by gang.an on 2017/10/21.
 */

public interface TTSControler {
    void startSpeeking();

    void stopSpeeking();

    boolean isSpeeking();

    Bitmap getCurrentPageBitmap();
    Bitmap getNextPageBitmap();
    Bitmap toNextPage();
    Bitmap toPrevPage();
    void addBookMard();
    void fontDiscre();
    void fontIncre();
    int getBegin();
    int getFontSize();
    void setPageBG(boolean isNight);

}
