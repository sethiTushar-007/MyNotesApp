package com.example.mynotesapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.mynotesapp.data.NotesContract;
import com.example.mynotesapp.data.NotesDbHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private TextView t1,t2,header;
    private String email,phone,username,password,url;
    private int flag_internet;
    private GifImageView gif;
    private LinearLayout lin;
    private TextView gtv;
    FloatingActionButton fab;

    private ProgressBar pb3;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Notes> notesList = new ArrayList<>();
    private RecyclerView recyclerView;
    private NoteAdapter mAdapter;
    private ImageView img;
    private int flag=0;
    private NotesDbHelper mDbHelper;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gif = (GifImageView)findViewById(R.id.main_gif);
        lin = (LinearLayout)findViewById(R.id.main_everything);
        gtv = (TextView)findViewById(R.id.main_tv);

        mDbHelper = new NotesDbHelper(this);
        database = mDbHelper.getWritableDatabase();

        Intent intent = getIntent();
        if(intent.hasExtra("email")){
            email = intent.getStringExtra("email");
        }


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,finalNotes.class);
                intent.putExtra("Email",email);
                startActivity(intent);
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        t1 = (TextView) headerView.findViewById(R.id.textView1);
        t2 = (TextView) headerView.findViewById(R.id.textView2);
        img = (ImageView)headerView.findViewById(R.id.imageView);
        header = (TextView) findViewById(R.id.header);



        DocumentReference docref = db.collection("User Login Details").document(email);
        docref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    username = document.getString("User name");
                    phone = document.getString("Phone");
                    password = document.getString("Password");

                    String heading = username+", here are your notes...";
                    t1.setText(username);
                    t2.setText(email);
                    header.setText(heading);
                }
                else{
                    Toast.makeText(MainActivity.this,"You are Offline or in poor internet connection!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        pb3 = (ProgressBar)findViewById(R.id.pb3);
        settingList();
        recyclerView = (RecyclerView) findViewById(R.id.list);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Notes not = notesList.get(position);
                Intent intent2 = new Intent(MainActivity.this,finalNotes.class);
                intent2.putExtra("Email",email);
                intent2.putExtra("Title",not.getTitle());
                intent2.putExtra("Content",not.getContent());
                startActivity(intent2);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        show1();

        internet_connection();


    }

    public void settingList(){
        pb3.setVisibility(View.VISIBLE);
        final RelativeLayout empty_view = (RelativeLayout) findViewById(R.id.empty_view);

        db.collection(email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    notesList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String title12 = document.getString("Title");
                        String content12 = document.getString("Content");
                        String date12 = document.getString("Date");
                        Notes notes1 = new Notes(title12,date12,content12);
                        notesList.add(notes1);
                    }

                    Collections.sort(notesList,new DateSorter());
                    Collections.reverse(notesList);
                    mAdapter = new NoteAdapter(notesList);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(mAdapter);
                    pb3.setVisibility(View.INVISIBLE);

                    if(notesList.size()==0){
                        empty_view.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(GONE);
                        header.setVisibility(GONE);
                    }
                    else{
                        empty_view.setVisibility(GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        header.setVisibility(View.VISIBLE);
                    }
                    mAdapter.notifyDataSetChanged();

                }
                else {
                    Toast.makeText(MainActivity.this,"You are Offline or in poor internet connection!",Toast.LENGTH_SHORT).show();
                    pb3.setVisibility(View.INVISIBLE);
                }
            }
        });


    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(flag==1){
                moveTaskToBack(true);
            }
            else{
                Toast.makeText(MainActivity.this,"Press again to exit.",Toast.LENGTH_SHORT).show();
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete_all) {

            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Delete All Notes");
            alertDialog.setMessage("Do you really want to delete all your notes ?");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    pb3.setVisibility(View.VISIBLE);
                    db.collection(email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for (QueryDocumentSnapshot docs : task.getResult()){
                                    db.collection(email).document(docs.getId()).delete();
                                }
                                pb3.setVisibility(View.INVISIBLE);
                            }
                            else{
                                Toast.makeText(MainActivity.this,"You are Offline or in poor internet connection!",Toast.LENGTH_SHORT).show();
                                pb3.setVisibility(View.INVISIBLE);
                            }
                            settingList();
                        }
                    });
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
            settingList();
        }
        else if(id==R.id.action_refresh){
            settingList();
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_update) {
            toupdate();

        } else if (id == R.id.nav_delete) {
            delete();

        } else if (id == R.id.nav_logout) {
            logOut();
        } else if( id == R.id.nav_photo){
            Intent intent9 = new Intent(MainActivity.this,PhotoUpload.class);
            intent9.putExtra("email",email);
            intent9.putExtra("flag",1);
            startActivity(intent9);
        }
        else if(id==R.id.nav_phone){
            Intent intent = new Intent(MainActivity.this,OTP.class);
            intent.putExtra("email",email);
            intent.putExtra("otp_check",2);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void toupdate(){
        Intent intent2 = new Intent(MainActivity.this,SignUp.class);
        intent2.putExtra("username",username);
        intent2.putExtra("phone",phone);
        intent2.putExtra("email",email);
        intent2.putExtra("password",password);
        startActivity(intent2);
    }

    public void logOut(){

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Log Out");
        alertDialog.setMessage("Do you really want to log out from your account ?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pb3.setVisibility(View.VISIBLE);

                String where = "email = ?";
                String[] args = {email};

                int n = database.delete(NotesContract.NotesEntry.TABLE_NAME,where,args);
                if(n>0){
                    Toast.makeText(MainActivity.this,"Logged out",Toast.LENGTH_SHORT).show();
                    Intent intent1 = new Intent(MainActivity.this,LoginActivity.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent1);
                    pb3.setVisibility(GONE);
                    finish();
                }
                else{
                    Toast.makeText(MainActivity.this,"Error in log out due to poor connection.",Toast.LENGTH_SHORT).show();
                    pb3.setVisibility(View.INVISIBLE);
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




    }
    public void delete(){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Delete Account");
        alertDialog.setMessage("Do you really want to permanently delete your account ?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pb3.setVisibility(View.VISIBLE);

                String where = "email = ?";
                String[] args = {email};

                int m = database.delete(NotesContract.NotesEntry.TABLE_NAME,where,args);
                if(m>0){
                    DocumentReference document = db.collection("User Login Details").document(email);
                    document.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            db.collection(email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()){
                                        for (QueryDocumentSnapshot docs : task.getResult()){
                                            db.collection(email).document(docs.getId()).delete();
                                        }
                                        Toast.makeText(MainActivity.this,"Successfully deleted!",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(MainActivity.this,LoginActivity.class));
                                        pb3.setVisibility(GONE);
                                        finish();
                                    }
                                    else{
                                        Toast.makeText(MainActivity.this,"You are Offline or in poor internet connection!",Toast.LENGTH_SHORT).show();
                                        pb3.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this,"Delete failed! Please try again.",Toast.LENGTH_SHORT).show();
                            pb3.setVisibility(View.INVISIBLE);
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
    }
    public void show1(){
        pb3.setVisibility(View.VISIBLE);
        db.collection("User Login Details").document(email).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){

                    url = documentSnapshot.getString("Url");
                    if(url==null){
                        img.setImageResource(R.mipmap.usericon);
                    }
                    else {
                        Glide.with(getApplicationContext()).load(url).into(img);
                        pb3.setVisibility(View.INVISIBLE);
                        Log.v("URL test", url);
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"Photo can't load due to poor internet connection!",Toast.LENGTH_SHORT).show();
                    pb3.setVisibility(View.INVISIBLE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Photo can't load due to poor internet connection!",Toast.LENGTH_SHORT).show();
                pb3.setVisibility(View.INVISIBLE);
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
            if(flag_internet==0) {
                Toast.makeText(MainActivity.this, "No internet connection.", Toast.LENGTH_SHORT).show();
                flag_internet=1;
            }
            lin.setVisibility(GONE);
            gif.setVisibility(View.VISIBLE);
            gtv.setVisibility(View.VISIBLE);

        }
        else{
            if(flag_internet==1) {
                Toast.makeText(MainActivity.this, "Connected to internet.", Toast.LENGTH_SHORT).show();
                flag_internet= 0;
            }
            lin.setVisibility(View.VISIBLE);
            gif.setVisibility(GONE);
            gtv.setVisibility(GONE);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                internet_connection();
            }
        },2000);
    }

}
