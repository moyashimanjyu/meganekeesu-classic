package jp.rsn.meganecase;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class OnseiWidget extends AppWidgetProvider {

    public static final String ACTION_WIDGET_SWITCH = "jp.rsn.meganecase.ONSEI_WIDGET_SWITCH";
    public static final String ACTION_WIDGET_UPDATE = "jp.rsn.meganecase.ONSEI_WIDGET_UPDATE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int id : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.onsei);
            Intent intent = new Intent(ACTION_WIDGET_SWITCH);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.onsei_switch, pendingIntent);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean("mode_voice", false)) {
                views.setTextViewText(R.id.onsei_text, "ON");
            }
            else {
                views.setTextViewText(R.id.onsei_text, "OFF");
            }
            appWidgetManager.updateAppWidget(id, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_WIDGET_SWITCH.equals(intent.getAction())) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Editor editor = prefs.edit();
            editor.putBoolean("mode_voice", !prefs.getBoolean("mode_voice", false));
            editor.commit();
            ComponentName widget = new ComponentName(context, OnseiWidget.class);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widget);
            for (int id : appWidgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.onsei);
                if (prefs.getBoolean("mode_voice", false)) {
                    views.setTextViewText(R.id.onsei_text, "ON");
                }
                else {
                    views.setTextViewText(R.id.onsei_text, "OFF");
                }
                appWidgetManager.updateAppWidget(id, views);
            }
        }
        else if (ACTION_WIDGET_UPDATE.equals(intent.getAction())) {
            ComponentName widget = new ComponentName(context, OnseiWidget.class);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(widget);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            for (int id : appWidgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.onsei);
                if (prefs.getBoolean("mode_voice", false)) {
                    views.setTextViewText(R.id.onsei_text, "ON");
                }
                else {
                    views.setTextViewText(R.id.onsei_text, "OFF");
                }
                appWidgetManager.updateAppWidget(id, views);
            }
        }
    }
}
