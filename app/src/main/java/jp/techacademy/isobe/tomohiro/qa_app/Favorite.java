package jp.techacademy.isobe.tomohiro.qa_app;

import java.io.Serializable;


public class Favorite implements Serializable {

    private String mUid;
    private String mQuestionUid;
    private int mGenre;



    public String getUid() {
        return mUid;
    }

    public String getQuestionUid() {
        return mQuestionUid;
    }

    public int getGenre() {
        return mGenre;
    }





    public Favorite(String questionUid, int genre) {

        mQuestionUid = questionUid;
        mGenre = genre;

    }
}
