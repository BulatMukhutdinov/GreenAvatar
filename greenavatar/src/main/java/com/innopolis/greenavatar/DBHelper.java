package com.innopolis.greenavatar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Nikolay | 01.02.2016.
 * Class DBHelper provides SQLite interface
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "consumDB";

    public static final String TABLE1 = "consumption";
    public static final String TABLE2 = "user";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATETIME = "dateTime";
    public static final String COLUMN_CONSPERC = "consPerc";

    public static final String COLUMN_IP = "ip";
    public static final String COLUMN_PASS = "password";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE consumption (_id INTEGER PRIMARY KEY, dateTime TEXT, consPerc TEXT)");
        db.execSQL("CREATE TABLE user (_id INTEGER PRIMARY KEY, ip TEXT, password TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE1);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE2);
        onCreate(db);
    }

    public static void deleteIP(SQLiteDatabase db){
        db.execSQL("delete * from "+ TABLE2);
    }
}
