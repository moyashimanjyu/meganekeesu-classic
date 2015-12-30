package jp.rsn.meganecase;

import java.io.BufferedInputStream;
import java.io.InputStream;

import twitter4j.GeoLocation;
import twitter4j.StatusUpdate;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

public class TweetTask extends Thread {

    private final Context context;
    private final String tweet;
    private Long inReplyTo = null;
    private Uri picture = null;
    private Double latitude = null;
    private Double longitude = null;
    private TaskErrorListener errorListener = null;

    public TweetTask(Context context, String tweet) {
        this.context = context;
        this.tweet = tweet;
        setPriority(Thread.NORM_PRIORITY + 1);
    }

    public TweetTask(Context context, String tweet, Long inReplyTo) {
        this(context, tweet);
        this.inReplyTo = inReplyTo;
    }

    public final void setInReplyTo(Long inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    public final void setPicture(Uri picture) {
        this.picture = picture;
    }

    public final void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public final void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public final TaskErrorListener getOnErrorListener() {
        return errorListener;
    }

    public final void setOnErrorListener(TaskErrorListener listener) {
        this.errorListener = listener;
    }

    @Override
    public void run() {
        App.Token token = App.getToken(context);
        try {
            StatusUpdate statusUpdate = new StatusUpdate(tweet);
            if (inReplyTo != null && inReplyTo != 0) {
                statusUpdate.setInReplyToStatusId(inReplyTo);
            }
            GeoLocation geo = null;
            if (latitude != null && longitude != null) {
                geo = new GeoLocation(latitude, longitude);
                statusUpdate.setLocation(geo);
            }
            if (picture != null) {
                ContentResolver resolver = context.getContentResolver();
                InputStream in = resolver.openInputStream(picture);
                in = new BufferedInputStream(in);
                MeganeCaseTwitter.twitpic(tweet, in, token, inReplyTo, geo);
            }
            else {
                MeganeCaseTwitter.post(statusUpdate, token);
            }
        }
        catch (Exception e) {
            if (errorListener != null) {
                errorListener.onError(e);
            }
        }
        errorListener = null;
    }
}