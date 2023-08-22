package com.workfence.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = DataDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "dataset.db";

    private static final int DATABASE_VERSION = 1;

    public DataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_DATA_TABLE = "CREATE TABLE " + DataContract.InteractionEntry.TABLE_NAME + " ("
                + DataContract.InteractionEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DataContract.InteractionEntry.COLUMN_CLIENTID + " TEXT , "
                + DataContract.InteractionEntry.COLUMN_NAME + " TEXT , "
                + DataContract.InteractionEntry.COLUMN_MODEL + " TEXT , "
                + DataContract.InteractionEntry.COLUMN_ADDRESS + " TEXT NOT NULL, "
                + DataContract.InteractionEntry.COLUMN_AVG_RSSI + " TEXT, "
                + DataContract.InteractionEntry.COLUMN_DISTANCE + " TEXT, "
                + DataContract.InteractionEntry.COLUMN_AVG_DISTANCE + " TEXT, "
                + DataContract.InteractionEntry.COLUMN_MIN_DISTANCE + " TEXT, "
                + DataContract.InteractionEntry.COLUMN_PHONE_STATE + " INTEGER, "
                + DataContract.InteractionEntry.COLUMN_ZONE + " TEXT, "
                + DataContract.InteractionEntry.COLUMN_TSTART + " TEXT, "
                + DataContract.InteractionEntry.COLUMN_TSTOP + " TEXT);";

        String SQL_CREATE_ATTENDANCE_TABLE = "CREATE TABLE " + DataContract.AttendanceEntry.TABLE_NAME + " ("
                + DataContract.AttendanceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DataContract.AttendanceEntry.COLUMN_ID + " TEXT , "
                + DataContract.AttendanceEntry.COLUMN_MODE + " TEXT NOT NULL, "
                + DataContract.AttendanceEntry.COLUMN_TIME + " TEXT );";

        String SQL_CREATE_VOLLEY_TABLE = "CREATE TABLE " + DataContract.VolleyCache.TABLE_NAME + " ("
                + DataContract.VolleyCache._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DataContract.VolleyCache.COLUMN_MODEL + " TEXT, "
                + DataContract.VolleyCache.COLUMN_TX_POWER + " TEXT, "
                + DataContract.VolleyCache.COLUMN_CLIENTID + " TEXT, "
                + DataContract.VolleyCache.COLUMN_URL + " TEXT, "
                + DataContract.VolleyCache.COLUMN_START + " TEXT, "
                + DataContract.VolleyCache.COLUMN_STOP + " TEXT, "
                + DataContract.VolleyCache.COLUMN_MIN + " TEXT, "
                + DataContract.VolleyCache.COLUMN_AVG + " TEXT, "
                + DataContract.VolleyCache.COLUMN_AVG_RSSI + " TEXT, "
                + DataContract.VolleyCache.COLUMN_STATUS + " TEXT, "
                + DataContract.VolleyCache.COLUMN_OUT_DURATION + " TEXT, "
                + DataContract.VolleyCache.COLUMN_SCREEN_TIME + " TEXT, "
                + DataContract.VolleyCache.COLUMN_CAMERA_COUNT + " TEXT );";

        db.execSQL(SQL_CREATE_DATA_TABLE);
        db.execSQL(SQL_CREATE_ATTENDANCE_TABLE);
        db.execSQL(SQL_CREATE_VOLLEY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}