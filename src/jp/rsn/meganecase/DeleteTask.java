package jp.rsn.meganecase;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class DeleteTask extends AsyncTask<Info, Void, Boolean> {

    private final Context context;
    private TaskErrorListener errorListener;

    public DeleteTask(Context context) {
        this.context = context;
    }

    public final TaskErrorListener getOnErrorListener() {
        return errorListener;
    }

    public final void setOnErrorListener(TaskErrorListener listener) {
        this.errorListener = listener;
    }

    @Override
    protected Boolean doInBackground(Info... infos) {
        App.Token token = App.getToken(context);
        try {
            MeganeCaseTwitter.delete(infos[0].getId(), token);
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
            Toast.makeText(context, R.string.finish_delete, Toast.LENGTH_SHORT).show();
        }
        errorListener = null;
    }
}