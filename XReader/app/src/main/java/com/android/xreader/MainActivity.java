package com.android.xreader;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.android.xreader.db.DBManager;
import com.android.xreader.module.BookFile;
import com.android.xreader.utils.FusionField;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity implements OnSeekBarChangeListener, OnClickListener {


    public static final String tag = "txt";

    private static final String TAG = "BookActivity";

    public static final int DIR_CODE = 123;

    public static final String DIR_KEY = "begin";

    public static final String DIR_NAME = "filepath";

    //private PageWidget mPageWidget;

    Bitmap mCurPageBitmap, mNextPageBitmap;

    Canvas mCurPageCanvas, mNextPageCanvas;

    //BookPageFactory pagefactory;

    private String filepath;

    private int width;

    private int height;

    private static int begin = 0;// 记录的书籍开始位置

    private SharedPreferences sp;

    private SharedPreferences.Editor editor;

    private int light; // 亮度值

    private int size; // 字体大小

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

    //private List<BookMark> bookmarks;

    private static final int FONT_STEP = 2;

    private int mCurrentFontSize; // 字体大小

    private Boolean mIsMainPopupWindowShowing = false;// 主popwindow是否显示
    private boolean mIsSubPopUpWindowShowing = false;

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

        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();
        // 读取SP记录
        begin = sp.getInt(filepath + "begin", 0);
        light = sp.getInt("light", 5);
        isNight = sp.getBoolean("night", false);
        size = sp.getInt("size", defaultSize);
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

        RelativeLayout rlayout = (RelativeLayout) findViewById(R.id.readlayout);
        topBar = findViewById(R.id.top_bar);
        //rlayout.addView(mPageWidget);
        // 工厂
        //pagefactory = new BookPageFactory(this, width, height);

        BookFile bookFile = (BookFile) getIntent().getExtras().getSerializable("path");
        filepath = bookFile.name;

        initTopBar();

        // 阅读背景
        setReadBg();

        /*try {
            if (bookFile.flag.equals("1")) {
                pagefactory.openbook(bookFile.path, begin);
            } else {
                pagefactory.openbook(filecatchpath + "catch.txt", begin);
            }
            pagefactory.setM_fontSize(size);
            pagefactory.onDraw(mCurPageCanvas);
        } catch (IOException e1) {
            e1.printStackTrace();
            Toast.makeText(this, "no find file", Toast.LENGTH_SHORT).show();
        }*/

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

        //mCurrentFontSize = pagefactory.getM_fontSize();

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

                /*mPageWidget.abortAnimation();
                pagefactory.onDraw(mCurPageCanvas);
                pagefactory.onDraw(mNextPageCanvas);
                mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
                mPageWidget.postInvalidate();*/
            }
        });
    }

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

            default:
                break;
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

    }



    private void saveSp() {
        editor.putInt("light", light);
        editor.putBoolean("night", isNight);
        editor.putInt("size", size);
        editor.putInt(filepath + "begin", begin);
        editor.commit();
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
        /*pagefactory.setTextColor(isNight);
        if (isNight) {
            pagefactory.setBgBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.bg_book_night));
        } else {
            pagefactory.setBgBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.bg_book_day));
        }*/
    }
}
