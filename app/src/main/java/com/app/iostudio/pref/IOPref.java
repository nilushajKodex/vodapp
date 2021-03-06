package com.app.iostudio.pref;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by vis on 08-05-2015.
 */
public class IOPref {

    private static final IOPref instance = new IOPref();
    private static final String PREF_NAME = "IOPref";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    public Set<String> getAllPreferences(Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Map<String, ?> entries = prefs.getAll();
        return entries.keySet();
    }
    public Map<String, ?> getAllPreferencesComplete(Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Map<String, ?> entries = prefs.getAll();
        return entries;
    }

    public void removeKey(Context context, String key) {
        try {
            SharedPreferences info = context.getApplicationContext()
                    .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = info.edit();
            editor.remove(key);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
            //  System.exit(0);
        }
    }

    public long getAllPreferencesSize(Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return prefs.getAll().size();
    }

    public interface PreferenceKey {

        public static final String userID = "userID";
        public static final String showGettingStartedBit = "showGettingStartedBit";
        public static final String isFBLogin = "isFBLoginSlurrp";


        String isMute="isMute";
    }

    private IOPref() {

    }

    public static IOPref getInstance() {
        return instance;
    }

    public void resetPreference(Context context) {
        SharedPreferences info = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = info.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * Save the string into the shared preference.
     *
     * @param context Context object.
     * @param key     Key to save.
     * @param value   String value associated with the key.
     */
    public void saveString(Context context, String key, String value) {
        try {
            SharedPreferences info = context.getApplicationContext()
                    .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = info.edit();
            editor.putString(key, value);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
          //  Log.e("SkedulePref", ":save:exit");
          //  System.exit(0);
        }
    }


    /**
     * Save the string into the shared preference.
     *  @param context Context object.
     * @param key     Key to save.
     * @param value   String value associated with the key.
     */
    public void saveStringSet(Context context, String key, HashSet<String> value) {
        try {
            SharedPreferences info = context.getApplicationContext()
                    .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = info.edit();
            editor.putStringSet(key, value);
            editor.apply();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the string value of key from shared preference.
     *
     * @param key      Key whose value need to be searched.
     * @param defValue Default value to return in case no such key exist.
     * @return Value associated with the key.
     */
    public String getString(Context context, String key, String defValue) {
        SharedPreferences info = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return info.getString(key, defValue);
    }

    /**
     * Get the string value of key from shared preference.
     *
     * @param key      Key whose value need to be searched.
     * @param defValue Default value to return in case no such key exist.
     * @return Value associated with the key.
     */
    public HashSet<String> getStringSet(Context context, String key, HashSet<String> defValue) {
        SharedPreferences info = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return (HashSet<String>) info.getStringSet(key, defValue);
    }

    /**
     * Save the boolean into the shared preference.
     *
     * @param context Context object.
     * @param key     Key to save.
     * @param value   String value associated with the key.
     */
    public void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences info = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = info.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * Get the boolean value of key from shared preference.
     *
     * @param key      Key whose value need to be searched.
     * @param defValue Default value to return in case no such key exist.
     * @return Value associated with the key.
     */
    public boolean getBoolean(Context context, String key, boolean defValue) {
        SharedPreferences info = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return info.getBoolean(key, defValue);
    }

    /**
     * Save the Integer into the shared preference.
     *
     * @param context Context object.
     * @param key     Key to save.
     * @param value   Integer value associated with the key.
     */
    public void saveInt(Context context, String key, int value) {
        SharedPreferences info = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = info.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * Get the Integer value of key from shared preference.
     *
     * @param key      Key whose value need to be searched.
     * @param defValue Default value to return in case no such key exist.
     * @return Value associated with the key.
     */
    public int getInt(Context context, String key, int defValue) {
        SharedPreferences info = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return info.getInt(key, defValue);
    }

    /**
     * Save the Integer into the shared preference.
     *
     * @param context Context object.
     * @param key     Key to save.
     * @param value   Integer value associated with the key.
     */
    public void saveDouble(Context context, String key, Double value) {
        SharedPreferences info = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = info.edit();
        editor.putLong(key, Double.doubleToRawLongBits(value));
        editor.commit();
    }

    /**
     * Get the Integer value of key from shared preference.
     *
     * @param key      Key whose value need to be searched.
     * @param defValue Default value to return in case no such key exist.
     * @return Value associated with the key.
     */
    public double getDouble(Context context, String key, double defValue) {

        SharedPreferences info = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return Double.longBitsToDouble(info.getLong(key, Double.doubleToLongBits(defValue)));

    }

}