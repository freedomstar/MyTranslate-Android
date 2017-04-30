package com.example.huizai.mytranslate.bean;
import java.io.Serializable;
import java.util.List;


/**
 * Created by huizai on 2017/4/29.
 */
public class Word implements Serializable
{
    private String out;
    private String wordName;
    private int status;
    private String phTtsMp3;
    private List<String> wordMean;


    public void setOut(String out)
    {
        this.out = out;
    }

    public String getOut() {
        return out;
    }

    public void setWordName(String wordName)
    {
        this.wordName = wordName;
    }

    public String getWordName() {
        return wordName;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }


    public void setPhTtsMp3(String phTtsMp3) {
        this.phTtsMp3 = phTtsMp3;
    }
    public String getPhTtsMp3() {
        return phTtsMp3;
    }

    public void setWordMean(List<String> wordMean) {
        this.wordMean = wordMean;
    }
    public List<String> getWordMean() {
        return wordMean;
    }


}
