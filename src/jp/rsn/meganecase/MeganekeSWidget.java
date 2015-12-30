package jp.rsn.meganecase;

import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

public class MeganekeSWidget extends AppWidgetProvider {

    public static final String ACTION_WIDGET_UPDATE = "jp.rsn.meganecase.REPLY_WIDGET_UPDATE";

    private static int[] imageIds = { R.id.meganekesImage01, R.id.meganekesImage02,
            R.id.meganekesImage03, R.id.meganekesImage04, R.id.meganekesImage05,
            R.id.meganekesImage06, R.id.meganekesImage07, R.id.meganekesImage08,
            R.id.meganekesImage09, R.id.meganekesImage10 };
    private static int[] textIds = { R.id.meganekesText01, R.id.meganekesText02,
            R.id.meganekesText03, R.id.meganekesText04, R.id.meganekesText05, R.id.meganekesText06,
            R.id.meganekesText07, R.id.meganekesText08, R.id.meganekesText09, R.id.meganekesText10 };

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int id : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.meganekes);
            Intent intent = new Intent(ACTION_WIDGET_UPDATE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.reply_widget, pendingIntent);
            updateViews(context, views);
            appWidgetManager.updateAppWidget(id, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_WIDGET_UPDATE.equals(intent.getAction())) {
            ComponentName widget = new ComponentName(context, MeganekeSWidget.class);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widget);
            for (int id : appWidgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.meganekes);
                updateViews(context, views);
                appWidgetManager.updateAppWidget(id, views);
            }
        }
    }

    private void updateViews(Context context, RemoteViews views) {
        IconManager iconManager = new IconManager(context);
        App.Token token = App.getToken(context);
        if (token != null) {
            Log.v(App.TAG, "has oauth token.");
            List<Info> mentions = MeganeCaseTwitter.getMentions(token);
            for (int i = 0; i < mentions.size() && i < 10; i++) {
                Info info = mentions.get(i);
                views.setTextViewText(textIds[i], info.getText());
                views.setImageViewUri(imageIds[i], Uri.parse("about:brank"));
                Bitmap bitmap = iconManager.getIcon(info.getUsername(), info.getIconUrl());
                views.setImageViewBitmap(imageIds[i], bitmap);
            }
        }
        else {
            Log.v(App.TAG, "token missing.");
            for (int i = 0; i < 10; i++) {
                views.setTextViewText(textIds[i], "");
                views.setImageViewUri(imageIds[i], Uri.parse("about:brank"));
            }
        }
    }
}
