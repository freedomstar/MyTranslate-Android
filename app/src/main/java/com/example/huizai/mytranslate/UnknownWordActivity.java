package com.example.huizai.mytranslate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.huizai.mytranslate.bean.Word;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UnknownWordActivity extends AppCompatActivity {
    ArrayList<String> unknownWordData;
    ArrayList<JSONObject> unknownWordJsonData;
    ListView unknownListView;

    @Override
    protected void onPostResume()
    {
        super.onPostResume();
        getDelegate().onPostResume();
        unknownWordData=new ArrayList<>();
        unknownWordJsonData=new ArrayList<>();
        SharedPreferences h=getSharedPreferences("data",MODE_PRIVATE);
        try
        {
            JSONArray jsonArray = new JSONArray(h.getString("unknownWordTag", "[]"));
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject o=jsonArray.getJSONObject(i);
                unknownWordJsonData.add(o);
                unknownWordData.add(o.getString("wordName"));
            }
            wordListAdapter ad=new wordListAdapter(UnknownWordActivity.this,R.layout.historylistlayout,unknownWordData);
            unknownListView.setAdapter(ad);
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unkonw_word);
        unknownListView=(ListView)findViewById(R.id.unknownWordListView);
        unknownListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                try
                {
                    Word word= new Word();
                    JSONObject contentJson = unknownWordJsonData.get(position);
                    int status = contentJson.getInt("status");
                    word.setStatus(status);
                    if (status==1)
                    {
                        word.setOut(contentJson.getString("out"));
                    }
                    else
                    {
                        JSONArray wordMeanJsonArray = new JSONArray(contentJson.getString("wordMean"));
                        List<String> wordMeanList=new ArrayList<>();
                        for (int i=0;i<wordMeanJsonArray.length();i++)
                        {
                            wordMeanList.add(wordMeanJsonArray.getString(i));
                        }
                        word.setWordMean(wordMeanList);
                        word.setPhTtsMp3(contentJson.getString("phTtsMp3"));
                    }
                    word.setWordName(unknownWordData.get(position));
                    Intent intent = new Intent();
                    intent.setClass(UnknownWordActivity.this, vocabularyActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("word",word);
                    intent.putExtras(bundle);
                    UnknownWordActivity.this.startActivity(intent);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
