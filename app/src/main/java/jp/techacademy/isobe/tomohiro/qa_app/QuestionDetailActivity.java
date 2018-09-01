package jp.techacademy.isobe.tomohiro.qa_app;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity implements DatabaseReference.CompletionListener {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mAnswerRef;
    private DatabaseReference mFavoritesRef;

    private boolean favorite;



    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for (Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener mFavoriteEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            favorite = true;

            FloatingActionButton fab2 = (FloatingActionButton)findViewById(R.id.fab2);
            fab2.setImageResource(R.drawable.hart2);

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();



        // お気に入りの追加
        FloatingActionButton fab2 = (FloatingActionButton)findViewById(R.id.fab2);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference favoRef = databaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());

        favoRef.addChildEventListener(mFavoriteEventListener);



        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                DatabaseReference favoRef = databaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
                FloatingActionButton fab2 = (FloatingActionButton)findViewById(R.id.fab2);

                Map<String, String> data = new HashMap<String, String>();
                data.put("Genre", String.valueOf(mQuestion.getGenre()));

                // お気に入り追加と削除の場合分け
                if (favorite) {
                    favoRef.removeValue();
                    fab2.setImageResource(R.drawable.hart);
                    Snackbar.make(v, "お気に入りから削除しました", Snackbar.LENGTH_LONG).show();
                    favorite = false;


                } else {
                    favoRef.setValue(data);
                    fab2.setImageResource(R.drawable.hart2);
                    Snackbar.make(v, "お気に入りに追加しました", Snackbar.LENGTH_LONG).show();
                    favorite = true;
                }

            }
        });



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    //questionを渡す
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });


        mAnswerRef = databaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }






    @Override
    public void onResume() {
        super.onResume();

        // ログイン済みのユーザーを取得する, お気に入りボタン表示の場合分け
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FloatingActionButton fab2 = (FloatingActionButton)findViewById(R.id.fab2);

        if (user == null) {
            fab2.hide();
        } else {
            fab2.show();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference favoRef = databaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
            if (favoRef.equals(mQuestion.getQuestionUid())) {
                fab2.setImageResource(R.drawable.hart2);
            }
        }
    }

    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

        if (databaseError == null) {
            Snackbar.make(findViewById(android.R.id.content), "お気に入りに追加しました", Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(findViewById(android.R.id.content), "お気に入りに追加に失敗しました", Snackbar.LENGTH_LONG).show();
        }
    }
}
