package com.android.xreader.localfile;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.xreader.R;
import com.android.xreader.utils.Tools;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by gang.an on 2017/10/24.
 */

public class FilePickerActivity extends AppCompatActivity {

    public static final int OK = 0;
    public static final int NO_DATA_SELECTED = 1;
    FilePickAdapter mAdapter;
    TextView title;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        Intent intent = getIntent();
        int themeRes = intent.getIntExtra("theme", -1);

        if (themeRes != -1) {
            setTheme(themeRes);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_picker_main);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.file_picker_main_recycler_view);
        title = (TextView) findViewById(R.id.file_picker_main_text);
        mAdapter = new FilePickAdapter(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter.setDirChangeCallback(new PickDictChangeCallback());
        //必须在setDirChangeCallback后执行startPick
        mAdapter.startPick(Environment.getExternalStorageDirectory());
        Toolbar toolbar = (Toolbar) findViewById(R.id.file_picker_main_toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    class PickDictChangeCallback implements FilePickAdapter.DictChangeCallback{
        @Override
        public void onDictChange(File curDir) {
            title.setText("当前目录:" + curDir.getName());
        }
    }

    @Override
    public void onBackPressed() {
        try{
            if(mAdapter != null) {
                if (mAdapter.getmCurrentDir().getCanonicalPath().equals("/storage/emulated/0")){
                    Tools.log("root dir,return");
                    //super.onBackPressed();
                    showAlertDialog();
                }else if(mAdapter.getmCurrentDir().getCanonicalPath() != null && !mAdapter.getmCurrentDir().getCanonicalPath().equals("")){
                    File father = mAdapter.getmCurrentDir().getParentFile();
                    mAdapter.startPick(father);
                }
            }
        }catch (Exception e){
        }


    }

    private void showAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.FileSearcherDialogTheme);
        builder.setMessage(getText(R.string.dialog_message_pick));
        builder.setPositiveButton(getText(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FilePickerActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton(getText(R.string.cancel),null);
        builder.create().show();
    }


}
