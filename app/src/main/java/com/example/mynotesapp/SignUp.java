package com.example.mynotesapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import pl.droidsonroids.gif.GifImageView;

public class SignUp extends AppCompatActivity {

    private TextInputLayout mUsername,mEmail,mnewpassword,mconfirmpassword;
    private String user_name,email,password;
    private ProgressBar pb2;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView t1,t2;
    private TextView gtv;
    private int flag1,flag2,flag3,flag4,flag;
    private GifImageView gif;
    private LinearLayout lin;
    private int flag_internet,flag_back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        flag1=0;
        flag2=0;
        flag3=0;
        flag4=0;

        mUsername = (TextInputLayout)findViewById(R.id.register_username);
        mEmail = (TextInputLayout)findViewById(R.id.register_mail);
        mnewpassword = (TextInputLayout)findViewById(R.id.register_password1);
        mconfirmpassword = (TextInputLayout)findViewById(R.id.register_password2);

        lin = (LinearLayout)findViewById(R.id.sign_up_everything);
        gif = (GifImageView)findViewById(R.id.sign_up_gif);
        gtv= (TextView)findViewById(R.id.sign_up_tv);

        pb2 = (ProgressBar)findViewById(R.id.pb2);
        t1 = (TextView)findViewById(R.id.registerupdate);
        t2 = (TextView)findViewById(R.id.small);


        Intent intent2 = getIntent();
        if(intent2.hasExtra("username")){
            user_name = intent2.getStringExtra("username");
        }
        if(intent2.hasExtra("email")){
            email = intent2.getStringExtra("email");
            flag3 = 1;
        }
        if(intent2.hasExtra("password")){
            password = intent2.getStringExtra("password");
        }

        if(flag3==1){
            mEmail.getEditText().setText(email);
            mEmail.getEditText().setEnabled(false);
            mUsername.getEditText().setText(user_name);
            mnewpassword.getEditText().setText(password);
            mconfirmpassword.getEditText().setText(password);

            t1.setText("Update");
            t2.setVisibility(View.INVISIBLE);
        }
        else {
            t1.setText("Verify Phone Number");
            mEmail.getEditText().setEnabled(true);
            t2.setVisibility(View.VISIBLE);

        }

        internet_connection();
    }

    public boolean validate_username(){
        user_name = mUsername.getEditText().getText().toString().trim();
        if(TextUtils.isEmpty(user_name)){
            mUsername.setError("This field is required.");
            return false;
        }
        else if(user_name.length()>15){
            mUsername.setError("Username is too long.");
            return false;
        }
        else{
            mUsername.setError(null);
            return true;
        }
    }

    public boolean validate_email(){
        email = mEmail.getEditText().getText().toString().trim();
        if(TextUtils.isEmpty(email)){
            mEmail.setError("This field is required.");
            return false;
        }
        else if(email.length()>30){
            mEmail.setError("E-mail Id is too long.");
            return false;
        }
        else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            mEmail.setError("Invalid Email id.");
            return false;
        }
        else{
            mEmail.setError(null);
            return true;
        }
    }

    public boolean validate_password(){
        String pass1 = mnewpassword.getEditText().getText().toString().trim();
        String pass2 = mconfirmpassword.getEditText().getText().toString().trim();
        if(TextUtils.isEmpty(pass1)){
            mnewpassword.setError("This field is required.");
            flag1=0;
        }
        else if(pass1.length()<6 || pass1.length()>8){
            if(pass1.length()<6){
                mnewpassword.setError("Password should be of minimum 6 characters long.");
            }
            else{
                mnewpassword.setError("Password should be of maximum 8 characters long.");
            }

            flag1=0;
        }
        else{
            flag1=1;
            mnewpassword.setError(null);
        }
        if(TextUtils.isEmpty(pass2)){
            mconfirmpassword.setError("This field is required.");
            flag2=0;
        }
        else if(pass2.length()<6 || pass2.length()>8){
            if(pass2.length()<6) {
                mconfirmpassword.setError("Password should be minimum 6 characters long.");
            }
            else{
                mconfirmpassword.setError("Password should be maximum 8 characters long.");
            }
            flag2=0;
        }
        else{
            flag2=1;
            mconfirmpassword.setError(null);
        }
        if(flag1==1&&flag2==1){
            if(pass1.equals(pass2)){
                password=pass1;
                mnewpassword.setError(null);
                mconfirmpassword.setError(null);
                return true;
            }
            else{
                mnewpassword.setError("Passwords do not match.");
                mconfirmpassword.setError("Passwords do not match.");
                return false;
            }
        }
        else{
            return false;
        }
    }


    public void registration(View view){
        pb2.setVisibility(View.VISIBLE);
        if(flag3==0) {
            if (!validate_username() | !validate_email() | !validate_password()) {
                Toast.makeText(SignUp.this, "Error in registration", Toast.LENGTH_SHORT).show();
                pb2.setVisibility(View.INVISIBLE);
            }
            else {
                flag=0;

                db.collection("User Login Details").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult()!=null){
                                for (QueryDocumentSnapshot docs : task.getResult()){
                                    String m = docs.getString("Email");
                                    if(m.equals(email)){
                                        mEmail.setError("Email id already exists.");
                                        Toast.makeText(SignUp.this,"Error in registration!",Toast.LENGTH_SHORT).show();
                                        pb2.setVisibility(View.INVISIBLE);
                                        flag = 1;
                                        break;
                                    }
                                }
                                if(flag==0){
                                    Intent intent = new Intent(SignUp.this,OTP.class);
                                    intent.putExtra("email",email);
                                    intent.putExtra("username",user_name);
                                    intent.putExtra("password",password);
                                    intent.putExtra("otp_check",1);
                                    startActivity(intent);
                                    pb2.setVisibility(View.INVISIBLE);
                                }
                            }
                            else{
                                Intent intent = new Intent(SignUp.this,OTP.class);
                                intent.putExtra("email",email);
                                intent.putExtra("username",user_name);
                                intent.putExtra("password",password);
                                intent.putExtra("otp_check",1);
                                startActivity(intent);
                                pb2.setVisibility(View.INVISIBLE);
                            }
                        }
                        else{
                            Toast.makeText(SignUp.this,"Error in registration!",Toast.LENGTH_SHORT).show();
                            pb2.setVisibility(View.INVISIBLE);
                        }
                    }
                });

            }
        }

        else{
            if (!validate_username() | !validate_email() | !validate_password()) {
                Toast.makeText(SignUp.this, "Error in Update", Toast.LENGTH_SHORT).show();
                pb2.setVisibility(View.INVISIBLE);
            }
            else{
                DocumentReference docref = db.collection("User Login Details").document(email);
                docref.update("User name",user_name,
                        "Password",password).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SignUp.this,"Successfully updated!",Toast.LENGTH_SHORT).show();

                        Intent intent2 = new Intent(SignUp.this,MainActivity.class);
                        intent2.putExtra("email",email);
                        startActivity(intent2);
                        pb2.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignUp.this,"Error in update! Please try again being online.",Toast.LENGTH_SHORT).show();
                        pb2.setVisibility(View.INVISIBLE);
                    }
                });

            }

        }
    }
    public void toLogin(View v){
        startActivity(new Intent(SignUp.this,LoginActivity.class));
        finish();
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
            lin.setVisibility(View.GONE);
            gif.setVisibility(View.VISIBLE);
            gtv.setVisibility(View.VISIBLE);
            getSupportActionBar().hide();
        }
        else{
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
                Toast.makeText(SignUp.this,"Press again to exit.",Toast.LENGTH_SHORT).show();
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
