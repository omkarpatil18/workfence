package com.workfence.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class DataProvider extends ContentProvider {

    public static final String LOG_TAG = DataProvider.class.getSimpleName();

    private static final int DATA = 100;
    private static final int ATTENDANCE = 101;
    private static final int VOLLEY = 102;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static String groupBy = null;
    public static String having = null;

    static {
        sUriMatcher.addURI(DataContract.CONTENT_AUTHORITY, DataContract.PATH_DATA, DATA);
        sUriMatcher.addURI(DataContract.CONTENT_AUTHORITY, DataContract.PATH_ATTENDANCE, ATTENDANCE);
        sUriMatcher.addURI(DataContract.CONTENT_AUTHORITY, DataContract.PATH_VOLLEY, VOLLEY);
    }

    private DataDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DataDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case DATA:
                cursor = database.query(DataContract.InteractionEntry.TABLE_NAME, projection, selection, selectionArgs,
                        groupBy, having, sortOrder);
                break;
            case VOLLEY:
                cursor = database.query(DataContract.VolleyCache.TABLE_NAME, projection, selection, selectionArgs,
                        groupBy, having, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DATA:
                return insertData(uri, contentValues);
            case ATTENDANCE:
                return insertAttendane(uri, contentValues);
            case VOLLEY:
                return insertVolley(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertVolley(Uri uri, ContentValues contentValues) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(DataContract.VolleyCache.TABLE_NAME, null, contentValues);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertAttendane(Uri uri, ContentValues contentValues) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(DataContract.AttendanceEntry.TABLE_NAME, null, contentValues);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertData(Uri uri, ContentValues values) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(DataContract.InteractionEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DATA:
                return updateData(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateData(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        return database.update(DataContract.InteractionEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DATA:
                return database.delete(DataContract.InteractionEntry.TABLE_NAME, selection, selectionArgs);
            case VOLLEY:
                return database.delete(DataContract.VolleyCache.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DATA:
                return DataContract.InteractionEntry.CONTENT_LIST_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
