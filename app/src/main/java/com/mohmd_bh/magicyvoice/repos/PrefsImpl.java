package com.mohmd_bh.magicyvoice.repos;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsImpl implements Prefs {

    private static final String PREF_NAME = "com.mohmd_bh.magicyvoice.repos.PrefsImpl";
    private static final String PREF_KEY_IS_FIRST_RUN = "is_first_run";
    private static final String PREF_KEY_ACTIVE_RECORD = "active_record";
    private static final String PREF_KEY_FILE_PATH = "file_path";
    private SharedPreferences sharedPreferences;
    private volatile static PrefsImpl instance;


    private PrefsImpl(Context context){
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static PrefsImpl getInstance(Context context){
        if(instance == null){
            synchronized (PrefsImpl.class) {
                if (instance == null) {
                    instance = new PrefsImpl(context);
                }
            }
        }
        return instance;
    }


    @Override
    public boolean isFirstRun() {
        return !sharedPreferences.contains(PREF_KEY_IS_FIRST_RUN) || !sharedPreferences
                .getBoolean(PREF_KEY_IS_FIRST_RUN, false);
    }

    @Override
    public void firstRunExecuted() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_KEY_IS_FIRST_RUN, true);
        editor.apply();
    }

    @Override
    public void setActiveFile(String path) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_KEY_FILE_PATH, path);
        editor.apply();
    }

    @Override
    public String getActiveFile(){
        if(sharedPreferences.contains(PREF_KEY_FILE_PATH) && !sharedPreferences.getString(PREF_KEY_FILE_PATH,"").equals("")){
            return sharedPreferences.getString(PREF_KEY_FILE_PATH,"");
        }
        return null;
    }




}
