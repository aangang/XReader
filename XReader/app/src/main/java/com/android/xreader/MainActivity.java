package com.android.xreader;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.android.xreader.db.DBManager;
import com.android.xreader.module.BookFile;
import com.android.xreader.module.BookMark;
import com.android.xreader.services.PageFlipingControler;
import com.android.xreader.services.TTSControler;
import com.android.xreader.services.TTSService;
import com.android.xreader.tts.KqwSpeechCompound;
import com.android.xreader.tts.TtsSettings;
import com.android.xreader.utils.FusionField;
import com.android.xreader.utils.SharedPreferencesUtils;
import com.android.xreader.utils.Tools;
import com.android.xreader.views.CircleProgressbar;
import com.android.xreader.views.CountDownView;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class MainActivity extends Activity implements OnSeekBarChangeListener, OnClickListener {


    public static final String tag = Tools.tag;

    private static final String TAG = "BookActivity";

    public static final int DIR_CODE = 123;
    public static final int SETTING_CODE = 234;
    public static final int TIMER_CODE = 345;

    public static final String SETTING_KEY = "setting";
    public static final String TIMER_KEY = "timer_setting";
    public static final String DIR_KEY = "begin";
    public static final String DIR_NAME = "filepath";


    //private PageWidget mPageWidget;

    BookPageFactory pagefactory;

    private String filepath;

    private int width;

    private int height;

    private int light; // 亮度值

    private PopupWindow mPopupWindow, mToolpop, mToolpop1, mToolpop2, mToolpop4;

    private View popupwindwow, toolpop, toolpop1, toolpop2, toolpop4, topBar;

    private View countDown;
    private PopupWindow mCountDownWindow;
    private CountDownView mCircleTimer;

    TextView btnSpeek;

    private SeekBar seekBar1, seekBar2, seekBar4;

    private int a = 0, b = 0;// 记录toolpop的位置

    private boolean isNight; // 亮度模式,白天和晚上

    private TextView markEdit4;

    private Context mContext = null;

    private static final int FONT_STEP = 2;

    //private int mCurrentFontSize; // 字体大小

    private Boolean mIsMainPopupWindowShowing = false;// 主popwindow是否显示
    private boolean mIsSubPopUpWindowShowing = false;

    ImageView book_image;
    Button prev,next;

    boolean isSpeeking = false;
    TTSControler ttsControler;
    PageAutoFlipinger mPageFlipinger = new PageAutoFlipinger();

    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 竖屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        log("onCreate %%%%%%%%%%%%%%%%%");

        //mgr = new DBManager(this);

        mContext = getBaseContext();

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;

        // 读取SP记录
        //begin =(int)SharedPreferencesUtils.getParam(this,filepath + "begin", 0);
        light =(int)SharedPreferencesUtils.getParam(this,"light", 5);
        isNight =(boolean)SharedPreferencesUtils.getParam(this,"night", false);
        //mCurrentFontSize =(int)SharedPreferencesUtils.getParam(this,"size", defaultSize);

        //set brightness
        WindowManager.LayoutParams lp = MainActivity.this.getWindow().getAttributes();
        lp.screenBrightness = (float) light * (1f / 255f);
        MainActivity.this.getWindow().setAttributes(lp);

        //page view
        //mPageWidget = new PageWidget(this, width, height);

        setContentView(R.layout.read);

        RelativeLayout rlayout = (RelativeLayout) findViewById(R.id.readlayout);
        topBar = findViewById(R.id.top_bar);
        //rlayout.addView(mPageWidget);

        BookFile bookFile = (BookFile) getIntent().getExtras().getSerializable("path");
        filepath = bookFile.name;

        initTopBar();

        prev = (Button) findViewById(R.id.prev);
        next = (Button) findViewById(R.id.next);
        prev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                log("prev  clicked");
                //toPrePage();
                showPrevPage();
                speekOrNot();
            }
        });
        next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                log("next  clicked");
                //toNextPage();

                showNextPage();
                speekOrNot();
            }
        });

        mGestureDetector = new GestureDetector(new mGestureListener());

        book_image = (ImageView) findViewById(R.id.book_view);
        book_image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        /*mPageWidget.setBitmaps(mCurPageBitmap, mCurPageBitmap);
        mPageWidget.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {

                boolean ret;
                if (v == mPageWidget) {
                    if (e.getAction() == MotionEvent.ACTION_DOWN) {
                        if (e.getY() > height) {
                            return false;
                        }
                        mPageWidget.abortAnimation();
                        mPageWidget.calcCornerXY(e.getX(), e.getY());
                        pagefactory.onDraw(mCurPageCanvas);
                    }
                    ret = mPageWidget.doTouchEvent(e, MainActivity.this);
                    return ret;
                }
                return false;
            }
        });*/

        setPop();

        /*mPageWidget.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsSubPopUpWindowShowing) {
                    hideSubMenu();
                }
                if (mIsMainPopupWindowShowing) {
                    setMainMenuVisibility(false);
                } else {
                    setMainMenuVisibility(true);
                }
            }
        });*/
        //rlayout.setOnClickListener(new OnClickListener() {
        /*book_image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsSubPopUpWindowShowing) {
                    hideSubMenu();
                }
                if (mIsMainPopupWindowShowing) {
                    setMainMenuVisibility(false);
                } else {
                    setMainMenuVisibility(true);
                }
            }
        });*/

        Intent intent = new Intent(this, TTSService.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("path", bookFile);
        bundle.putInt("width",width);
        bundle.putInt("height",height);
        intent.putExtras(bundle);

        //混合调用
        //为了把服务所在进程变成服务进程
        //startService(intent);
        //为了拿到中间对象
        bindService(intent, TTSConn, BIND_AUTO_CREATE);

    }

    private class mGestureListener implements GestureDetector.OnGestureListener{

                        // 用户轻触触摸屏，由1个MotionEvent ACTION_DOWN触发
                       public boolean onDown(MotionEvent e) {
                        Log.i("MyGesture", "onDown");
                        Toast.makeText(MainActivity.this, "onDown", Toast.LENGTH_SHORT).show();
                        return false;
                   }

                        /*
39.         * 用户轻触触摸屏，尚未松开或拖动，由一个1个MotionEvent ACTION_DOWN触发
40.         * 注意和onDown()的区别，强调的是没有松开或者拖动的状态
41.         *
42.         * 而onDown也是由一个MotionEventACTION_DOWN触发的，但是他没有任何限制，
43.         * 也就是说当用户点击的时候，首先MotionEventACTION_DOWN，onDown就会执行，
44.         * 如果在按下的瞬间没有松开或者是拖动的时候onShowPress就会执行，如果是按下的时间超过瞬间
45.         * （这块我也不太清楚瞬间的时间差是多少，一般情况下都会执行onShowPress），拖动了，就不执行onShowPress。
46.         */
                        public void onShowPress(MotionEvent e) {
                        Log.i("gesture", "onShowPress");
                        Toast.makeText(MainActivity.this, "onShowPress", Toast.LENGTH_SHORT).show();
                    }

                        // 用户（轻触触摸屏后）松开，由一个1个MotionEvent ACTION_UP触发
                        ///轻击一下屏幕，立刻抬起来，才会有这个触发
                        //从名子也可以看出,一次单独的轻击抬起操作,当然,如果除了Down以外还有其它操作,那就不再算是Single操作了,所以这个事件 就不再响应
                        public boolean onSingleTapUp(MotionEvent e) {
                        Log.i("gesture", "onSingleTapUp");
                        Toast.makeText(MainActivity.this, "onSingleTapUp", Toast.LENGTH_SHORT).show();
                        return true;
                    }

                        // 用户按下触摸屏，并拖动，由1个MotionEvent ACTION_DOWN, 多个ACTION_MOVE触发
                        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                                                   float distanceX, float distanceY) {
                        Log.i("gesture", "onScroll:"+(e2.getX()-e1.getX()) +"   "+distanceX);
                        Toast.makeText(MainActivity.this, "onScroll", Toast.LENGTH_LONG).show();

                       return true;
                    }

                        // 用户长按触摸屏，由多个MotionEvent ACTION_DOWN触发
                        public void onLongPress(MotionEvent e) {
                         Log.i("gesture", "onLongPress");
                         Toast.makeText(MainActivity.this, "onLongPress", Toast.LENGTH_LONG).show();
                    }

                        // 用户按下触摸屏、快速移动后松开，由1个MotionEvent ACTION_DOWN, 多个ACTION_MOVE, 1个ACTION_UP触发
                        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                                                  float velocityY) {
                        Log.i("gesture", "onFling");
                       Toast.makeText(MainActivity.this, "onFling", Toast.LENGTH_LONG).show();
                        return true;
                    }
            };


    private class mSimpleGestureListener extends GestureDetector.SimpleOnGestureListener {

                /*****OnGestureListener的函数*****/
                public boolean onDown(MotionEvent e) {
                       Log.i("gesture", "onDown");
                        //Toast.makeText(MainActivity.this, "onDown", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                public void onShowPress(MotionEvent e) {
                        Log.i("gesture", "onShowPress");
                        Toast.makeText(MainActivity.this, "onShowPress", Toast.LENGTH_SHORT).show();
                    }

                public boolean onSingleTapUp(MotionEvent e) {
                        Log.i("gesture", "onSingleTapUp");
                        Toast.makeText(MainActivity.this, "onSingleTapUp", Toast.LENGTH_SHORT).show();
                    if (mIsSubPopUpWindowShowing) {
                        hideSubMenu();
                    }
                    if (mIsMainPopupWindowShowing) {
                        setMainMenuVisibility(false);
                    } else {
                        setMainMenuVisibility(true);
                    }
                        return true;
                    }

                public boolean onScroll(MotionEvent e1, MotionEvent e2,
                               float distanceX, float distanceY) {
                        Log.i("gesture", "onScroll:" + (e2.getX() - e1.getX()) + "   " + distanceX);
                        Toast.makeText(MainActivity.this, "onScroll", Toast.LENGTH_LONG).show();

                        return true;
                    }

                public void onLongPress(MotionEvent e) {
                        Log.i("gesture", "onLongPress");
                        //Toast.makeText(MainActivity.this, "onLongPress", Toast.LENGTH_LONG).show();
                    }

                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                        float velocityY) {
                       Log.i("gesture", "onFling");
                        Toast.makeText(MainActivity.this, "onFling", Toast.LENGTH_LONG).show();

                        return true;
                    }

                /*****OnDoubleTapListener的函数*****/
                public boolean onSingleTapConfirmed(MotionEvent e) {
                        Log.i("gesture", "onSingleTapConfirmed");
                        Toast.makeText(MainActivity.this, "onSingleTapConfirmed", Toast.LENGTH_LONG).show();
                                return true;
                   }

                public boolean onDoubleTap(MotionEvent e) {
                        Log.i("gesture", "onDoubleTap");
                        Toast.makeText(MainActivity.this, "onDoubleTap", Toast.LENGTH_LONG).show();
                        return true;
                    }

                public boolean onDoubleTapEvent(MotionEvent e) {
                        Log.i("gesture", "onDoubleTapEvent");
                        Toast.makeText(MainActivity.this, "onDoubleTapEvent", Toast.LENGTH_LONG).show();
                        return true;
                    }


    }



    class PageAutoFlipinger implements PageFlipingControler{
        @Override
        public void showNextPage() {
            if(isSpeeking){
                MainActivity.this.showCurrPage();
            }else{
                MainActivity.this.showNextPage();
            }

        }

        @Override
        public void showPrevPage() {
            MainActivity.this.showPrevPage();
        }

    }

    //TTS service
    private ServiceConnection TTSConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            ttsControler = (TTSControler) service;
            setReadBg();
            ttsControler.setPageFlipinger(mPageFlipinger);
            book_image.setImageBitmap(ttsControler.getCurrentPageBitmap()/*mCurPageBitmap*/);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public static void log(String string){
        Log.i(tag, string);
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

    public void showPrevPage(){
        book_image.setImageBitmap(ttsControler.toPrevPage());
    }

    public void showNextPage(){
        book_image.setImageBitmap(ttsControler.toNextPage());
    }

    public void showCurrPage(){
        book_image.setImageBitmap(ttsControler.getCurrentPageBitmap());
    }

    /**
     * 初始化所有POPUPWINDOW
     */
    private void setPop() {
        int subPopUpWindowHeigt = getResources().getDimensionPixelSize(R.dimen.sub_popup_widow_height);
        popupwindwow = this.getLayoutInflater().inflate(R.layout.bookpop, null);
        toolpop = this.getLayoutInflater().inflate(R.layout.toolpop, null);
        mPopupWindow = new PopupWindow(popupwindwow, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mToolpop = new PopupWindow(toolpop, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        toolpop1 = this.getLayoutInflater().inflate(R.layout.tool11, null);// 字体
        mToolpop1 = new PopupWindow(toolpop1, ViewGroup.LayoutParams.MATCH_PARENT, subPopUpWindowHeigt);
        toolpop2 = this.getLayoutInflater().inflate(R.layout.tool22, null);// 亮度
        mToolpop2 = new PopupWindow(toolpop2, ViewGroup.LayoutParams.MATCH_PARENT, subPopUpWindowHeigt);
        toolpop4 = this.getLayoutInflater().inflate(R.layout.tool44, null);// 进度
        mToolpop4 = new PopupWindow(toolpop4, ViewGroup.LayoutParams.MATCH_PARENT, subPopUpWindowHeigt);

        countDown = this.getLayoutInflater().inflate(R.layout.timer_pop,null);
        mCountDownWindow = new PopupWindow(countDown, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mIsSubPopUpWindowShowing) {
                hideSubMenu();
                return true;
            }
            if (mIsMainPopupWindowShowing) {
                setMainMenuVisibility(false);
                return true;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (mIsSubPopUpWindowShowing) {
                hideSubMenu();
                return true;
            }

            if (mIsMainPopupWindowShowing) {
                setMainMenuVisibility(false);
            } else {
                setMainMenuVisibility(true);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void hideSubMenu() {
        mToolpop1.dismiss();
        mToolpop2.dismiss();
        mToolpop4.dismiss();

        mIsSubPopUpWindowShowing = false;
        mIsMainPopupWindowShowing = true;

        //mPopupWindow.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 0);
        mPopupWindow.showAtLocation((RelativeLayout) findViewById(R.id.readlayout), Gravity.BOTTOM, 0, 0);
    }

    private  void showTimerPop(int timerMinute){
        mCircleTimer = (CountDownView) countDown.findViewById(R.id.timer_pop);
        mCircleTimer.setCountdownTime(timerMinute);
        mCircleTimer.setAddCountDownListener(new CountDownView.OnCountDownFinishListener() {
            @Override
            public void countDownFinished() {
                Toast.makeText(MainActivity.this, "倒计时结束", Toast.LENGTH_SHORT).show();
                if(btnSpeek != null){
                    btnSpeek.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.play_ic,0,0);
                }
                hideTimerPop();
            }
        });
        mCountDownWindow.showAtLocation((RelativeLayout) findViewById(R.id.readlayout), Gravity.CENTER_VERTICAL|Gravity.END, 0, 0);
        mCircleTimer.startCountDown();
    }

    private void hideTimerPop(){
        mCountDownWindow.dismiss();
    }

    private void setMainMenuVisibility(boolean show) {
        if (show) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            mIsMainPopupWindowShowing = true;
            pop();
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            mIsMainPopupWindowShowing = false;
            mPopupWindow.dismiss();
            popDismiss();
        }
    }

    /**
     * 关闭55个弹出pop
     */
    public void popDismiss() {
        mToolpop.dismiss();
        mToolpop1.dismiss();
        mToolpop2.dismiss();
        mToolpop4.dismiss();
        topBar.setVisibility(View.INVISIBLE);
    }

    /**
     * popupwindow的弹出,工具栏
     */
    public void pop() {

        topBar.setVisibility(View.VISIBLE);
        //mPopupWindow.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 0);
        mPopupWindow.showAtLocation((RelativeLayout) findViewById(R.id.readlayout), Gravity.BOTTOM, 0, 0);
        TextView btnDirectory = (TextView) popupwindwow.findViewById(R.id.btn_directory);
        TextView btnProgress = (TextView) popupwindwow.findViewById(R.id.btn_progress);
        TextView btnTextSize = (TextView) popupwindwow.findViewById(R.id.btn_text_size);
        TextView btnBrightness = (TextView) popupwindwow.findViewById(R.id.btn_brightness);
        btnSpeek = (TextView) popupwindwow.findViewById(R.id.btn_speek);
        TextView btnSetting = (TextView) popupwindwow.findViewById(R.id.btn_setting);
        TextView btnTimer = (TextView) popupwindwow.findViewById(R.id.btn_timer);
        final TextView btnNight = (TextView) popupwindwow.findViewById(R.id.btn_night);
        final Drawable drawableNight = getResources().getDrawable(R.drawable.btn_night);
        // drawableNight.setBounds(0,0,20,20);
        final Drawable drawableDay = getResources().getDrawable(R.drawable.btn_day);
        // drawableDay.setBounds(0,0,20,20);
        if (isNight) {
            btnNight.setText(getString(R.string.bookpop_night));
            btnNight.setCompoundDrawablesWithIntrinsicBounds(null, drawableNight, null, null);
        } else {
            btnNight.setText(getString(R.string.bookpop_day));
            btnNight.setCompoundDrawablesWithIntrinsicBounds(null, drawableDay, null, null);
        }
        btnSpeek.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!ttsControler.isSpeeking()){

                    ttsControler.startSpeeking();
                    btnSpeek.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.pause_ic,0,0);
                    isSpeeking = true;
                }else{
                    ttsControler.stopSpeeking();
                    btnSpeek.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.play_ic,0,0);

                    isSpeeking = false;
                }
            }
        });
        btnSetting.setOnClickListener(this);

        btnTimer.setOnClickListener(this);

        btnDirectory.setOnClickListener(this);
        btnProgress.setOnClickListener(this);
        btnTextSize.setOnClickListener(this);
        btnBrightness.setOnClickListener(this);
        btnNight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNight) {
                    btnNight.setText(getString(R.string.bookpop_night));
                    btnNight.setCompoundDrawablesWithIntrinsicBounds(null, drawableNight, null, null);
                    isNight = true;
                } else {
                    btnNight.setText(getString(R.string.bookpop_day));
                    btnNight.setCompoundDrawablesWithIntrinsicBounds(null, drawableDay, null, null);
                    isNight = false;
                }
                setReadBg();

                book_image.setImageBitmap(ttsControler.getCurrentPageBitmap());
                /*mPageWidget.abortAnimation();
                pagefactory.onDraw(mCurPageCanvas);
                pagefactory.onDraw(mNextPageCanvas);
                mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
                mPageWidget.postInvalidate();*/
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        log("Main onStop");
        //unbindService(TTSConn);
        log("onStop %%%%%%%%%%%%%%%%%%%%");
        saveSp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log("onDestroy %%%%%%%%%%%%%%%%%%%%");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ib_back_top:
                if(isSpeeking){
                    Toast.makeText(this,"正在阅读，如要退出，需先停止阅读",Toast.LENGTH_SHORT).show();
                }else {
                    finish();
                }
                break;
            case R.id.ib_add_mark_top:
                ttsControler.addBookMard();
                Toast.makeText(getApplication(), "书签添加成功", Toast.LENGTH_SHORT).show();
                break;
            // 目录
            case R.id.btn_directory:
                Intent intent = new Intent(this, DirectoryActivity.class);
                intent.putExtra(DIR_NAME, filepath);
                startActivityForResult(intent, DIR_CODE);
            break;
            // 字体按钮
            case R.id.btn_text_size:
                a = 1;
                setToolPop(a);
                break;

            // 亮度按钮
            case R.id.btn_brightness:
                a = 2;
                setToolPop(a);
                break;
            // 进度
            case R.id.btn_progress:
                a = 4;
                setToolPop(a);
                break;
            case R.id.iv_font_discre:
                //mPageWidget.invalidate();
                ttsControler.fontDiscre();
                speekOrNot();
                book_image.setImageBitmap(ttsControler.getCurrentPageBitmap()/*mCurPageBitmap*/);
                break;

            case R.id.iv_font_incre:
                //mPageWidget.invalidate();
                ttsControler.fontIncre();
                speekOrNot();
                book_image.setImageBitmap(ttsControler.getCurrentPageBitmap()/*mCurPageBitmap*/);
                break;

            case R.id.btn_setting:
                Intent setting = new Intent(MainActivity.this,TtsSettings.class);
                startActivityForResult(setting,SETTING_CODE);
                break;
            case R.id.btn_timer:
                Intent timer = new Intent(MainActivity.this,TimerSettingActivity.class);
                startActivityForResult(timer,TIMER_CODE);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DIR_CODE:
                if (data != null) {
                    /*int markBegin = data.getExtras().getInt(DIR_KEY);
                    if (markBegin > 0) {
                        try {
                            pagefactory.nextPage();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        pagefactory.setM_mbBufEnd(markBegin);
                        pagefactory.setM_mbBufBegin(markBegin);
                        pagefactory.onDraw(mNextPageCanvas);
                        //mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
                        //mPageWidget.invalidate();
                        book_image.setImageBitmap(mCurPageBitmap);
                        postInvalidateUI();
                    }*/
                }
                break;
            case SETTING_CODE:
                log("from tts setting");
                if (data != null) {
                    boolean settingChanged = data.getExtras().getBoolean(SETTING_KEY);
                    if(settingChanged){
                        speekOrNot();
                    }
                }
                break;
            case TIMER_CODE:
                log("from tts timer");
                if (data != null) {
                    int timerMinute = data.getExtras().getInt(TIMER_KEY);
                    //set tts time secconds
                    ttsControler.setTTSTimer(timerMinute * 60);
                    if(timerMinute > 0){
                        showTimerPop(timerMinute);
                        if(!ttsControler.isSpeeking()){
                            ttsControler.startSpeeking();
                            btnSpeek.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.pause_ic,0,0);
                            isSpeeking = true;
                        }
                    }else if(timerMinute == 0){
                        hideTimerPop();
                        if(ttsControler.isSpeeking()){
                            ttsControler.stopSpeeking();
                            btnSpeek.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.play_ic,0,0);
                            isSpeeking = false;
                        }
                    }

                }
                break;
        }
    }

    void speekOrNot(){
        if(isSpeeking){
            ttsControler.stopSpeeking();
            ttsControler.startSpeeking();
        }
    }

    /**
     * 设置popupwindow的显示与隐藏
     *
     * @param a
     */
    public void setToolPop(int a) {
        if (a != 3 && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();

            mIsMainPopupWindowShowing = false;
            mIsSubPopUpWindowShowing = true;
        }

        // mToolpop.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
        // width * 45 / 320);
        //mToolpop.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 0);
        mToolpop.showAtLocation(book_image, Gravity.BOTTOM, 0, 0);
        topBar.setVisibility(View.VISIBLE);
        // 点击字体按钮
        if (a == 1) {
            //mToolpop1.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 0);
            mToolpop1.showAtLocation(book_image, Gravity.BOTTOM, 0, 0);
            ImageView fontDiscreBtn = (ImageView) toolpop1.findViewById(R.id.iv_font_discre);
            ImageView fontIncreBtn = (ImageView) toolpop1.findViewById(R.id.iv_font_incre);
            fontDiscreBtn.setOnClickListener(this);
            fontIncreBtn.setOnClickListener(this);
        }
        // 点击亮度按钮
        if (a == 2) {
            //mToolpop2.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 0);
            mToolpop2.showAtLocation(book_image, Gravity.BOTTOM, 0, 0);
            seekBar2 = (SeekBar) toolpop2.findViewById(R.id.seekBar2);
            // 取得当前亮度
            seekBar2.setMax(255);
            // 进度条绑定当前亮度
            seekBar2.setProgress(light);
            seekBar2.setOnSeekBarChangeListener(this);
        }
        // 点击跳转按钮
        if (a == 4) {
            //mToolpop4.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 0);
            mToolpop4.showAtLocation(book_image, Gravity.BOTTOM, 0, 0);
            seekBar4 = (SeekBar) toolpop4.findViewById(R.id.seekBar4);
            markEdit4 = (TextView) toolpop4.findViewById(R.id.markEdit4);
            // jumpPage = sp.getInt(bookPath + "jumpPage", 1);
            float fPercent = ttsControler.getCurrPercent();
            DecimalFormat df = new DecimalFormat("#0");
            String strPercent = df.format(fPercent * 100) + "%";
            markEdit4.setText(strPercent);
            seekBar4.setProgress(Integer.parseInt(df.format(fPercent * 100)));
            seekBar4.setOnSeekBarChangeListener(this);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {

            case R.id.seekBar2:
                // 取得当前进度
                int tmpInt = seekBar2.getProgress();
                // 当进度小于80时，设置成80，防止太黑看不见的后果。
                if (tmpInt < 80) {
                    tmpInt = 80;
                }
                WindowManager.LayoutParams lp = MainActivity.this.getWindow().getAttributes();
                lp.screenBrightness = (float) tmpInt * (1f / 255f);
                MainActivity.this.getWindow().setAttributes(lp);
                light = tmpInt;
                break;
            case R.id.seekBar4:
                markEdit4.setText("" + seekBar4.getProgress() + "%");
                ttsControler.setCurrPercent(seekBar4.getProgress());

                book_image.setImageBitmap(ttsControler.getCurrentPageBitmap());
                speekOrNot();
                break;
            default:
                break;
        }

    }

    @Override
    public void onBackPressed() {
        //实现Home键效果
        //super.onBackPressed();这句话一定要注掉,不然又去调用默认的back处理方式了
        /*Intent i= new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);*/

        if(isSpeeking){
            Toast.makeText(this,"正在阅读，如要退出，需先停止阅读",Toast.LENGTH_SHORT).show();
        }else{
            super.onBackPressed();
        }
    }

    private void saveSp() {

        SharedPreferencesUtils.setParam(this,"light", light);
        SharedPreferencesUtils.setParam(this,"night", isNight);
        SharedPreferencesUtils.setParam(this,"size", ttsControler.getFontSize());
        SharedPreferencesUtils.setParam(this,filepath + "begin", ttsControler.getBegin());
    }

    private void initTopBar() {
        ImageButton back = (ImageButton) findViewById(R.id.ib_back_top);
        ImageButton addMark = (ImageButton) findViewById(R.id.ib_add_mark_top);
        TextView name = (TextView) findViewById(R.id.tv_name_top);
        name.setText(filepath);
        back.setOnClickListener(this);
        addMark.setOnClickListener(this);
    }

    private void setReadBg() {
        ttsControler.setPageBG(isNight);
    }
}
