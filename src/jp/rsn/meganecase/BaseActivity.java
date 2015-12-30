package jp.rsn.meganecase;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

public class BaseActivity extends Activity {

    private static Method fullScreenMode;

    static {
        try {
            Class<?> sgm = Class.forName("jp.co.sharp.android.softguide.SoftGuideManager");
            fullScreenMode = sgm.getMethod("setFullScreenMode", boolean.class);
        }
        catch (Exception e) {
            fullScreenMode = null;
        }
    }

    private Handler handler = null;

    protected final Handler getHandler() {
        if (handler == null) {
            handler = new Handler(getMainLooper());
        }
        return handler;
    }

    public final Context getContext() {
        return this;
    }

    protected final App getApp() {
        return (App) getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(App.TAG, getLocalClassName() + " onCreate");
        getApp().getTracker();
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("mode_powersave", false)) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (fullScreenMode != null) {
            try {
                fullScreenMode.invoke(null, true);
            }
            catch (Exception e) {
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(App.TAG, getLocalClassName() + " onPause");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v(App.TAG, getLocalClassName() + " onRestoreInstanceState");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(App.TAG, getLocalClassName() + " onSaveInstanceState");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.v(App.TAG, getLocalClassName() + " onConfigurationChanged");
    }

	protected final void trackEvent(String action) {
		try {
			getApp().getTracker().send(
					new HitBuilders.EventBuilder()
							.setCategory(getLocalClassName()).setAction(action)
							.build());
		} catch (Exception e) {
		}
	}

    private Runnable errorToast = null;

    public final void showError() {
        if (!isFinishing()) {
            if (errorToast == null) {
                errorToast = new Runnable() {
                    public void run() {
                        Toast.makeText(BaseActivity.this, R.string.error, Toast.LENGTH_SHORT)
                                .show();
                    }
                };
            }
            getHandler().post(errorToast);
        }
    }
}
