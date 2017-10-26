package com.android.xreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;


import com.android.xreader.db.DBManager;
import com.android.xreader.module.BookMark;
import com.android.xreader.utils.Tools;

import java.util.List;

public class DirectoryActivity extends Activity {
    private DBManager mgr;

    private List<BookMark> bookmarks;

    private TextView mBookName;

    private TextView mEmptyView;

    private ListView mListView;

    private MarkAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_directory);

        mgr = new DBManager(this);
        mBookName = (TextView) findViewById(R.id.tv_name_dir);
        TextView mAuthor = (TextView) findViewById(R.id.tv_author_dir);
        mAuthor.setVisibility(View.GONE);
        mEmptyView = (TextView) findViewById(R.id.empty_view_dir);
        mListView = (ListView) findViewById(R.id.lv_dir);
        ImageButton mBack = (ImageButton) findViewById(R.id.ib_back_dir);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                Tools.log("mark  position:" + position);
                intent.putExtra(MainActivity.DIR_KEY, bookmarks.get(position).begin);
                setResult(MainActivity.DIR_CODE, intent);
                finish();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mAdapter = new MarkAdapter();
        init(getIntent().getExtras().getString(MainActivity.DIR_NAME));
    }

    private void init(String filepath) {
        Tools.log("book name:" + filepath);
        bookmarks = mgr.queryMarks(filepath);
        if (bookmarks.size() <= 0) {
            mBookName.setText(filepath);
            mListView.setVisibility(View.GONE);
            mEmptyView.setText("你还没有添加书签");
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mBookName.setText(filepath);
            mListView.setAdapter(mAdapter);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    class MarkAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public MarkAdapter() {
            mInflater = (LayoutInflater) DirectoryActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return bookmarks.size();
        }

        @Override
        public Object getItem(int position) {
            return bookmarks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            BookMark mark = bookmarks.get(position);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.mark_item, null);
                holder = new Holder();
                holder.word = (TextView) convertView.findViewById(R.id.word_mark);
                holder.time = (TextView) convertView.findViewById(R.id.time_mark);
                holder.time.setVisibility(View.GONE);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.word.setText(mark.word);
            holder.time.setText(mark.time);
            return convertView;
        }

        private class Holder {
            TextView word, time;
        }
    }
}
