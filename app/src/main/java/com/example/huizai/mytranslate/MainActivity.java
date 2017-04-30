package com.example.huizai.mytranslate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;

import com.victor.loading.rotate.RotateLoading;
import com.example.huizai.mytranslate.bean.Word;


public class MainActivity extends AppCompatActivity
{
    static String searchHistoryTag;
    ArrayList<String> historyData;
    ListView historyListView;
    EditText searchEditText;
    Button searchButton;
    RotateLoading loading;


    @Override
    protected void onStart() {
        super.onStart();
        getDelegate().onStart();
        loadSearchWord();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loading=(RotateLoading)findViewById(R.id.rotateloading);
        historyData=new ArrayList<>();
        searchEditText=(EditText)findViewById(R.id.searchEditText);
        historyListView=(ListView)findViewById(R.id.historyListView);
        searchButton=(Button)findViewById(R.id.searchButton);
        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchEditText.setText(historyData.get(position));
                if (isNetworkConnected(MainActivity.this.getApplicationContext()))
                {
                    loading.start();
                    new Thread(networkTask).start();
                }
                else
                {
                    Toast.makeText(MainActivity.this,"当前网络不可用",0).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public void search(View view)
    {
        if (isNetworkConnected(MainActivity.this.getApplicationContext()))
        {
            historyListView.setClickable(false);
            searchButton.setClickable(false);
            String w=searchEditText.getText().toString().replace(" ","");
            if (!w.equals(""))
            {
                new Thread(networkTask).start();
                loading.start();
            }
        }
        else
        {
            Toast.makeText(MainActivity.this,"当前网络不可用",0).show();
        }
    }


    Runnable networkTask = new Runnable() {
        @Override
        public void run()
        {
            postSearch();
        }
    };

    private void postSearch()
    {
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            String wordName=searchEditText.getText()+"";
            URL url = new URL("http://fy.iciba.com/ajax.php?a=fy");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("POST");
            String data = "f=auto&t=auto&w="+wordName;
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(data.getBytes());
            int responseCode = connection.getResponseCode();
            if(responseCode ==200){
                InputStream is = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String strRead = null;
                while ((strRead = reader.readLine()) != null) {
                    sbf.append(strRead);
                    sbf.append("\r\n");
                }
                reader.close();
                Word word=jsonDecode(sbf.toString(),wordName);
                recordSearchWord(wordName);
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, vocabularyActivity.class);
                Bundle bundle=new Bundle();
                bundle.putSerializable("word",word);
                intent.putExtras(bundle);
                MainActivity.this.startActivity(intent);
            }
            else
            {
                mHandler.sendEmptyMessage(1);
                return;
            }
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        } catch (ProtocolException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        mHandler.sendEmptyMessage(0);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {//此方法在ui线程运行
            switch(msg.what)
            {
                case 0:
                    loading.stop();
                    searchButton.setClickable(true);
                    historyListView.setClickable(true);
                    break;
                case 1:
                    Toast.makeText(MainActivity.this,"无法搜索",0).show();
                    break;
            }
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.UnknownWord:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, UnknownWordActivity.class);
                MainActivity.this.startActivity(intent);
                break;
            case R.id.clearHistory:
                SharedPreferences h=getSharedPreferences("data",MODE_PRIVATE);
                SharedPreferences.Editor e=h.edit();
                e.remove(searchHistoryTag);
                e.commit();
                loadSearchWord();
                break;
        }

        return true;
    }

    private Word jsonDecode(String json,String wordName)
    {
        Word word = new Word();
        try {
            JSONObject jsonObject = new JSONObject(json);
            int status = jsonObject.getInt("status");
            JSONObject contentJson = jsonObject.getJSONObject("content");
            word.setStatus(status);
            if (status==1)
            {
                word.setOut(contentJson.getString("out"));
            }
            else
            {
                JSONArray wordMeanJsonArray = contentJson.getJSONArray("word_mean");
                List<String> wordMeanList=new ArrayList<>();
                for (int i=0;i<wordMeanJsonArray.length();i++)
                {
                    wordMeanList.add(wordMeanJsonArray.getString(i));
                }
                word.setWordMean(wordMeanList);
                word.setPhTtsMp3(contentJson.getString("ph_tts_mp3"));
            }
            word.setWordName(wordName);
            return word;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return word;
    }


    private void loadSearchWord()
    {
        historyData.clear();
        SharedPreferences h=getSharedPreferences("data",MODE_PRIVATE);
        try {
            JSONArray jsonArray = new JSONArray(h.getString(searchHistoryTag,"[]"));
            for (int i = 0; i < jsonArray.length(); i++) {
                historyData.add(jsonArray.getString(i));
            }
            wordListAdapter ad=new wordListAdapter(MainActivity.this,R.layout.historylistlayout,historyData);
            historyListView.setAdapter(ad);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recordSearchWord(String searchWord)
    {
        historyData.clear();
        SharedPreferences h=getSharedPreferences("data",MODE_PRIVATE);
        SharedPreferences.Editor e=h.edit();
        try
        {
            JSONArray jsonArray = new JSONArray(h.getString(searchHistoryTag,"[]"));
            for (int i = 0; i < jsonArray.length(); i++)
            {
                historyData.add(jsonArray.getString(i));
            }

            for (int i=0;i<historyData.size();i++)
            {
                if (historyData.get(i).equals(searchWord))
                {
                    historyData.remove(i);
                    break;
                }
            }
            historyData.add(0,searchWord);
            JSONArray newJsonArray = new JSONArray(historyData);
            e.putString(searchHistoryTag,newJsonArray.toString());
            e.commit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
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
