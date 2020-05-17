package com.example.mynotesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import pl.droidsonroids.gif.GifImageView;

public class OTP extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private TextInputLayout t1;
    private EditText e1,e2,e3,e4,e5,e6;
    private String es1,es2,es3,es4,es5,es6;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView heading,onCard;
    private String phone,verification,code_entered,email,username,password;
    private ProgressBar pb4;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private TextView gtv;
    private int flag1,otp_check=0,flag;
    private int flag_internet,flag_back;
    private LinearLayout lin;
    private GifImageView gif;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        pb4 = (ProgressBar)findViewById(R.id.pb4);
        t1 = (TextInputLayout)findViewById(R.id.phonenumber);

        e1 = (EditText)findViewById(R.id.otp1);
        e2 = (EditText)findViewById(R.id.otp2);
        e3 = (EditText)findViewById(R.id.otp3);
        e4 = (EditText)findViewById(R.id.otp4);
        e5 = (EditText)findViewById(R.id.otp5);
        e6 = (EditText)findViewById(R.id.otp6);


        heading = (TextView)findViewById(R.id.phone_number_heading);
        onCard = (TextView)findViewById(R.id.onCard);

        lin = (LinearLayout)findViewById(R.id.otp_everything);
        gif = (GifImageView)findViewById(R.id.otp_gif);
        gtv = (TextView)findViewById(R.id.otp_tv);

        t1.setEnabled(true);
        e1.setEnabled(false);
        e2.setEnabled(false);
        e3.setEnabled(false);
        e4.setEnabled(false);
        e5.setEnabled(false);
        e6.setEnabled(false);

        mFirebaseAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        if(intent.hasExtra("email")){
            email = intent.getStringExtra("email");
        }
        if(intent.hasExtra("username")){
            username = intent.getStringExtra("username");
        }
        if(intent.hasExtra("password")){
            password = intent.getStringExtra("password");
        }
        if(intent.hasExtra("otp_check")){
            otp_check = intent.getIntExtra("otp_check",0);
        }

        if(otp_check==2){
            db.collection("User Login Details").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        phone = document.getString("Phone");
                        t1.getEditText().setText(phone);
                        heading.setText("Enter your updated Phone Number (with country code) for OTP verification :");
                    }
                    else{
                        Toast.makeText(OTP.this,"Error occurred! Please try again.",Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        else if(otp_check==0){
            heading.setText("Enter your registered Phone Number (with country code) for OTP verification :");
        }
        else {
            heading.setText("Enter your Phone Number (with country code) for OTP verification and complete the registration process :");
        }

        internet_connection();
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            resendToken = forceResendingToken;
            verification = s;

            pb4.setVisibility(View.INVISIBLE);

            t1.setEnabled(false);
            e1.setEnabled(true);
            e2.setEnabled(true);
            e3.setEnabled(true);
            e4.setEnabled(true);
            e5.setEnabled(true);
            e6.setEnabled(true);

            changeEditText();


            onCard.setText("Resend OTP");
            heading.setText("Enter the 6-digit OTP sent to the registered phone number:");
            pb4.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            pb4.setVisibility(View.INVISIBLE);
            Log.v("OTP.this",e.toString());
            Toast.makeText(OTP.this,"Error occurred due to poor connection!",Toast.LENGTH_SHORT).show();
        }
    };

    private boolean validate_phone(){
        phone = t1.getEditText().getText().toString();
        if(TextUtils.isEmpty(phone)){
            t1.setError("This field is required.");
            return false;
        }
        else if(phone.length()!=13){
            t1.setError("Enter a valid phone number");
            return false;
        }
        else{
            t1.setError("");
            return true;
        }
    }

    private void check_phone() {
        pb4.setVisibility(View.VISIBLE);
        if (otp_check == 1 || otp_check==2) {
            flag=0;
            db.collection("User Login Details").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        if(task.getResult()!=null) {
                            for (QueryDocumentSnapshot docs : task.getResult()) {
                                String ph = docs.getString("Phone");
                                if (ph.equals(phone)) {
                                    t1.setError("Phone number is already registered.");
                                    Toast.makeText(OTP.this, "Error in phone number!", Toast.LENGTH_SHORT).show();
                                    pb4.setVisibility(View.INVISIBLE);
                                    flag = 1;
                                    break;
                                }
                            }
                            if (flag == 0) {
                                sendVerificationCode(phone);
                            }
                        }
                        else{
                            Toast.makeText(OTP.this,"No account exists.",Toast.LENGTH_SHORT).show();
                            pb4.setVisibility(View.INVISIBLE);
                        }
                    }
                    else{
                        Toast.makeText(OTP.this,"Error due to network problem!",Toast.LENGTH_SHORT).show();
                        pb4.setVisibility(View.INVISIBLE);
                    }
                }
            });

        } else {
            flag1 = 0;
            db.collection("User Login Details").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String ph = document.getString("Phone");
                                if (ph.equals(phone)) {
                                    email = document.getString("Email");
                                    flag1 = 1;
                                    sendVerificationCode(phone);
                                    break;
                                }
                            }
                            if (flag1 == 0) {
                                Toast.makeText(OTP.this, "Phone number not registered!", Toast.LENGTH_SHORT).show();
                                pb4.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            Toast.makeText(OTP.this, "No account exists!", Toast.LENGTH_SHORT).show();
                            pb4.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        Toast.makeText(OTP.this, "Error occurred, Please try again!", Toast.LENGTH_SHORT).show();
                        pb4.setVisibility(View.INVISIBLE);
                    }
                }

            });
        }
    }

    public void generate_otp(View view){
        if(!validate_phone()){
            Toast.makeText(OTP.this,"Invalid fields!",Toast.LENGTH_SHORT).show();
        }
        else{
            check_phone();
        }
    }

    private void sendVerificationCode(String number) {
        pb4.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack,
                resendToken
        );

    }
    public void submit_otp(){
        code_entered = es1+es2+es3+es4+es5+es6;
        if(TextUtils.isEmpty(code_entered)){
            Toast.makeText(OTP.this,"First enter the OTP", Toast.LENGTH_LONG).show();
        }
        else{
            verifyCode(code_entered);
        }
    }

    private void verifyCode(String code) {
        pb4.setVisibility(View.VISIBLE);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verification, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if(otp_check==0){
                                Intent intent = new Intent(OTP.this,NewPassword.class);
                                intent.putExtra("email",email);
                                startActivity(intent);
                                finish();
                            }
                            else if(otp_check==1){
                                addData();
                                Intent intent = new Intent(OTP.this,PhotoUpload.class);
                                intent.putExtra("email",email);
                                intent.putExtra("flag",0);
                                startActivity(intent);
                                finish();
                            }
                            else{
                                addPhone();
                                Intent intent = new Intent(OTP.this,MainActivity.class);
                                intent.putExtra("email",email);
                                startActivity(intent);
                                finish();
                            }

                            pb4.setVisibility(View.INVISIBLE);

                        } else {
                            pb4.setVisibility(View.INVISIBLE);
                            Toast.makeText(OTP.this,"Invalid OTP",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void addPhone(){
        db.collection("User Login Details").document(email).update("Phone",phone).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(OTP.this,"Phone number registered!",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(OTP.this,"Error occurred. Please try again!",Toast.LENGTH_SHORT).show();
                    pb4.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    private void addData(){
        Map<String,String> user = new HashMap<>();
        user.put("User name",username);
        user.put("Email",email);
        user.put("Phone",phone);
        user.put("Url",null);
        user.put("Password",password);

        db.collection("User Login Details").document(email).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(OTP.this,"Registration Successful",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(OTP.this,"Error occurred in registration! Please try again.",Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                Toast.makeText(OTP.this,"Press again to exit.",Toast.LENGTH_SHORT).show();
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
    private void changeEditText(){
        e1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(e1.getText().toString().length()==1){
                    es1 = e1.getText().toString();
                    e2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        e2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(e2.getText().toString().length()==1){
                    es2 = e2.getText().toString();
                    e3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        e3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(e3.getText().toString().length()==1){
                    es3 = e3.getText().toString();
                    e4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        e4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(e4.getText().toString().length()==1){
                    es4 = e4.getText().toString();
                    e5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        e5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(e5.getText().toString().length()==1){
                    es5 = e5.getText().toString();
                    e6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        e6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(e6.getText().toString().length()==1){
                    es6 = e6.getText().toString();
                    submit_otp();

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

}
