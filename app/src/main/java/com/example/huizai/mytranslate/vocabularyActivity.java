package com.example.huizai.mytranslate;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import com.example.huizai.mytranslate.bean.Word;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class vocabularyActivity extends AppCompatActivity {
    static String unknownWordTag="unknownWordTag";
    int state;
    private Word word;
    private TextView wordTextView;
    private TextView meanTextView;
    private Button soundButton;
    private MediaPlayer mediaPlayer;
    Button addOrRemoveWordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary);
        wordTextView=(TextView)findViewById(R.id.wordTextView);
        meanTextView=(TextView)findViewById(R.id.meanTextView);
        soundButton=(Button) findViewById(R.id.soundButton);
        mediaPlayer = new MediaPlayer();
        addOrRemoveWordButton=(Button)findViewById(R.id.addOrRemoveWordButton);
        setUp();
        setAddOrRemoveWordButton();
    }

    void setAddOrRemoveWordButton()
    {
        SharedPreferences h=getSharedPreferences("data",MODE_PRIVATE);
        try {
            JSONArray jsonArray = new JSONArray(h.getString(unknownWordTag, "[]"));
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject JO=jsonArray.getJSONObject(i);
                String wordName=JO.getString("wordName");
                if (wordName.equals(word.getWordName()))
                {
                    addOrRemoveWordButton.setText("从生词本中移除");
                    state=1;
                }
                else
                {
                    addOrRemoveWordButton.setText("加入生词本");
                    state=0;
                }
            }
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }

        addOrRemoveWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SharedPreferences h=getSharedPreferences("data",MODE_PRIVATE);
                SharedPreferences.Editor e=h.edit();
                try {
                    JSONObject o = new JSONObject();
                    o.put("status", word.getStatus());
                    if (word.getStatus() == 1) {
                        o.put("wordName", word.getWordName());
                        o.put("out", word.getOut());
                    } else {
                        o.put("wordName", word.getWordName());
                        o.put("phTtsMp3", word.getPhTtsMp3());
                        JSONArray a = new JSONArray(word.getWordMean());
                        o.put("wordMean", a.toString());
                    }

                    JSONArray jsonArray = new JSONArray(h.getString(unknownWordTag,"[]"));
                    if (state==0)
                    {
                        jsonArray.put(o);
                        e.putString(unknownWordTag,jsonArray.toString());
                        addOrRemoveWordButton.setText("从生词本中移除");
                        state=1;
                    }
                    else
                    {
                        for (int i=0;i<jsonArray.length();i++)
                        {
                            JSONObject p=jsonArray.getJSONObject(i);
                            if ( p.getString("wordName").equals(word.getWordName()))
                            {
                                jsonArray.remove(i);
                                break;
                            }
                        }
                        e.putString(unknownWordTag,jsonArray.toString());
                        addOrRemoveWordButton.setText("加入生词本");
                        state=0;
                    }
                    e.commit();
                }catch (JSONException ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }

    void setUp()
    {
        word = (Word) getIntent().getSerializableExtra("word");
        wordTextView.setText(word.getWordName());
        try
        {
            if (word.getStatus()==0)
            {
                List<String> meanList=word.getWordMean();
                String meanText="";
                for (int i=0;i<meanList.size();i++)
                {
                    meanText+=meanList.get(i)+"\n";
                }
                meanTextView.setText(meanText);
                if (isNetworkConnected(vocabularyActivity.this.getApplicationContext()))
                {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(word.getPhTtsMp3());
                    mediaPlayer.prepare();
                    soundButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            if (!mediaPlayer.isPlaying())
                            {
                                mediaPlayer.start();
                            }

                        }
                    });
                }
                else
                {
                    soundButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            Toast.makeText(vocabularyActivity.this,"当前网络不可用",0).show();
                        }
                    });
                }
            }
            else
            {
                soundButton.setAlpha(0);
                String out=word.getOut();
                out=out.replace("<br/>","");
                if (out.equals(word.getWordName()))
                {
                    meanTextView.setText("没有该词的解释");
                }
                else
                {
                    meanTextView.setText(out);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public boolean isNetworkConnected(Context context)
    {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
}
