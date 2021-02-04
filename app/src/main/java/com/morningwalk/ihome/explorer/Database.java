// Database.java
// Version 4.0
// November 25, 2018.

package com.morningwalk.ihome.explorer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import android.database.sqlite.SQLiteOpenHelper;

import static android.widget.Toast.LENGTH_LONG;

public class Database extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "myLocations";
    public static final String TABLE_NAME = "locations";
    public static final String COLUMN_IDX = "idx";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LNG = "lng";
    public static final String COLUMN_ALT = "alt";
    public static final String COLUMN_VEL = "vel";
    Context m_context;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        m_context = context;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_THE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + "(" +
                COLUMN_IDX + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_LAT + " FLOAT," +
                COLUMN_LNG + " FLOAT," +
                COLUMN_ALT + " FLOAT," +
                COLUMN_VEL + " FLOAT)";
        db.execSQL (CREATE_THE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertPoint(int index, PointInfo<Double> pi) {
        long res = 0;
        SQLiteDatabase db = getWritableDatabase ();
        try {
            ContentValues values = new ContentValues ();
            values.put (Database.COLUMN_IDX, index);
            values.put (Database.COLUMN_LAT, pi.getLat ());
            values.put (Database.COLUMN_LNG, pi.getLng ());
            values.put (Database.COLUMN_ALT, pi.getAlt ());
            values.put (Database.COLUMN_VEL, pi.getVel ());

            res = db.insertOrThrow (Database.TABLE_NAME, null, values);
        }
        catch (Exception ex) {
            Toast.makeText (m_context, ex.getMessage (),  LENGTH_LONG).show ();
        }
        finally {
            db.close ();
        }

        if(res == -1) return false;
        return true;
    }

    public int getPointsCount() {
        String countQuery = "SELECT * FROM " + Database.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public List<PointInfo> getAllPoints() {
        List<PointInfo> list = new ArrayList<PointInfo> ();
        String selectQuery = "SELECT * FROM " + Database.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase ();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new PointInfo<Double> (
                        cursor.getDouble (cursor.getColumnIndex (Database.COLUMN_LAT)),
                        cursor.getDouble (cursor.getColumnIndex (Database.COLUMN_LNG)),
                        cursor.getDouble (cursor.getColumnIndex (Database.COLUMN_ALT)),
                        cursor.getDouble (cursor.getColumnIndex (Database.COLUMN_VEL))));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.execSQL("DELETE FROM " + Database.TABLE_NAME);
        db.close();

        return list;
    }
}
