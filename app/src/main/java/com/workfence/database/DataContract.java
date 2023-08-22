package com.workfence.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class DataContract {

    public static final String CONTENT_AUTHORITY = "com.workfence";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_DATA = "data";
    public static final String PATH_ATTENDANCE = "attendance";
    public static final String PATH_VOLLEY = "volley";

    private DataContract() {
    }

    public static final class InteractionEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_DATA);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DATA;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DATA;

        public final static String TABLE_NAME = "data";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_CLIENTID = "clientID";

        public final static String COLUMN_NAME = "name";

        public final static String COLUMN_MODEL = "model";

        public final static String COLUMN_ADDRESS = "addr";

        //public final static String COLUMN_COUNT = "count";

        public final static String COLUMN_AVG_RSSI = "rssi";

        public final static String COLUMN_DISTANCE = "dist";

        public final static String COLUMN_ZONE = "zone";

        public final static String COLUMN_TSTART = "start_time";

        public final static String COLUMN_TSTOP = "stop_time";

        public final static String COLUMN_AVG_DISTANCE = "avg_distance";

        public final static String COLUMN_MIN_DISTANCE = "min_distance";

        public final static String COLUMN_PHONE_STATE = "phone_state";
    }

    public static final class AttendanceEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ATTENDANCE);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ATTENDANCE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ATTENDANCE;

        public final static String TABLE_NAME = "attendance";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_ID = "uniq_id";

        public final static String COLUMN_MODE = "mode"; // IN / OUT

        public final static String COLUMN_TIME = "time";
    }

    public static final class VolleyCache implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VOLLEY);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VOLLEY;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VOLLEY;

        public final static String TABLE_NAME = "volley";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_CLIENTID = "clientID";

        public final static String COLUMN_URL = "url";

        public final static String COLUMN_START = "start_time";

        public final static String COLUMN_STOP = "stop_time";

        public final static String COLUMN_MIN = "min_dist";

        public final static String COLUMN_AVG = "avg_time";

        public final static String COLUMN_AVG_RSSI = "avg_rssi";

        public final static String COLUMN_MODEL = "model";

        public final static String COLUMN_TX_POWER = "tx_power";

        public final static String COLUMN_STATUS = "status";

        public final static String COLUMN_OUT_DURATION = "out_duration";

        public final static String COLUMN_SCREEN_TIME = "screen_time";

        public final static String COLUMN_CAMERA_COUNT = "camera_count";
    }

}
