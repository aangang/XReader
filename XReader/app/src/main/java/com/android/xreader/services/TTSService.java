package com.android.xreader.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.xreader.BookPageFactory;
import com.android.xreader.MainActivity;
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

    private int mTtsTimerLeft = 0;
    CountDownTimer mCountDown;

    public TTSService() {


    }

    @Override
    public void onCreate() {
        super.onCreate();


        // 参数一：唯一的通知标识；参数二：通知消息。
        startForeground(110, makeCustomNotification());// 开始前台服务
    }

    Notification makeCustomNotification(){
        Notification.Builder builder = new Notification.Builder
                (this.getApplicationContext()); //获取一个Notification构造器
        RemoteViews remoteViews = new RemoteViews(this.getPackageName(),R.layout.notification_layout);// 获取remoteViews（参数一：包名；参数二：布局资源）
        builder = new Notification.Builder(this.getApplicationContext())
            .setContent(remoteViews);// 设置自定义的Notification内容
        Intent nfIntent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.header);
        Notification notification = builder.getNotification();// 获取构建好的通知--.build()最低要求在
        // API16及以上版本上使用，低版本上可以使用.getNotification()。
        return notification;
    }
    Notification makeNotification(){
        Notification.Builder builder = new Notification.Builder
                (this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.noti_bg)) // 设置下拉列表中的图标(大图标)
                .setSmallIcon(R.drawable.header) // 设置状态栏内的小图标
                //.setContentText("要显示的内容") // 设置上下文内容
                //.setContentTitle("下拉列表中的Title") // 设置下拉列表里的标题
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        Notification notification = builder.build(); // 获取构建好的Notification
        //notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        return notification;

    }

    @Override
    public void onDestroy() {

        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        mgr.closeDB();
        super.onDestroy();

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

        @Override
        public void setTTSTimer(int seconds) {
            mTtsTimerLeft = seconds;

            if(mCountDown != null){
                mCountDown.cancel();
            }
            mCountDown = new CountDownTimer(seconds * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    mTtsTimerLeft = mTtsTimerLeft -1;
                }
                @Override
                public void onFinish() {
                    mTtsTimerLeft = 0;
                    if(isSpeeking()){
                        stopSpeeking();
                    }
                }
            };
            mCountDown.start();
        }

        @Override
        public int getTTSTimer() {
            return  mTtsTimerLeft;
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
