package Util;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by Rajvi on 4/20/2018.
 */

public class Prefs {

    SharedPreferences preferences;

    public Prefs(Activity activity) {
        preferences = activity.getPreferences(Activity.MODE_PRIVATE);
    }


    public void setCity(String city) {
        preferences.edit().putString("city", city).commit();
    }

    //If user has not chose a city, return default!
    public String getCity() {
        return preferences.getString("city", "Spokane");
    }
}
