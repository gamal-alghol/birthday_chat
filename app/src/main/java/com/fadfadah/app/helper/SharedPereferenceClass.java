package com.fadfadah.app.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fadfadah.app.R;

import static android.content.Context.MODE_PRIVATE;

public class SharedPereferenceClass {

    public static final String MY_PREFS_NAME = "MyPrefsFileFile";
    Context context;

    public void storeKey(Context context, String key, String value) {
        Log.d("SharedPereferenceClass", "storeKey: "+value);
        SharedPreferences msharedPreferences = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = msharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();

    }

    public void storeKey(Context context, String key, int value) {
        SharedPreferences msharedPreferences = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = msharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();

    }

    public void storeKey(Context context, String key, Boolean value) {
        SharedPreferences msharedPreferences = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = msharedPreferences.edit();
        editor.putBoolean(key, true);
        editor.apply();

    }

    public String getLng(Context context) {
        SharedPreferences msharedPreferences = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        if (msharedPreferences.getString("AppLanguage", "arabic").toLowerCase().equals("arabic")) {
            Log.d("SharedPereferenceClass", "getLng: arabic");
            return "ar";

        }
        Log.d("SharedPereferenceClass", "getLng: english");
        return "en";
    }

    public String getStoredKey(Context context, String key) {
        SharedPreferences msharedPreferences = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String storedvalue = msharedPreferences.getString(key, "");
        return storedvalue;
    }

    public Boolean getbooleanStoredKey(Context context, String key) {
        SharedPreferences msharedPreferences = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        boolean storedvalue = msharedPreferences.getBoolean(key, false);
        return storedvalue;
    }

    public int getintStoredKey(Context context, String key) {
        SharedPreferences msharedPreferences = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        int storedvalue = msharedPreferences.getInt(key, 0);
        return storedvalue;
    }

    public static void storeColor(int color, Context context) {
        SharedPreferences msharedPreferences = context.getSharedPreferences("toolbarcolor", MODE_PRIVATE);
        SharedPreferences.Editor editor = msharedPreferences.edit();
        editor.putInt("color", color);
        editor.apply();
    }

    public static int getStoredColor(Context context) {
        SharedPreferences msharedPreferences = context.getSharedPreferences("toolbarcolor", MODE_PRIVATE);
        int selectedcolor = msharedPreferences.getInt("color", R.color.colorPrimary);
        return selectedcolor;
    }

}


