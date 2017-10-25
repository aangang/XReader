package com.android.xreader.localfile;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.xreader.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gang.an on 2017/10/24.
 */

public class FilePickAdapter extends RecyclerView.Adapter<FilePickAdapter.FSViewHolder> {

    private Context context;
    private List<FileDetail> resultList = new ArrayList<>();
    private List<Boolean> checkStatusMap = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private int colorAccent;
    private File mCurrentDir;

    FilePickAdapter(final Context context, final TextView textView){
        this.context = context;
        colorAccent = fetchAccentColor();
    }

    public void startPick(File dir){
        mCurrentDir = dir;
        resultList = getChildDirs(mCurrentDir);
        notifyDataSetChanged();
    }

    public List<FileDetail> getChildDirs(File dir){
        List<FileDetail> tmpList = new ArrayList<>();
        for(File file: dir.listFiles()){
            final FileDetail detail = new FileDetail(file);
            tmpList.add(detail);
            checkStatusMap.add(false);
        }

        return tmpList;
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    @Override
    public FSViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.file_searcher_item,parent,false);
        return new FSViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FSViewHolder holder, int position) {
        FileDetail file = resultList.get(position);
        holder.title.setText(context.getString(R.string.file_name)+file.getName());
        holder.location.setText(context.getString(R.string.file_path)+file.getPath());
        holder.size.setText(context.getString(R.string.file_size)+file.getSize());
        holder.time.setText(context.getString(R.string.file_last_modified_time)+file.getLastModifiedTime());
        CardView cardView = (CardView) holder.itemView;
        if(checkStatusMap.get(position)){
            cardView.setCardBackgroundColor(colorAccent);
        }else{
            cardView.setCardBackgroundColor(context.getResources().getColor(R.color.cardview_light_background));
        }
    }

    class FSViewHolder extends RecyclerView.ViewHolder{
        TextView title,location,size,time;
        FSViewHolder(View view){
            super(view);
            title = (TextView) view.findViewById(R.id.file_searcher_item_title);
            location = (TextView) view.findViewById(R.id.file_searcher_item_location);
            size = (TextView) view.findViewById(R.id.file_searcher_item_size);
            time = (TextView) view.findViewById(R.id.file_searcher_item_create_time);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkStatusMap.set(getAdapterPosition(),!checkStatusMap.get(getAdapterPosition()));
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }
    public void changeAllCheckBoxStatus(){

        for(Boolean b :checkStatusMap){
            if(b){
                for(int i=0;i<checkStatusMap.size();i++){
                    checkStatusMap.set(i,false);
                }
                notifyDataSetChanged();
                return;
            }
        }

        for(int i=0;i<checkStatusMap.size();i++){
            checkStatusMap.set(i,true);
        }
        notifyDataSetChanged();
    }
    public List<File> getSelectedItems(){
        List<File> list = new ArrayList<>();
        for(int i=0;i<checkStatusMap.size();i++){
            if(checkStatusMap.get(i)){
                list.add(resultList.get(i).getFile());
            }
        }
        return list;
    }
    private int fetchAccentColor() {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }



}
