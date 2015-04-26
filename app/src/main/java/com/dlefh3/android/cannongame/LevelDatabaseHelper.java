package com.dlefh3.android.cannongame;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

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

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_LEVEL + "("
            + COLUMN_ID    + " integer primary key autoincrement, "
            + COLUMN_SCORE + " real not null "
            + ");";

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
    public void addScore(double score)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO " + TABLE_LEVEL + " (" + COLUMN_SCORE + ") VALUES (" + score + ");");

    }
    public ArrayList<Double> getTopScores()
    {
        //String query = "SELECT * FROM" + TABLE_LEVEL + " ORDER BY " + COLUMN_SCORE + " DESC LIMIT 10;";
        ArrayList<Double> results = new ArrayList<Double>();
        String[] cols = {COLUMN_SCORE};
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_LEVEL, cols,null, null, null, COLUMN_SCORE + " DESC", "10" );
        do
        {
            results.add(cursor.getDouble(0));
        }while(cursor.moveToNext());
        return results;
    }
    public void clearScores()
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEVEL + ";");
        onCreate(db);
    }
}
