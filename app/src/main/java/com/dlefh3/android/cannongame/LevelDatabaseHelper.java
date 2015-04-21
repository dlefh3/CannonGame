package com.dlefh3.android.cannongame;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper Class to maintain the SQLite Database
 * that will hold level and score information
 */
public class LevelDatabaseHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "level.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_LEVEL = "level";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LEVEL_NUMBER = "level_number";
    public static final String COLUMN_SCORE = "score";
    public static final String COLUMN_STATUS = "status";

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_LEVEL + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_LEVEL_NUMBER
            + " integer not null, " + COLUMN_SCORE + " integer not null, "
            + COLUMN_STATUS + " integer not null);";

    public LevelDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEVEL);
        onCreate(db);
    }
}
