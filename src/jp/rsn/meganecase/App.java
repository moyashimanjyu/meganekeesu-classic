package jp.rsn.meganecase;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class App extends Application {

    public static final String TAG = "MeganeCase";
    
    public static final Token getToken(Context context) {
        Token token = new Token();
        SharedPreferences pref = context.getSharedPreferences(TAG, MODE_PRIVATE);
        token.setToken(pref.getString("token", null));
        token.setTokenSecret(pref.getString("tokenSecret", null));
        if (Util.isEmpty(token.getToken()) || Util.isEmpty(token.getTokenSecret())) {
            return null;
        }
        return token;
    }

    public static final class Token {
        private String token;
        private String tokenSecret;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getTokenSecret() {
            return tokenSecret;
        }

        public void setTokenSecret(String tokenSecret) {
            this.tokenSecret = tokenSecret;
        }
    }

    private Tracker tracker;

    public synchronized Tracker getTracker() { 
    	if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(R.xml.analytics);
    	}
    	return tracker;
    }

	@Override
	public void onCreate() {
		super.onCreate();
		getTracker();
	}
}
