package com.android.xreader.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.android.xreader.BookPageFactory;
import com.android.xreader.R;
import com.android.xreader.db.DBManager;
import com.android.xreader.module.BookFile;
import com.android.xreader.module.BookMark;
import com.android.xreader.tts.KqwSpeechCompound;
import com.android.xreader.utils.FusionField;
import com.android.xreader.utils.SharedPreferencesUtils;
import com.android.xreader.utils.Tools;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class TTSService extends Service {

    private final int FONT_STEP = 2;
    //tts
    private KqwSpeechCompound mKqwSpeechCompound;

    Vector<String> curPageLines;
    String mPageLines = "";

    //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
    public static final String SETTING_KEY = "setting";
    public static final String DIR_KEY = "begin";
    public static final String DIR_NAME = "filepath";
    BookPageFactory pagefactory;
    private String filepath;
    private int width;
    private int height;
    private static int begin = 0;// 记录的书籍开始位置
    // catch路径
    private String filecatchpath = "/data/data/" + FusionField.baseActivity.getPackageName() + "/";
    int defaultSize = 30;
    private DBManager mgr;

    private int mCurrentFontSize; // 字体大小
    private static String word = "";// 记录当前页面的文字

    Bitmap mCurPageBitmap, mNextPageBitmap;
    Canvas mCurPageCanvas, mNextPageCanvas;

    PageFlipingControler mPageFlipinger;


    public TTSService() {


    }

    @Override
    public IBinder onBind(Intent intent) {
        //throw new UnsupportedOperationException("Not yet implemented");
        Tools.log("onBind service %%%%%%%%%%%%%%%%%%%%%%%");
        BookFile bookFile = (BookFile) intent.getExtras().getSerializable("path");
        filepath = bookFile.name;
        // 读取SP记录
        begin =(int) SharedPreferencesUtils.getParam(this,filepath + "begin", 0);
        Tools.log("begin:" + begin + "   %%%%%%%%%%%%%%%%%%%%%%%%%%  filepath:" + filepath);
        mCurrentFontSize =(int)SharedPreferencesUtils.getParam(this,"size", defaultSize);

        width = intent.getExtras().getInt("width");
        height = intent.getExtras().getInt("height");
        // 当前页
        mCurPageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 下一页
        mNextPageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 画布
        mCurPageCanvas = new Canvas(mCurPageBitmap);
        mNextPageCanvas = new Canvas(mNextPageBitmap);

        // 工厂
        pagefactory = new BookPageFactory(this, width, height);

        try {
            if (bookFile.flag.equals("1")) {
                pagefactory.openbook(bookFile.path, begin);
            } else {
                pagefactory.openbook(filecatchpath + "catch.txt", begin);
            }
            Tools.log("book opened :" + bookFile.name);
            pagefactory.setM_fontSize(mCurrentFontSize);
            pagefactory.onDraw(mCurPageCanvas);
        } catch (IOException e1) {
            e1.printStackTrace();
            Toast.makeText(this, "no find file", Toast.LENGTH_SHORT).show();
        }

        // 初始化语音合成对象
        mKqwSpeechCompound = new KqwSpeechCompound(TTSService.this);
        mKqwSpeechCompound.setTtsListener(mTtsListener);

        mgr = new DBManager(this);
        return new TTSBinder();
    }

    public Bitmap toPrevPage() {
        // TODO Auto-generated method stub
        try {
            pagefactory.prePage();
            begin = pagefactory.getM_mbBufBegin();// 获取当前阅读位置
            word = pagefactory.getFirstLineText();// 获取当前阅读位置的首行文字
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (pagefactory.isfirstPage()) {
            Toast.makeText(this, "当前是第一页", Toast.LENGTH_SHORT).show();
        }
        pagefactory.onDraw(mNextPageCanvas);
        //mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
        //book_image.setImageBitmap(mNextPageBitmap);
        SharedPreferencesUtils.setParam(this,filepath + "begin", begin);

        return mNextPageBitmap;
    }


    public Bitmap toNextPage() {
        // TODO Auto-generated method stub
        try {
            pagefactory.nextPage();
            begin = pagefactory.getM_mbBufBegin();// 获取当前阅读位置
            word = pagefactory.getFirstLineText();// 获取当前阅读位置的首行文字
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (pagefactory.islastPage()) {
            Toast.makeText(this, "已经是最后一页了", Toast.LENGTH_SHORT).show();
        }
        pagefactory.onDraw(mNextPageCanvas);
        //mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
        //book_image.setImageBitmap(mNextPageBitmap);
        SharedPreferencesUtils.setParam(this,filepath + "begin", begin);

        return mNextPageBitmap;
    }





    //必须继承binder，才能作为中间人对象返回
    class TTSBinder extends Binder implements TTSControler{

        @Override
        public void startSpeeking() {
            Tools.log("startSpeeking");
            TTSService.this.startSpeeking();
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
        public Bitmap getCurrentPageBitmap(){
            pagefactory.onDraw(mCurPageCanvas);
            return mCurPageBitmap;
        }

        @Override
        public Bitmap getNextPageBitmap(){
            pagefactory.onDraw(mNextPageCanvas);
            return mNextPageBitmap;
        }

        @Override
        public Bitmap toNextPage(){
            return TTSService.this.toNextPage();
        }

        @Override
        public Bitmap toPrevPage(){
            return TTSService.this.toPrevPage();
        }

        @Override
        public void addBookMard(){
            TTSService.this.addBookMark();
        }

        @Override
        public float getCurrPercent() {
            return (float) (getBegin() * 1.0 / pagefactory.getM_mbBufLen());
        }

        @Override
        public void setCurrPercent(int progress) {
            begin = pagefactory.getM_mbBufLen() * progress / 100;
            if (begin > 0) {
                try {
                    pagefactory.nextPage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pagefactory.setM_mbBufEnd(begin);
                pagefactory.setM_mbBufBegin(begin);
                pagefactory.onDraw(mCurPageCanvas);
            }
        }

        @Override
        public void fontDiscre() {
            mCurrentFontSize -= FONT_STEP;
            pagefactory.setTextSize(mCurrentFontSize);
            pagefactory.onDraw(mCurPageCanvas);
            pagefactory.onDraw(mNextPageCanvas);
        }

        @Override
        public void fontIncre() {
            mCurrentFontSize += FONT_STEP;
            pagefactory.setTextSize(mCurrentFontSize);
            pagefactory.onDraw(mCurPageCanvas);
            pagefactory.onDraw(mNextPageCanvas);
        }

        @Override
        public int getBegin() {
            return begin;
        }

        @Override
        public int getFontSize() {
            return mCurrentFontSize;
        }

        @Override
        public void setPageBG(boolean isNight) {
            pagefactory.setTextColor(isNight);
            if (isNight) {
                pagefactory.setBgBitmap(BitmapFactory.decodeResource(TTSService.this.getResources(), R.drawable.bg_book_night));
            } else {
                pagefactory.setBgBitmap(BitmapFactory.decodeResource(TTSService.this.getResources(), R.drawable.bg_book_day));
            }
        }

        @Override
        public void setPageFlipinger(PageFlipingControler pageFlipinger) {
            TTSService.this.mPageFlipinger = pageFlipinger;
        }
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            Log.i("txt", "onSpeakBegin");
        }

        @Override
        public void onSpeakPaused() {
            Log.i("txt", "onSpeakPaused");
        }

        @Override
        public void onSpeakResumed() {
            Log.i("txt", "onSpeakResumed");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            // TODO 缓冲的进度
            Log.i("txt", "onBufferProgress : " + percent);
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // TODO 说话的进度
            Log.i("txt", "onSpeakProgress : " + percent);
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                Log.i("txt", "onCompleted  toNextPage");

                toNextPage();
                mPageFlipinger.showNextPage();
                String page = "";
                curPageLines = pagefactory.getCurrentPageLines();
                for(String line:curPageLines){
                    page = page + line;
                }
                Tools.log(page);
                mPageLines = page;
                if(page != null && !page.equals("")) {
                    Tools.log("speeking continue");
                    //mKqwSpeechCompound.speaking(page);
                    startSpeeking(mPageLines);
                }

            } else if (error != null) {
                Log.i("txt", error.getPlainDescription(true));
            }

        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }
    };


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mgr.closeDB();
    }

    public void startSpeeking(String lines){
        mKqwSpeechCompound.speaking(lines);
    }

    public void startSpeeking() {
        Tools.log("TTSService startSpeeking");
        if(!isSpeeking()){
            curPageLines = pagefactory.getCurrentPageLines();
            String page = "";
            for (String line : curPageLines) {
                page = page + line;
            }
            Tools.log(page);
            mPageLines = page;
        }

        mKqwSpeechCompound.speaking(mPageLines);
    }

    public void stopSpeeking() {
        Tools.log("TTSService stopSpeeking");
        mKqwSpeechCompound.stopSpeaking();
    }

    public boolean isSpeeking() {
        Tools.log("TTSService isSpeeking");
        return mKqwSpeechCompound.isSpeaking();
    }

    public void addBookMark(){
        BookMark mark = new BookMark();
        mark.name = filepath;
        mark.begin = begin;
        mark.time = getStringCurrentDate();
        if (word.trim().equals("")) {
            mark.word = pagefactory.getSecLineText().trim();
        } else {
            mark.word = word.trim();
        }
        mark.word += "\n" + mark.time;
        mgr.addMarks(mark);
    }


    public static String getStringCurrentDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(currentTime);
    }
}
