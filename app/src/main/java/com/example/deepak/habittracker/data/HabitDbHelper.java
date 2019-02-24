package com.example.deepak.habittracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HabitDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = HabitDbHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "habits.db";

    private static final int DATABASE_VERSION = 1;

    public HabitDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

         //This string has the sql statement to create a database table
         String SQL_CREATE_HABITS_TABLE = "CREATE TABLE " + HabitContract.HabitEntry.TABLE_NAME + " ("
                + HabitContract.HabitEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + HabitContract.HabitEntry.COLUMN_HABIT + " TEXT NOT NULL,"
                + HabitContract.HabitEntry.COLUMN_FREQUENCY + " INTEGER NOT NULL DEFAULT 0);";

         //this statement executes the string containing the sql create statement
         db.execSQL(SQL_CREATE_HABITS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //WE DO NOTHING UNTIL WE UPDATE THE DATABASE
    }
}
