package jp.rsn.meganecase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.ListView;

public class DialogManager {

    public static final int DIALOG_WAIT = 0;
    public static final int DIALOG_ERROR = 1;
    public static final int DIALOG_RETWEET = 2;
    public static final int DIALOG_FAVORITE = 3;
    public static final int DIALOG_BLANK = 4;
    public static final int DIALOG_FAVRT = 5;
    public static final int DIALOG_SECRET = 6;
    public static final int DIALOG_HISTORY = 7;
    public static final int DIALOG_DELETE = 8;

    private final Context context;

    private DialogAction action;
    private OkClickListener ok;
    private DialogInterface.OnClickListener cancel;
    private EditText secretInput;
    private ListView listView;

    public DialogManager(Context context) {
        this.context = context;
        ok = new OkClickListener();
        cancel = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        };
    }

    public DialogManager(Context context, DialogAction action) {
        this(context);
        this.action = action;
    }

    public void setDialogAction(DialogAction action) {
        this.action = action;
    }

    public Dialog createDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = null;
        switch (id) {
        case DIALOG_WAIT:
            ProgressDialog progress = new ProgressDialog(context);
            progress.setMessage(context.getString(R.string.wait));
            progress.setCancelable(true);
            dialog = progress;
            break;

        case DIALOG_ERROR:
            dialog = new Dialog(context);
            dialog.setTitle(context.getString(R.string.error));
            break;

        case DIALOG_RETWEET:
        case DIALOG_FAVORITE:
        case DIALOG_BLANK:
        case DIALOG_FAVRT:
        case DIALOG_DELETE:
            builder = new AlertDialog.Builder(context);
            builder.setMessage(getMessage(id));
            builder.setCancelable(true);
            builder.setPositiveButton("OK", ok);
            builder.setNegativeButton("Cancel", cancel);
            dialog = builder.create();
            break;

        case DIALOG_SECRET:
            builder = new AlertDialog.Builder(context);
            secretInput = new EditText(context);
            secretInput.setWidth(((Activity) context).getWindowManager().getDefaultDisplay()
                    .getWidth() / 2);
            builder.setView(secretInput);
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    String text = secretInput.getText().toString();
                    action.secretAction(text);
                }
            });
            builder.setNegativeButton("Cancel", cancel);
            dialog = builder.create();
            break;

        case DIALOG_HISTORY:
            builder = new AlertDialog.Builder(context);
            listView = new ListView(context);
            builder.setView(listView);
            builder.setNegativeButton("Close", cancel);
            dialog = builder.create();
            break;

        default:
            break;
        }
        return dialog;
    }

    public void prepareDialog(Info info, int id, Dialog dialog) {
        switch (id) {
        case DIALOG_RETWEET:
        case DIALOG_FAVORITE:
        case DIALOG_BLANK:
        case DIALOG_FAVRT:
        case DIALOG_DELETE:
            ok.setInfo(info, id);
            break;
        default:
            break;
        }
    }

    public void prepareDialogForHistory(MeganeCaseTwitter twitter, Info info, int id, Dialog dialog) {
        switch (id) {
        case DIALOG_HISTORY:
            listView.setAdapter(new HistoryAdapter(context, twitter, info));
            break;
        default:
            break;
        }
    }

    public class OkClickListener implements DialogInterface.OnClickListener {

        private Info info = null;
        private int id = -1;

        public Info getInfo() {
            return info;
        }

        public void setInfo(Info info, int id) {
            this.info = info;
            this.id = id;
        }

        public void onClick(DialogInterface dialog, int id) {
            switch (this.id) {
            case DIALOG_RETWEET:
                action.retweetAction(info);
                break;
            case DIALOG_FAVORITE:
                action.favoriteAction(info);
                break;
            case DIALOG_BLANK:
                action.blankAction(info);
                break;
            case DIALOG_FAVRT:
                action.favrtAction(info);
                break;
            case DIALOG_DELETE:
                action.deleteAction(info);
                break;
            default:
                break;
            }
        }
    }

    private final String getMessage(int id) {
        switch (id) {
        case DIALOG_RETWEET:
            return context.getString(R.string.confirm_retweet);
        case DIALOG_FAVORITE:
            return context.getString(R.string.confirm_favorite);
        case DIALOG_BLANK:
            return context.getString(R.string.confirm_blank);
        case DIALOG_FAVRT:
            return context.getString(R.string.confirm_favrt);
        case DIALOG_DELETE:
            return context.getString(R.string.confirm_delete);
        default:
            return null;
        }
    }

    public static interface DialogAction {
        public void retweetAction(Info info);

        public void favoriteAction(Info info);

        public void blankAction(Info info);

        public void favrtAction(Info info);

        public void deleteAction(Info info);

        public void secretAction(String text);
    }
}
