package com.android.xreader.tts;

/**
 * Created by Administrator on 2017/10/18.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.xreader.utils.SharedPreferencesUtils;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

/**
 * 语音合成的类 发音人明细http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=367
 *
 * @author kongqw
 */
public class KqwSpeechCompound {
    // Log标签
    private static final String TAG = "KqwSpeechCompound";
    // 上下文
    private Context mContext;
    // 语音合成对象
    private static SpeechSynthesizer mTts;
    private Toast mToast;

    SynthesizerListener mTtsListener;
    String mSpeeker = "xiaoyan";
    String mSpeed = "50";
    String mPitch = "50";
    String mVolume = "100";


    /**
     * 发音人
     */
    public final static String[] COLOUD_VOICERS_ENTRIES =
            {"小燕", "小宇", "凯瑟琳", "亨利", "玛丽", "小研", "小琪", "小峰", "小梅", "小莉", "小蓉", "小芸", "小坤", "小强 ", "小莹",
            "小新", "楠楠", "老孙",};
    public final static String[] COLOUD_VOICERS_VALUE =
            {"xiaoyan", "xiaoyu", "catherine", "henry", "vimary", "vixy", "xiaoqi", "vixf", "xiaomei",
            "xiaolin", "xiaorong", "xiaoqian", "xiaokun", "xiaoqiang", "vixying", "xiaoxin", "nannan", "vils",};


    /**
     * 构造方法
     *
     * @param context
     */
    public KqwSpeechCompound(Context context) {
        // 上下文
        mContext = context;
        mToast = Toast.makeText(mContext,"",Toast.LENGTH_SHORT);
        mTtsListener = mDefaultTtsListener;

        mSpeeker = (String) SharedPreferencesUtils.getParam(mContext,"speeker","xiaoyan");
        mSpeed = (String) SharedPreferencesUtils.getParam(mContext,"speed_preference","50");
        mPitch = (String) SharedPreferencesUtils.getParam(mContext,"pitch_preference", "50");
        mVolume = (String) SharedPreferencesUtils.getParam(mContext,"volume_preference", "100");

        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(mContext, new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    Log.i(TAG, "初始化失败,错误码：" + code);
                    showTip("初始化失败,错误码："+code);
                }
            }
        });
    }

    public void setTtsListener(SynthesizerListener ttsListener){
        this.mTtsListener =ttsListener;
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    /**
     * 开始合成
     *
     * @param text
     */
    public void speaking(String text) {
        //set params
        setParam();
        // 非空判断
        if (TextUtils.isEmpty(text)) {
            return;
        }
        int code = mTts.startSpeaking(text, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                Toast.makeText(mContext, "没有安装语音+ code = " + code, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "语音合成失败,错误码: " + code, Toast.LENGTH_SHORT).show();
            }
        }


    }

    /*
     * 停止语音播报
     */
    public static void stopSpeaking() {
        // 对象非空并且正在说话
        if (null != mTts && mTts.isSpeaking()) {
            // 停止说话
            mTts.stopSpeaking();
        }
    }

    /**
     * 判断当前有没有说话
     *
     * @return
     */
    public static boolean isSpeaking() {
        if (null != mTts) {
            return mTts.isSpeaking();
        } else {
            return false;
        }
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mDefaultTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            Log.i(TAG, "开始播放");
        }

        @Override
        public void onSpeakPaused() {
            Log.i(TAG, "暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            Log.i(TAG, "继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            // TODO 缓冲的进度
            Log.i(TAG, "缓冲 : " + percent);
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // TODO 说话的进度
            Log.i(TAG, "合成 : " + percent);
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                Log.i(TAG, "播放完成");

            } else if (error != null) {
                Log.i(TAG, error.getPlainDescription(true));
            }

        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }
    };

    /**
     * 参数设置
     *
     * @return
     */
    private void setParam() {

        mSpeeker = (String) SharedPreferencesUtils.getParam(mContext,"speeker","xiaoyan");
        mSpeed = (String) SharedPreferencesUtils.getParam(mContext,"speed_preference","50");
        mPitch = (String) SharedPreferencesUtils.getParam(mContext,"pitch_preference", "50");
        mVolume = (String) SharedPreferencesUtils.getParam(mContext,"volume_preference", "100");

        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 引擎类型 网络
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, mSpeeker/*COLOUD_VOICERS_VALUE[0]*/);
        // 设置语速
        mTts.setParameter(SpeechConstant.SPEED, mSpeed);
        // 设置音调
        mTts.setParameter(SpeechConstant.PITCH, mPitch);
        // 设置音量
        mTts.setParameter(SpeechConstant.VOLUME, mVolume);
        // 设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");

        // mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/KRobot/wavaudio.pcm");
        // 背景音乐  1有 0 无
        // mTts.setParameter("bgs", "1");
    }
}
