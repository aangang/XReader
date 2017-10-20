package com.android.xreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
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
import com.android.xreader.tts.KqwSpeechCompound;
import com.android.xreader.tts.TtsSettings;
import com.android.xreader.utils.FusionField;
import com.android.xreader.utils.SharedPreferencesUtils;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class MainActivity extends Activity implements OnSeekBarChangeListener, OnClickListener {


    public static final String tag = "txt";

    private static final String TAG = "BookActivity";

    public static final int DIR_CODE = 123;
    public static final int SETTING_CODE = 234;

    public static final String DIR_KEY = "begin";

    public static final String DIR_NAME = "filepath";

    //private PageWidget mPageWidget;

    Bitmap mCurPageBitmap, mNextPageBitmap;

    Canvas mCurPageCanvas, mNextPageCanvas;

    BookPageFactory pagefactory;

    private String filepath;

    private int width;

    private int height;

    private static int begin = 0;// 记录的书籍开始位置

    private int light; // 亮度值

    private static String word = "";// 记录当前页面的文字

    // catch路径
    private String filecatchpath = "/data/data/" + FusionField.baseActivity.getPackageName() + "/";

    private PopupWindow mPopupWindow, mToolpop, mToolpop1, mToolpop2, mToolpop4;

    private View popupwindwow, toolpop, toolpop1, toolpop2, toolpop4, topBar;

    private SeekBar seekBar1, seekBar2, seekBar4;

    private int a = 0, b = 0;// 记录toolpop的位置

    private boolean isNight; // 亮度模式,白天和晚上

    private TextView markEdit4;

    int defaultSize = 30;

    // int readHeight; // 电子书显示高度
    private Context mContext = null;

    private DBManager mgr;

    private List<BookMark> bookmarks;

    private static final int FONT_STEP = 2;

    private int mCurrentFontSize; // 字体大小

    private Boolean mIsMainPopupWindowShowing = false;// 主popwindow是否显示
    private boolean mIsSubPopUpWindowShowing = false;

    ImageView book_image;
    Button prev,next;

    //tts
    private KqwSpeechCompound mKqwSpeechCompound;
    Vector<String> curPageLines;

    String mPageLines = "";
    boolean isSpeeking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 竖屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mgr = new DBManager(this);

        mContext = getBaseContext();

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;

        // 读取SP记录
        begin =(int)SharedPreferencesUtils.getParam(this,filepath + "begin", 0);
        light =(int)SharedPreferencesUtils.getParam(this,"light", 5);
        isNight =(boolean)SharedPreferencesUtils.getParam(this,"night", false);
        mCurrentFontSize =(int)SharedPreferencesUtils.getParam(this,"size", defaultSize);

        //set brightness
        WindowManager.LayoutParams lp = MainActivity.this.getWindow().getAttributes();
        lp.screenBrightness = (float) light * (1f / 255f);
        MainActivity.this.getWindow().setAttributes(lp);

        //page view
        //mPageWidget = new PageWidget(this, width, height);
        // 当前页
        mCurPageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 下一页
        mNextPageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 画布
        mCurPageCanvas = new Canvas(mCurPageBitmap);
        mNextPageCanvas = new Canvas(mNextPageBitmap);

        setContentView(R.layout.read);

        // 初始化语音合成对象
        mKqwSpeechCompound = new KqwSpeechCompound(this);
        mKqwSpeechCompound.setTtsListener(mTtsListener);

        RelativeLayout rlayout = (RelativeLayout) findViewById(R.id.readlayout);
        topBar = findViewById(R.id.top_bar);
        //rlayout.addView(mPageWidget);
        // 工厂
        pagefactory = new BookPageFactory(this, width, height);

        BookFile bookFile = (BookFile) getIntent().getExtras().getSerializable("path");
        filepath = bookFile.name;

        initTopBar();

        // 阅读背景
        setReadBg();

        try {
            if (bookFile.flag.equals("1")) {
                pagefactory.openbook(bookFile.path, begin);
            } else {
                pagefactory.openbook(filecatchpath + "catch.txt", begin);
            }
            log("book opened :" + bookFile.name);
            pagefactory.setM_fontSize(mCurrentFontSize);
            pagefactory.onDraw(mCurPageCanvas);
        } catch (IOException e1) {
            e1.printStackTrace();
            Toast.makeText(this, "no find file", Toast.LENGTH_SHORT).show();
        }
        prev = (Button) findViewById(R.id.prev);
        next = (Button) findViewById(R.id.next);
        prev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                log("prev  clicked");
                toPrePage();
            }
        });
        next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                log("next  clicked");
                toNextPage();
            }
        });
        book_image = (ImageView) findViewById(R.id.book_view);
        book_image.setImageBitmap(mCurPageBitmap);

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

        mCurrentFontSize = pagefactory.getM_fontSize();

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
        rlayout.setOnClickListener(new OnClickListener() {
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
        });

    }

    public static void log(String string){
        Log.i(tag, string);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    public void toPrePage() {
        // TODO Auto-generated method stub
        try {
            pagefactory.prePage();
            begin = pagefactory.getM_mbBufBegin();// 获取当前阅读位置
            word = pagefactory.getFirstLineText();// 获取当前阅读位置的首行文字
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (pagefactory.isfirstPage()) {
            Toast.makeText(mContext, "当前是第一页", Toast.LENGTH_SHORT).show();
        }
        pagefactory.onDraw(mNextPageCanvas);
        //mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
        book_image.setImageBitmap(mNextPageBitmap);
        SharedPreferencesUtils.setParam(this,filepath + "begin", begin);
    }


    public void toNextPage() {
        // TODO Auto-generated method stub
        try {
            pagefactory.nextPage();
            begin = pagefactory.getM_mbBufBegin();// 获取当前阅读位置
            word = pagefactory.getFirstLineText();// 获取当前阅读位置的首行文字
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (pagefactory.islastPage()) {
            Toast.makeText(mContext, "已经是最后一页了", Toast.LENGTH_SHORT).show();
        }
        pagefactory.onDraw(mNextPageCanvas);
        //mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
        book_image.setImageBitmap(mNextPageBitmap);
        SharedPreferencesUtils.setParam(this,filepath + "begin", begin);
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
        final TextView btnSpeek = (TextView) popupwindwow.findViewById(R.id.btn_speek);
        TextView btnSetting = (TextView) popupwindwow.findViewById(R.id.btn_setting);
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
                /*String line =pagefactory.getFirstLineText();
                if(pagefactory != null && line != null && line != ""){
                    mKqwSpeechCompound.speaking(line);
                }*/
                if(!mKqwSpeechCompound.isSpeaking()) {
                    curPageLines = pagefactory.getCurrentPageLines();
                    String page = "";
                    for (String line : curPageLines) {
                        page = page + line;
                    }
                    log(page);
                    btnSpeek.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.pause_ic,0,0);
                    mPageLines = page;
                    mKqwSpeechCompound.speaking(page);
                    isSpeeking = true;
                    log("speek");
                }else{
                    mKqwSpeechCompound.stopSpeaking();
                    isSpeeking = false;
                    btnSpeek.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.play_ic,0,0);
                }
            }
        });
        btnSetting.setOnClickListener(this);

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

                pagefactory.onDraw(mCurPageCanvas);
                book_image.setImageBitmap(mCurPageBitmap);
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
        if(mKqwSpeechCompound.isSpeaking()){
            mKqwSpeechCompound.stopSpeaking();
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
                String page = "";
                curPageLines = pagefactory.getCurrentPageLines();
                for(String line:curPageLines){
                    page = page + line;
                }
                log(page);
                mPageLines = page;
                if(page != null && !page.equals("")) {
                    log("speeking continue");
                    mKqwSpeechCompound.speaking(page);
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
    protected void onDestroy() {
        super.onDestroy();
        mgr.closeDB();
        saveSp();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ib_back_top:
                finish();
                break;
            case R.id.ib_add_mark_top:
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
                mCurrentFontSize -= FONT_STEP;
                pagefactory.setTextSize(mCurrentFontSize);
                pagefactory.onDraw(mCurPageCanvas);
                pagefactory.onDraw(mNextPageCanvas);
                //mPageWidget.invalidate();
                book_image.setImageBitmap(mCurPageBitmap);
                break;

            case R.id.iv_font_incre:
                mCurrentFontSize += FONT_STEP;
                pagefactory.setTextSize(mCurrentFontSize);
                pagefactory.onDraw(mCurPageCanvas);
                pagefactory.onDraw(mNextPageCanvas);
                //mPageWidget.invalidate();
                book_image.setImageBitmap(mCurPageBitmap);
                break;

            case R.id.btn_setting:
                Intent setting = new Intent(MainActivity.this,TtsSettings.class);
                startActivityForResult(setting,SETTING_CODE);
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
                    int markBegin = data.getExtras().getInt(DIR_KEY);
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
                    }
                }
                break;
            case SETTING_CODE:
                log("from tts setting");
                if(isSpeeking) {
                    mKqwSpeechCompound.stopSpeaking();
                    mKqwSpeechCompound.speaking(mPageLines);
                }

                break;
        }
    }

    public static String getStringCurrentDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(currentTime);
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
            float fPercent = (float) (begin * 1.0 / pagefactory.getM_mbBufLen());
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

                begin = pagefactory.getM_mbBufLen() * seekBar4.getProgress() / 100;
                if (begin > 0) {
                    try {
                        pagefactory.nextPage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    pagefactory.setM_mbBufEnd(begin);
                    pagefactory.setM_mbBufBegin(begin);
                    pagefactory.onDraw(mNextPageCanvas);
                    //mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
                    //mPageWidget.invalidate();
                    //postInvalidateUI();
                    book_image.setImageBitmap(mNextPageBitmap);
                    postInvalidateUI();
                }
                break;
            default:
                break;
        }

    }

    /**
     * 刷新界面
     */
    public void postInvalidateUI() {
        //mPageWidget.abortAnimation();
        pagefactory.onDraw(mCurPageCanvas);
        try {
            pagefactory.currentPage();
            begin = pagefactory.getM_mbBufBegin();// 获取当前阅读位置
            word = pagefactory.getFirstLineText();// 获取当前阅读位置的首行文字
        } catch (IOException e1) {
        }

        pagefactory.onDraw(mNextPageCanvas);

        //mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
        //mPageWidget.postInvalidate();
        book_image.setImageBitmap(mNextPageBitmap);
    }



    private void saveSp() {

        SharedPreferencesUtils.setParam(this,"light", light);
        SharedPreferencesUtils.setParam(this,"night", isNight);
        SharedPreferencesUtils.setParam(this,"size", mCurrentFontSize);
        SharedPreferencesUtils.setParam(this,filepath + "begin", begin);
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
        pagefactory.setTextColor(isNight);
        if (isNight) {
            pagefactory.setBgBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.bg_book_night));
        } else {
            pagefactory.setBgBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.bg_book_day));
        }
    }
}
