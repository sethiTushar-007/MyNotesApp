package com.example.mynotesapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mynotesapp.Notes;

public class NotesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lastAccount.db";
    private static final int DATABASE_VERSION = 1;

    public NotesDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_PETS_TABLE =  "CREATE TABLE " + NotesContract.NotesEntry.TABLE_NAME + " ("
                + NotesContract.NotesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NotesContract.NotesEntry.COLUMN_EMAIL + " TEXT NOT NULL, "
                + NotesContract.NotesEntry.COLUMN_PASSWORD + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_PETS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
