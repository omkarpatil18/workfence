package com.workfence.others;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Utils {
    public static int zone = 3;
    public static float distance = 0;
    public static int rssi = 0;
    public static int swiped = 0;
    public static String model = "Not Available";
    public static String otherName = "No Name";
    public static boolean wfh = false;
    public static float otherTxPower = -80;
    public static int fragment_id = 0;
    private static String ClientID = "";
    private static String companyID = "";
    private static String authToken = "";
    private static String name = "";
    private static String phone = "";
    private static boolean isAdmin = false;

    public static String getUserParams(Context context) {
        return "User params from : " + context.toString() + "\n" +
                "name : " + getName(context) + "\n" +
                "phone : " + getPhone(context) + "\n" +
                "company token: " + getCompanyID(context) + "\n" +
                "client ID : " + getClientID(context) + "\n" +
                "authToken : " + getAuthToken(context) + "\n" +
                "Is supervisor? " + getIsAdmin(context);
    }

    public static void clearPrefs(Context context) {

        model = "Not Available";
        otherName = "No Name";
        wfh = false;
        otherTxPower = -80;
        fragment_id = 0;
        ClientID = "";
        companyID = "";
        authToken = "";
        name = "";
        phone = "";
        isAdmin = false;

        SharedPreferences sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().apply();
    }

    public static String getClientID(Context context) {
        if (ClientID.length() == 0 || ClientID.contains("-")) {
            SharedPreferences sharedPreferences;
            sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
            ClientID = sharedPreferences.getString("clientID", "-1111");
        }
        return ClientID;
    }

    public static void setClientID(Context context, String s) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("clientID", s).apply();
    }

    public static String getCompanyID(Context context) {
        if (companyID.length() == 0) {
            SharedPreferences sharedPreferences;
            sharedPreferences = context.getApplicationContext().getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
            companyID = sharedPreferences.getString("companyID", "");
        }
        return companyID;
    }

    public static void setCompanyID(Context context, String s) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("companyID", s).apply();
    }

    public static String getAuthToken(Context context) {
        if (authToken.length() == 0) {
            SharedPreferences sharedPreferences;
            sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
            authToken = sharedPreferences.getString("authToken", "");
        }
        return authToken;
    }

    public static void setAuthToken(Context context, String s) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("authToken", s).apply();
    }

    public static String getName(Context context) {
        if (name.length() == 0) {
            SharedPreferences sharedPreferences;
            sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
            name = sharedPreferences.getString("name", "");
        }
        return name;
    }

    public static void setName(Context context, String s) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("name", s).apply();
    }

    public static String getPhone(Context context) {
        if (phone.length() == 0) {
            SharedPreferences sharedPreferences;
            sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
            phone = sharedPreferences.getString("phone", "");
        }
        return phone;
    }

    public static void setPhone(Context context, String s) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("phone", s).apply();
    }

    public static boolean getIsAdmin(Context context) {
        SharedPreferences sharedPreferences;
        sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        isAdmin = sharedPreferences.getBoolean("is_supervisor", false);
        return isAdmin;
    }

    public static void setIsAdmin(Context context, boolean i) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        Log.i("TAG", "setIsAdmin: " + i);
        editor.putBoolean("is_supervisor", i).apply();
    }


    public static void setString(Context context, String key, String val) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, val).apply();
    }

    public static void setBoolean(Context context, String key, Boolean val) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, val).apply();
    }

    public static void setLong(Context context, String key, Long val) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, val).apply();
    }

    public static void setFloat(Context context, String key, Float val) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(key, val).apply();
    }

    public static void setInteger(Context context, String key, Integer val) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, val).apply();
    }

    public static boolean getBoolean(Context context, String key, Boolean def) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, def);
    }

    public static int getInteger(Context context, String key, Integer def) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, def);
    }

    public static String getString(Context context, String key, String def) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, def);
    }

    public static Long getLong(Context context, String key, Long def) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        return sharedPreferences.getLong(key, def);
    }

    public static Float getFloat(Context context, String key, Float def) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("workfence.data", Context.MODE_PRIVATE);
        return sharedPreferences.getFloat(key, def);
    }

}