package com.example.mynotesapp.data;

import android.provider.BaseColumns;

public final class NotesContract {
    private NotesContract(){}

    public static final class NotesEntry implements BaseColumns{

        public final static String TABLE_NAME = "last";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_EMAIL = "email";
        public final static String COLUMN_PASSWORD = "password";
    }
}
