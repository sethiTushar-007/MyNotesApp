package com.example.mynotesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mynotesapp.data.NotesContract;
import com.example.mynotesapp.data.NotesDbHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Splash extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressBar pb1;
    private NotesDbHelper mDbHelper;
    private ImageView img;
    private TextView t1,t2,tryUsing;
    private Animation a1,a2,a3,a4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        pb1 = (ProgressBar) findViewById(R.id.pb_splash_1);
        t1 = (TextView)findViewById(R.id.t1);
        t2 = (TextView)findViewById(R.id.t2);
        tryUsing = (TextView)findViewById(R.id.tryUsing);
        img = (ImageView)findViewById(R.id.image);

        mDbHelper = new NotesDbHelper(this);
        a1 = AnimationUtils.loadAnimation(this,R.anim.from_top);
        a2 = AnimationUtils.loadAnimation(this,R.anim.from_left);
        a3 = AnimationUtils.loadAnimation(this,R.anim.from_right);
        a4 = AnimationUtils.loadAnimation(this,R.anim.from_bottom);


        t1.setAnimation(a1);
        img.setAnimation(a2);
        tryUsing.setAnimation(a3);
        t2.setAnimation(a4);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pb1.setVisibility(View.VISIBLE);

                SQLiteDatabase database = mDbHelper.getReadableDatabase();

                String[] projection = {
                        NotesContract.NotesEntry.COLUMN_EMAIL
                };

                Cursor cursor = database.query(NotesContract.NotesEntry.TABLE_NAME,projection,null,null,null,null,null);

                if(cursor.getCount()==0){
                    startActivity(new Intent(Splash.this,LoginActivity.class));
                    finish();
                }
                else{
                    while(cursor.moveToNext()){
                        String email = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_EMAIL));
                        Intent intent = new Intent(Splash.this,MainActivity.class);
                        intent.putExtra("email",email);
                        cursor.close();
                        startActivity(intent);
                        finish();
                    }
                }
            }
        }, 2700);

    }

    @Override
    public void onBackPressed() {

    }
}
