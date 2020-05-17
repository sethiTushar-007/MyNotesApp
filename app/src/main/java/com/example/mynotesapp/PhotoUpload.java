package com.example.mynotesapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import pl.droidsonroids.gif.GifImageView;

public class PhotoUpload extends AppCompatActivity {

    private ImageView img;

    private final int PICK_IMAGE_REQUEST = 71;
    private final int REQUEST_CAMERA = 1;
    private TextView gtv;
    private Uri filePath=null;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private TextView t1;
    private FirebaseFirestore db;
    private int flag_internet;
    private String url,email,username;
    private ProgressBar pb;
    private GifImageView gif;
    private LinearLayout lin;
    private int flag=1,flag_back;
    private CardView c1;
    private String userChoosenTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_upload);
        t1 = (TextView)findViewById(R.id.register);
        c1 = (CardView)findViewById(R.id.remove_card);

        lin = (LinearLayout)findViewById(R.id.photo_upload_everything);
        gif = (GifImageView)findViewById(R.id.photo_upload_gif);
        gtv = (TextView)findViewById(R.id.photo_upload_tv);

        db = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        img = (ImageView)findViewById(R.id.imageView);
        pb = (ProgressBar)findViewById(R.id.pb_photo);

        Intent intent = getIntent();
        if(intent.hasExtra("email")){
            email = intent.getStringExtra("email");
        }
        if(intent.hasExtra("flag")){
            flag = intent.getIntExtra("flag",0);
        }
        if(intent.hasExtra("Username")){
            username = intent.getStringExtra("Username");
        }

        if(flag==0){
            t1.setText("Register");
            c1.setVisibility(View.GONE);
        }
        else {
            if(img.getId()==0){
                c1.setVisibility(View.GONE);
            }
            else{
                c1.setVisibility(View.VISIBLE);
            }
            t1.setText("Update");
            show();
        }
        internet_connection();

    }
    public void galleryIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select a picture"),PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && data!=null && data.getData()!=null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                filePath = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    img.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(requestCode == REQUEST_CAMERA){
                filePath = data.getData();
                try {
                    Bundle extras = data.getExtras();
                    Bitmap mImageUri1 = (Bitmap) extras.get("data");
                    img.setImageBitmap(mImageUri1);
                }catch (Exception e){
                    e.printStackTrace();
                }


            }
        }
        c1.setVisibility(View.VISIBLE);
    }
    public void upload(View view){
        if(filePath!=null){
            pb.setVisibility(View.VISIBLE);

            final StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());
            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = (String)uri.toString();
                            db.collection("User Login Details").document(email).update("Url",url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        show();
                                        Toast.makeText(PhotoUpload.this,"Uploaded!",Toast.LENGTH_SHORT).show();
                                        pb.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    });
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pb.setVisibility(View.INVISIBLE);
                    img.setImageResource(R.mipmap.usericon);
                    Toast.makeText(PhotoUpload.this,"Failed!",Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            img.setImageResource(R.mipmap.usericon);
            Toast.makeText(PhotoUpload.this,"Photo not chosen!",Toast.LENGTH_SHORT).show();
        }

    }
    public void register(View view){
        if(flag==0){
            Toast.makeText(PhotoUpload.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PhotoUpload.this,LoginActivity.class));
        }
        else{
            Intent intent1 = new Intent(PhotoUpload.this,MainActivity.class);
            intent1.putExtra("email",email);
            startActivity(intent1);
        }
    }
    public void remove(View view){
        pb.setVisibility(View.VISIBLE);
        db.collection("User Login Details").document(email).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    String new_url = documentSnapshot.getString("Url");
                    if (new_url == null) {
                        Toast.makeText(PhotoUpload.this, "Photo removed!", Toast.LENGTH_SHORT).show();
                        pb.setVisibility(View.INVISIBLE);
                    } else {
                        StorageReference sr = storage.getReferenceFromUrl(new_url);
                        sr.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                db.collection("User Login Details").document(email).update("Url", null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            img.setImageResource(R.mipmap.usericon);
                                            Toast.makeText(PhotoUpload.this, "Photo removed!", Toast.LENGTH_SHORT).show();
                                            pb.setVisibility(View.INVISIBLE);
                                        } else {
                                            Toast.makeText(PhotoUpload.this, "Error in removing photo due to poor connection!", Toast.LENGTH_SHORT).show();
                                            pb.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(PhotoUpload.this, "Error in removing photo due to poor connection!", Toast.LENGTH_SHORT).show();
                                pb.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PhotoUpload.this,"Error in removing photo due to poor connection!",Toast.LENGTH_SHORT).show();
                pb.setVisibility(View.INVISIBLE);
            }
        });
    }


    public void show(){
        pb.setVisibility(View.VISIBLE);
        db.collection("User Login Details").document(email).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {

                    url = documentSnapshot.getString("Url");
                    if (url == null) {
                        img.setImageResource(R.mipmap.usericon);
                        pb.setVisibility(View.INVISIBLE);
                    } else {
                        Glide.with(getApplicationContext()).load(url).into(img);
                        pb.setVisibility(View.INVISIBLE);
                    }
                }
                else{
                    Toast.makeText(PhotoUpload.this,"Photo can't load due to poor internet connection!",Toast.LENGTH_SHORT).show();
                    img.setImageResource(R.mipmap.usericon);
                    pb.setVisibility(View.INVISIBLE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PhotoUpload.this,"Photo can't load due to poor internet connection!",Toast.LENGTH_SHORT).show();
                img.setImageResource(R.mipmap.usericon);
                pb.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void dialog(View view){
        final String[] items = {"Take Photo","Choose from gallery","Cancel"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(PhotoUpload.this);
        builder.setTitle("Add a Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            boolean result = Utility.checkPermission(PhotoUpload.this);
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(items[which].equals("Take Photo")){
                    userChoosenTask = "Take Photo";
                    if(result){
                        cameraIntent();
                    }
                }
                else if(items[which].equals("Choose from gallery")){
                    userChoosenTask = "Choose from gallery";
                    if(result){
                        galleryIntent();
                    }
                }
                else{
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
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
                Toast.makeText(PhotoUpload.this,"Press again to exit.",Toast.LENGTH_SHORT).show();
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
