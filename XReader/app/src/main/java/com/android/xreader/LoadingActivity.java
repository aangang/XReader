package com.android.xreader;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Toast;


import com.android.xreader.utils.CopyFileListener;
import com.android.xreader.utils.FileManager;
import com.android.xreader.utils.Tools;
import com.android.xreader.views.CircleProgressbar;

import java.io.File;

public class LoadingActivity extends BaseActivity implements OnClickListener {

    private CircleProgressbar mCircleProgressbar;

    private boolean isClick = false;
    private boolean dataReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading);
        initView();
        mHandler.sendEmptyMessage(0);
    }

    private void initView() {
        mCircleProgressbar = (CircleProgressbar) findViewById(R.id.tv_red_skip);
        mCircleProgressbar.setOutLineColor(Color.TRANSPARENT);
        mCircleProgressbar.setInCircleColor(Color.parseColor("#505559"));
        mCircleProgressbar.setProgressColor(Color.parseColor("#1BB079"));
        mCircleProgressbar.setProgressLineWidth(5);
        mCircleProgressbar.setProgressType(CircleProgressbar.ProgressType.COUNT);
        mCircleProgressbar.setTimeMillis(3000);
        mCircleProgressbar.reStart();

        mCircleProgressbar.setCountdownProgressListener(1,progressListener);

        mCircleProgressbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                isClick = true;
                if(dataReady){
                    startActivity(new Intent(LoadingActivity.this,BookShelfActivity.class));
                    finish();
                }
            }
        });
    }

    private CircleProgressbar.OnCountdownProgressListener progressListener = new CircleProgressbar.OnCountdownProgressListener() {
        @Override
        public void onProgress(int what, int progress)
        {

            if(what==1 && progress==100 && !isClick)
            {
                startActivity(new Intent(LoadingActivity.this,BookShelfActivity.class));
                finish();
                Tools.log("onProgress: ==" + progress);
            }

        }
    };

    /**
     * 复制线程
     */
    private Thread copyDbThread = new Thread() {
        public void run() {
            FileManager.getInstance().moveToSystemDatabaseDir(copyDB);
        }
    };

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    copyDbThread.start();
                    break;
                case 1:
                    Log.i("txt","mkdir");
                    // 判断是否有SD卡
                    if (ExistSDCard()) {
                        File file = new File(FileManager.FILE_SDCARD_PATH);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                    }
                    Log.i("txt","start BookShelfActivity");
                    //startActivity(new Intent(LoadingActivity.this,BookShelfActivity.class));
                    //finish();
                    Toast.makeText(LoadingActivity.this, R.string.copy_finish, Toast.LENGTH_LONG).show();
                    dataReady = true;
                    break;
            }
        }
    };

    private CopyFileListener copyDB = new CopyFileListener() {
        @Override
        public void onCopyFinish() {
            mHandler.sendEmptyMessage(1);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }

    private boolean ExistSDCard() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }
}

