package com.example.mynotesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mynotesapp.data.NotesContract;
import com.example.mynotesapp.data.NotesDbHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import pl.droidsonroids.gif.GifImageView;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout mmail,mpassword;
    private ProgressBar mpb;
    private int flag_internet;

    private GifImageView gif;
    private LinearLayout lin;
    private TextView gtv;

    private String mailId,password,username1,mail1,phone1,pass1;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int flag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        lin = (LinearLayout)findViewById(R.id.login_everything);
        gif = (GifImageView)findViewById(R.id.login_gif);
        gtv = (TextView)findViewById(R.id.login_tv);

        mmail = (TextInputLayout)findViewById(R.id.text_input_email);
        mpassword = (TextInputLayout)findViewById(R.id.text_input_password);
        mpb = (ProgressBar)findViewById(R.id.pb1);

        internet_connection();
    }

    public void toRegister(View view){
        startActivity(new Intent(LoginActivity.this,SignUp.class));
    }
    public boolean validate_main_mail(){
        String my_mail = mmail.getEditText().getText().toString().trim();
        if(TextUtils.isEmpty(my_mail)){
            mmail.setError("This field is required.");
            return false;
        }
        else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(my_mail).matches()){
            mmail.setError("Invalid Email id.");
            return false;
        }
        else{
            mailId = my_mail;
            mmail.setError(null);
            return true;
        }
    }

    public boolean validate_main_password(){
        String my_pass = mpassword.getEditText().getText().toString();
        if (TextUtils.isEmpty(my_pass)) {
            mpassword.setError("This field is required.");
            return false;
        }
        else if(my_pass.length()<6 || my_pass.length()>8){
            if(my_pass.length()<6){
                mpassword.setError("Password should be of minimum 6 characters long.");
            }
            else{
                mpassword.setError("Password should be of maximum 8 characters long.");
            }
            return false;
        }
        else{
            password = my_pass;
            mpassword.setError(null);
            return true;
        }
    }
    public void confirm(View view){
        if(!validate_main_mail() | !validate_main_password()){
            Toast.makeText(LoginActivity.this,"Error in logging in.",Toast.LENGTH_SHORT).show();
            return;
        }
        verification();
    }

    public void forgotPassword(View view){
        startActivity(new Intent(LoginActivity.this,OTP.class));
    }
    public void verification(){
        mpb.setVisibility(View.VISIBLE);
        DocumentReference docref = db.collection("User Login Details").document(mailId);
        docref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    pass1 = documentSnapshot.getString("Password");
                    if(pass1.equals(password)){
                        phone1 = documentSnapshot.getString("Phone");
                        username1 = documentSnapshot.getString("User name");

                        NotesDbHelper mDbHelper = new NotesDbHelper(LoginActivity.this);
                        SQLiteDatabase database = mDbHelper.getWritableDatabase();

                        ContentValues values = new ContentValues();
                        values.put(NotesContract.NotesEntry.COLUMN_EMAIL,mailId);
                        values.put(NotesContract.NotesEntry.COLUMN_PASSWORD,pass1);

                        long id = database.insert(NotesContract.NotesEntry.TABLE_NAME,null,values);

                        if(id!=-1){
                            mpb.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this,"Login Successful!",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                            intent.putExtra("email",mailId);
                            intent.putExtra("phone",phone1);
                            intent.putExtra("username",username1);
                            startActivity(intent);
                        }
                    }
                    else{
                        Toast.makeText(LoginActivity.this,"Wrong password!",Toast.LENGTH_SHORT).show();
                        mpb.setVisibility(View.INVISIBLE);
                    }

                }
                else{
                    Toast.makeText(LoginActivity.this,"No account found with this mail id.",Toast.LENGTH_SHORT).show();
                    mpb.setVisibility(View.INVISIBLE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this,"You are Offline!",Toast.LENGTH_SHORT).show();
                mpb.setVisibility(View.INVISIBLE);
            }
        });
    }
    @Override
    public void onBackPressed() {
        if(flag==1){
            moveTaskToBack(true);
        }
        else{
            Toast.makeText(LoginActivity.this,"Press again to exit.",Toast.LENGTH_SHORT).show();
            flag=1;
            Handler hndler = new Handler();
            hndler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    flag=0;
                }
            },2700);
        }

    }
    private boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            if(nInfo != null && nInfo.isAvailable() && nInfo.isConnected()){
                connected = true;
            }
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }
    private void internet_connection(){
        if (!isConnected()) {
            if(flag_internet==0) {
                Toast.makeText(LoginActivity.this, "No internet connection.", Toast.LENGTH_SHORT).show();
                flag_internet=1;
            }

            lin.setVisibility(View.GONE);
            gif.setVisibility(View.VISIBLE);
            gtv.setVisibility(View.VISIBLE);
            getSupportActionBar().hide();
        }
        else{
            if(flag_internet==1) {
                Toast.makeText(LoginActivity.this, "Connected to internet.", Toast.LENGTH_SHORT).show();
                flag_internet = 0;
            }
            lin.setVisibility(View.VISIBLE);
            gif.setVisibility(View.GONE);
            gtv.setVisibility(View.GONE);
            getSupportActionBar().show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                internet_connection();
            }
        },2000);
    }
}
