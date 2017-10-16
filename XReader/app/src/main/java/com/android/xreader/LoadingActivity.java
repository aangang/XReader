package com.android.xreader;

import android.content.Intent;
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

import java.io.File;

public class LoadingActivity extends BaseActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading);
        initView();
        mHandler.sendEmptyMessage(0);
    }

    private void initView() {
    }

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
                    startActivity(new Intent(LoadingActivity.this,BookShelfActivity.class));
                    finish();
                    Toast.makeText(LoadingActivity.this, R.string.copy_finish,
                            Toast.LENGTH_LONG).show();
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

