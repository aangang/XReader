package com.android.xreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.xreader.localfile.FilePickerActivity;
import com.android.xreader.localfile.FileSearcherActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by gang.an on 2017/10/24.
 */

public class SearchActivity extends AppCompatActivity {
    private static final int SEARCH_CODE = 111;
    private static final int LOADBOOK_CODE = 222;

    Button search_btn,byhand_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final EditText editText = (EditText) findViewById(R.id.example_edit);
        search_btn = (Button) findViewById(R.id.search_btn);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = editText.getText().toString();
                if(content.replaceAll(" ","").isEmpty()){
                    Toast.makeText(SearchActivity.this, "请输入搜索关键字", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(SearchActivity.this, FileSearcherActivity.class);
                    intent.putExtra("keyword",content);
                    //intent.putExtra("theme",R.style.SearchTheme);
                    intent.putExtra("min",1024);
                    startActivityForResult(intent,SEARCH_CODE);
                }
            }
        });

        byhand_btn =  (Button) findViewById(R.id.byhand_btn);
        byhand_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = editText.getText().toString();
                Intent intent = new Intent(SearchActivity.this, FilePickerActivity.class);
                intent.putExtra("keyword",content);
                //intent.putExtra("theme",R.style.SearchTheme);
                intent.putExtra("min",50*1024);
                startActivityForResult(intent,LOADBOOK_CODE);
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SEARCH_CODE && resultCode ==FileSearcherActivity.OK && data != null){
            ArrayList<File> list = (ArrayList<File>) data.getSerializableExtra("data");
            Toast.makeText(this,"you selected"+list.size()+"items",Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            intent.putExtra("data",list);
            setResult(BookShelfActivity.SEARCH_OK,intent);
            finish();

        }else if(requestCode == LOADBOOK_CODE && resultCode == LoadBookActivity.OK && data != null){
            Toast.makeText(this,"book loaded",Toast.LENGTH_SHORT).show();

        }

    }




}
