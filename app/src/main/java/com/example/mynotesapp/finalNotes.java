package com.example.mynotesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Distribution;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.droidsonroids.gif.GifImageView;

public class finalNotes extends AppCompatActivity {

    private FirebaseFirestore db;
    private String email;
    private String oldTitle,title,content,date,oldContent;
    private GifImageView gif;
    private LinearLayout lin;
    private EditText ed_title,ed_content;
    private ProgressBar pb1;
    private TextView gtv;
    private int flag_internet;
    private int flag_back;
    int flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_notes);

        lin = (LinearLayout)findViewById(R.id.final_notes_everything);
        gif = (GifImageView) findViewById(R.id.final_notes_gif);

        ed_title = (EditText)findViewById(R.id.title);
        ed_content = (EditText)findViewById(R.id.content);

        gtv = (TextView)findViewById(R.id.final_notes_tv);

        pb1 = (ProgressBar)findViewById(R.id.pb_final_1);
        pb1.setVisibility(View.INVISIBLE);

        db = FirebaseFirestore.getInstance();
        flag=0;

        Intent intent = getIntent();
        if(intent.hasExtra("Email")){
            email = intent.getStringExtra("Email");
        }
        if(intent.hasExtra("Title")){
            oldTitle = intent.getStringExtra("Title");
        }
        if(intent.hasExtra("Content")){
            oldContent = intent.getStringExtra("Content");
            flag=1;
        }

        if(flag==1){
            ed_title.setText(oldTitle);
            ed_content.setText(oldContent);
        }

        internet_connection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.final_notes,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_delete:

                final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Delete The Note");
                alertDialog.setMessage("Do you really want to delete this note ?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pb1.setVisibility(View.VISIBLE);
                        if(flag==0){
                            Toast.makeText(finalNotes.this,"Notes deleted !",Toast.LENGTH_SHORT).show();
                            Intent intent1 = new Intent(finalNotes.this,MainActivity.class);
                            intent1.putExtra("email",email);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent1);
                            pb1.setVisibility(View.GONE);

                        }
                        else{
                            DocumentReference docref = db.collection(email).document(oldTitle);
                            docref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(finalNotes.this,"Successfully deleted!",Toast.LENGTH_SHORT).show();
                                        Intent intent1 = new Intent(finalNotes.this,MainActivity.class);
                                        intent1.putExtra("email",email);
                                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent1);
                                        pb1.setVisibility(View.GONE);
                                    }
                                    else {
                                        Toast.makeText(finalNotes.this,"Error occured while deleting. Please try again!",Toast.LENGTH_SHORT).show();
                                        pb1.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();

                break;
            case R.id.action_clear:
                ed_title.setText("");
                ed_content.setText("");
                break;
            case R.id.action_save:
                pb1.setVisibility(View.VISIBLE);
                title = ed_title.getText().toString();
                content = ed_content.getText().toString();

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy ' at 'HH:mm");
                date = sdf.format(new Date());

                if(!TextUtils.isEmpty(title)){
                    if(flag==0){
                        final Map<String,String> savenotes = new HashMap<>();
                        savenotes.put("Title",title);
                        savenotes.put("Content",content);
                        savenotes.put("Date",date);

                        db.collection(email).document(title).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){
                                    Toast.makeText(finalNotes.this,"Title already exists. Change your title.",Toast.LENGTH_SHORT).show();
                                    pb1.setVisibility(View.INVISIBLE);
                                }
                                else{
                                    db.collection(email).document(title).set(savenotes).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(finalNotes.this,"Successfully saved!",Toast.LENGTH_SHORT).show();
                                            Intent intent2 = new Intent(finalNotes.this,MainActivity.class);
                                            intent2.putExtra("email",email);
                                            intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent2);
                                            pb1.setVisibility(View.GONE);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(finalNotes.this,"You are Offline or in poor internet connection!",Toast.LENGTH_SHORT).show();
                                            pb1.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(finalNotes.this,"You are Offline or in poor internet connection!",Toast.LENGTH_SHORT).show();
                                pb1.setVisibility(View.INVISIBLE);
                            }
                        });


                    }
                    else{
                        if(oldTitle.equals(title)){
                            DocumentReference docref1 = db.collection(email).document(title);
                            docref1.update("Content",content,"Date",date).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(finalNotes.this,"Successfully saved!",Toast.LENGTH_SHORT).show();
                                    Intent intent4 = new Intent(finalNotes.this,MainActivity.class);
                                    intent4.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent4.putExtra("email",email);
                                    startActivity(intent4);
                                    pb1.setVisibility(View.GONE);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(finalNotes.this,"Error in saving due to poor connection. Please try again.",Toast.LENGTH_SHORT).show();
                                    pb1.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                        else{
                            DocumentReference docref2 = db.collection(email).document(oldTitle);
                            docref2.delete();
                            final Map<String,String> values = new HashMap<>();
                            values.put("Title",title);
                            values.put("Content",content);
                            values.put("Date",date);

                            db.collection(email).document(title).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()){
                                        Toast.makeText(finalNotes.this,"Title already exists. Please change your title.",Toast.LENGTH_SHORT).show();
                                        pb1.setVisibility(View.INVISIBLE);
                                    }
                                    else{
                                        DocumentReference docref3 = db.collection(email).document(title);
                                        docref3.set(values).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(finalNotes.this,"Successfully saved!",Toast.LENGTH_SHORT).show();
                                                Intent intent3 = new Intent(finalNotes.this,MainActivity.class);
                                                intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                intent3.putExtra("email",email);
                                                startActivity(intent3);
                                                pb1.setVisibility(View.GONE);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(finalNotes.this,"Error in saving due to poor internet connection. Please try again.",Toast.LENGTH_SHORT).show();
                                                pb1.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(finalNotes.this,"You are Offline or in poor internet connection!",Toast.LENGTH_SHORT).show();
                                    pb1.setVisibility(View.INVISIBLE);
                                }
                            });

                        }
                    }
                }
                else{
                    Toast.makeText(finalNotes.this,"Atleast give a title to your notes!",Toast.LENGTH_SHORT).show();
                    pb1.setVisibility(View.INVISIBLE);
                }
                break;

            case R.id.action_share :
                title = ed_title.getText().toString();
                content = ed_content.getText().toString();
                final String[] items = {"Email","SMS","Cancel"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(finalNotes.this);
                builder.setTitle("Share Notes");
                builder.setIcon(R.drawable.share);

                builder.setPositiveButton("SMS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setData(Uri.parse("smsto:"));
                        intent.putExtra("sms_body","Title : "+title+"(Sent through My NoteApp)"+"\n\n"+content );
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                });
                builder.setNegativeButton("Email", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"));
                        intent.putExtra(Intent.EXTRA_TEXT,"Title : "+title+"\n\n"+content);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Sent through My NoteApp");
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                });


                builder.show();
                break;

        }


        return super.onOptionsItemSelected(item);
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
                Toast.makeText(finalNotes.this,"Press again to exit.",Toast.LENGTH_SHORT).show();
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