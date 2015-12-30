package jp.rsn.meganecase;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SettingActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(App.TAG, getLocalClassName() + " onCreate");
        addPreferencesFromResource(R.xml.preference);
        App app = (App)getApplication();
        app.getTracker();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(App.TAG, getLocalClassName() + " onDestroy");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(App.TAG, getLocalClassName() + " onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(App.TAG, getLocalClassName() + " onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(App.TAG, getLocalClassName() + " onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(App.TAG, getLocalClassName() + " onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(App.TAG, getLocalClassName() + " onPause");
    }
}
