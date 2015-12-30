package jp.rsn.meganecase;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class BlankTask extends AsyncTask<Info, Void, Boolean> {

    private final Context context;
    private TaskErrorListener errorListener;

    public BlankTask(Context context) {
        this.context = context;
    }

    public final TaskErrorListener getOnErrorListener() {
        return errorListener;
    }

    public final void setOnErrorListener(TaskErrorListener listener) {
        this.errorListener = listener;
    }

    private static final Pattern PATTERN_SCREENNAME = Pattern.compile("@[a-zA-z0-9_]+");

    @Override
    protected Boolean doInBackground(Info... infos) {
        App.Token token = App.getToken(context);
        Long statusId = infos[0].getId();

        List<String> ids = new ArrayList<String>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("mode_multireply", false)) {
            Matcher matcher = PATTERN_SCREENNAME.matcher(infos[0].getText());
            while (matcher.find()) {
                String str = matcher.group();
                if (str.equals("@" + infos[0].getUsername())) {
                    continue;
                }
                if (ids.contains(str)) {
                    continue;
                }
                ids.add(str);
            }
        }

        String tweet = "@" + infos[0].getUsername();
        try {
            MeganeCaseTwitter.reply(tweet, token, statusId);
            for (String id : ids) {
                MeganeCaseTwitter.reply(id, token, statusId);
            }
            return true;
        }
        catch (Exception e) {
            if (errorListener != null) {
                errorListener.onError(e);
            }
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            Toast.makeText(context, R.string.finish_blank, Toast.LENGTH_SHORT).show();
        }
        errorListener = null;
    }
}