package com.example.huizai.mytranslate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by huizai on 2017/4/29.
 */

public class wordListAdapter extends ArrayAdapter<String>
{

    private int resourceID;
    public wordListAdapter(Context context, int resource , List<String> objects)
    {
        super(context, resource,objects);
        resourceID=resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        String HistoryWord=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceID,null);
        TextView wordName=(TextView)view.findViewById(R.id.wordListTextView);
        wordName.setText(HistoryWord);
        return view;
    }

}
