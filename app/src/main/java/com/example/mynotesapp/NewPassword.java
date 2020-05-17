package com.example.mynotesapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import pl.droidsonroids.gif.GifImageView;

public class NewPassword extends AppCompatActivity {

    private String email,password;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ProgressBar pb5;
    private TextInputLayout t1,t2,t3;
    private int flag_internet;
    private GifImageView gif;
    private TextView gtv;
    private LinearLayout lin;
    private int flag1,flag2,flag_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        flag1=0;
        flag2=0;

        pb5 = (ProgressBar)findViewById(R.id.pb5);

        lin = (LinearLayout)findViewById(R.id.new_password_everything);
        gif = (GifImageView)findViewById(R.id.new_password_gif);
        gtv = (TextView)findViewById(R.id.new_password_tv);

        Intent intent2 = getIntent();
        if(intent2.hasExtra("email")){
            email = intent2.getStringExtra("email");
        }

        t1 = (TextInputLayout)findViewById(R.id.mail1);
        t2 = (TextInputLayout)findViewById(R.id.register_password3);
        t3 = (TextInputLayout)findViewById(R.id.register_password4);

        t1.getEditText().setText(email);
        t1.setEnabled(false);

        internet_connection();
    }
    public boolean validate_password(){
        String pass1 = t2.getEditText().getText().toString().trim();
        String pass2 = t3.getEditText().getText().toString().trim();
        if(TextUtils.isEmpty(pass1)){
            t2.setError("This field is required.");
            flag1=0;
        }
        else if(pass1.length()<6 || pass1.length()>8){
            if(pass1.length()<6){
                t2.setError("Password should be of minimum 6 characters long.");
            }
            else{
                t2.setError("Password should be of maximum 8 characters long.");
            }

            flag1=0;
        }
        else{
            flag1=1;
            t2.setError(null);
        }
        if(TextUtils.isEmpty(pass2)){
            t3.setError("This field is required.");
            flag2=0;
        }
        else if(pass2.length()<6 || pass2.length()>8){
            if(pass2.length()<6) {
                t3.setError("Password should be minimum 6 characters long.");
            }
            else{
                t3.setError("Password should be maximum 8 characters long.");
            }
            flag2=0;
        }
        else{
            flag2=1;
            t3.setError(null);
        }
        if(flag1==1&&flag2==1){
            if(pass1.equals(pass2)){
                password=pass1;
                t2.setError(null);
                t3.setError(null);
                return true;
            }
            else{
                t2.setError("Passwords do not match.");
                t3.setError("Passwords do not match.");
                return false;
            }
        }
        else{
            return false;
        }
    }
    public void submit_password(View view){
        pb5.setVisibility(View.VISIBLE);
        if(!validate_password()){
            Toast.makeText(NewPassword.this,"Error occured due to invalid fields!",Toast.LENGTH_SHORT).show();
            pb5.setVisibility(View.INVISIBLE);
        }
        else{
            db.collection("User Login Details").document(email).update("Password",password).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(NewPassword.this,"Password changed successfully!",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(NewPassword.this,LoginActivity.class));
                        finish();
                        pb5.setVisibility(View.INVISIBLE);
                    }
                    else{
                        Toast.makeText(NewPassword.this,"Error due to poor internet connection!",Toast.LENGTH_SHORT).show();
                        pb5.setVisibility(View.INVISIBLE);
                    }
                }
            });
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
                Toast.makeText(NewPassword.this, "No internet connection.", Toast.LENGTH_SHORT).show();
                flag_internet=1;
            }
            lin.setVisibility(View.GONE);
            gif.setVisibility(View.VISIBLE);
            gtv.setVisibility(View.VISIBLE);
            getSupportActionBar().hide();
        }
        else{
            if(flag_internet==1) {
                Toast.makeText(NewPassword.this, "Connected to internet.", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        if(!isConnected()){
            if(flag_back==1){
                moveTaskToBack(true);
            }
            else{
                Toast.makeText(NewPassword.this,"Press again to exit.",Toast.LENGTH_SHORT).show();
                flag_back=1;
                Handler hndler = new Handler();
                hndler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        flag_back=0;
                    }
                },2700);
            }
        }
        else{
            super.onBackPressed();
        }
    }
}
