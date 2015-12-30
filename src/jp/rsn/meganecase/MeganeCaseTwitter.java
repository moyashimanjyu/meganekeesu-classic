package jp.rsn.meganecase;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import twitter4j.ConnectionLifeCycleListener;
import twitter4j.DirectMessage;
import twitter4j.GeoLocation;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserStreamAdapter;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.media.ImageUpload;
import twitter4j.media.ImageUploadFactory;
import twitter4j.media.MediaProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

public class MeganeCaseTwitter {

    private static final String CONSUMER_KEY = "xxxxxxxxxxxxxxxxxxxx";
    private static final String CONSUMER_SECRET = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    private static final String TWITPIC_API_KEY = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

    private final MainBaseActivity meganeCase;
    private final TimelineData data;

    private TwitterStream twitterStream = null;
    private Twitter twitter = null;
    private Configuration config = null;
    private String screenName = null;
    private boolean isStarted = false;
    private boolean vibe = false;
    private boolean beast = false;
    private boolean voice = false;

    public final boolean isVibe() {
        return vibe;
    }

    public final void setVibe(boolean vibe) {
        this.vibe = vibe;
    }

    public final boolean isBeast() {
        return beast;
    }

    public final void setBeast(boolean beast) {
        this.beast = beast;
    }

    public final boolean isVoice() {
        return voice;
    }

    public final void setVoice(boolean voice) {
        this.voice = voice;
    }

    public final String getScreenName() {
        return screenName;
    }

    public final void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public static final Configuration getConfiguration(App.Token token) {
        return getConfiguration(token, false);
    }

    public static final Configuration getConfiguration(App.Token token, boolean beast) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(CONSUMER_KEY);
        builder.setOAuthConsumerSecret(CONSUMER_SECRET);
        builder.setOAuthAccessToken(token.getToken());
        builder.setOAuthAccessTokenSecret(token.getTokenSecret());
        builder.setMediaProviderAPIKey(TWITPIC_API_KEY);
        builder.setUserStreamRepliesAllEnabled(beast);
        builder.setAsyncNumThreads(2);
        return builder.build();
    }

    public static final RequestToken getRequestToken() throws TwitterException {
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        return twitter.getOAuthRequestToken("http://bonheur.rsn.jp/hisaki/oauth.html");
    }

    public static final AccessToken getAccessToken(RequestToken requestToken, String verifier) throws TwitterException {
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        return twitter.getOAuthAccessToken(requestToken, verifier);
    }

    public static final List<Info> getMentions(App.Token token) {
        // for MeganekeS widget
        List<Info> result = new ArrayList<Info>();
        try {
            Configuration config = getConfiguration(token);
            Twitter twitter = new TwitterFactory(config).getInstance();
            String screenName = twitter.getScreenName();
            for (Status status : twitter.getMentionsTimeline()) {
                Info info = status2Info(status, screenName);
                result.add(info);
            }
        }
        catch (TwitterException e) {
        }
        return result;
    }

    public static final void post(StatusUpdate statusUpdate, App.Token token) throws TwitterException {
        Configuration config = getConfiguration(token);
        Twitter twitter = new TwitterFactory(config).getInstance();
        twitter.updateStatus(statusUpdate);
    }

    public static final void reply(String tweet, App.Token token, Long inReplyToStatusId) throws TwitterException {
        StatusUpdate statusUpdate = new StatusUpdate(tweet);
        statusUpdate.setInReplyToStatusId(inReplyToStatusId);
        post(statusUpdate, token);
    }

    public static final void retweet(Long id, App.Token token) throws TwitterException {
        Configuration config = getConfiguration(token);
        Twitter twitter = new TwitterFactory(config).getInstance();
        twitter.retweetStatus(id);
    }

    public static final void favorite(Long id, App.Token token) throws TwitterException {
        Configuration config = getConfiguration(token);
        Twitter twitter = new TwitterFactory(config).getInstance();
        twitter.createFavorite(id);
    }

    public static final void delete(Long id, App.Token token) throws TwitterException {
        Configuration config = getConfiguration(token);
        Twitter twitter = new TwitterFactory(config).getInstance();
        twitter.destroyStatus(id);
    }

    public static final void twitpic(String tweet, InputStream in, App.Token token, Long inReplyTo, GeoLocation geo)
            throws TwitterException {
        Configuration config = getConfiguration(token);
        ImageUpload iu = new ImageUploadFactory(config).getInstance(MediaProvider.TWITTER);
        String url = iu.upload(String.valueOf(System.currentTimeMillis()), in, tweet);

        int len = tweet.length() + url.length() + 1;
        if (len > 140) {
            tweet = tweet.substring(0, 140 - url.length() - 1);
        }

        Twitter twitter = new TwitterFactory(config).getInstance();
        StatusUpdate statusUpdate = new StatusUpdate(tweet + " " + url);
        if (inReplyTo != null && inReplyTo != 0) {
            statusUpdate.setInReplyToStatusId(inReplyTo);
        }
        if (geo != null) {
            statusUpdate.setLocation(geo);
        }
        twitter.updateStatus(statusUpdate);
    }

    public final void openHistory(HistoryAdapter adapter, Info first) {
        new HistoryGetter(adapter, first).start();
    }

    public class HistoryGetter extends Thread {
        private final HistoryAdapter adapter;
        private final Info first;

        public HistoryGetter(HistoryAdapter adapter, Info first) {
            this.adapter = adapter;
            this.first = first;
        }

        public void run() {
            try {
                if (twitter != null && first != null) {
                    Status status = twitter.showStatus(first.getId());
                    while (status.getInReplyToStatusId() != -1) {
                        status = twitter.showStatus(status.getInReplyToStatusId());
                        adapter.add(status2Info(status, first.getUsername()));
                    }
                }
            }
            catch (TwitterException e) {
                meganeCase.showError();
            }
        }
    }

    public MeganeCaseTwitter(MainBaseActivity meganeCase, TimelineData data) {
        this.meganeCase = meganeCase;
        this.data = data;
    }

    public void startStreaming(App.Token token) {
        if (isStarted) {
            return;
        }
        config = getConfiguration(token, beast);
        twitter = new TwitterFactory(config).getInstance();
        try {
            screenName = twitter.getScreenName();
        }
        catch (Exception e) {
            meganeCase.showError();
        }
        new Thread() {
            public void run() {
                twitterStream = new TwitterStreamFactory(config).getInstance();
                if (screenName == null) {
                    try {
                        screenName = twitterStream.getScreenName();
                    }
                    catch (Exception e) {
                        meganeCase.showError();
                    }
                }
                twitterStream.addListener(new SampleUserStreamListener());
                twitterStream.addConnectionLifeCycleListener(new ConnectionLifeCycleListener() {
                    public void onDisconnect() {
                        Log.v(App.TAG, "MeganeCaseTwitter onDisconnect");
                    }

                    public void onConnect() {
                        Log.v(App.TAG, "MeganeCaseTwitter onConnect");
                        new Thread() {
                            public void run() {
                                try {
                                    List<Status> hList = twitter.getHomeTimeline();
                                    Collections.reverse(hList);
                                    Iterator<Status> hIt = hList.iterator();
                                    new FirstAdder(hIt).start();
                                    List<Status> mList = twitter.getMentionsTimeline();
                                    Collections.reverse(mList);
                                    Iterator<Status> mIt = mList.iterator();
                                    new FirstAdder(mIt).start();
                                }
                                catch (Exception e) {
                                    meganeCase.showError();
                                }
                            }
                        }.start();
                    }

                    public void onCleanUp() {
                        Log.v(App.TAG, "MeganeCaseTwitter onCleanUp");
                    }
                });
                twitterStream.user();
                Log.v(App.TAG, "open stream.");
            }
        }.start();
        try {
            List<Status> mList = twitter.getMentionsTimeline();
            Collections.reverse(mList);
            Iterator<Status> mIt = mList.iterator();
            new FirstAdder(mIt).start();
        }
        catch (Exception e) {
            meganeCase.showError();
        }
        new Thread() {
            public void run() {
                try {
                    List<Status> hList = twitter.getHomeTimeline();
                    Collections.reverse(hList);
                    Iterator<Status> hIt = hList.iterator();
                    new FirstAdder(hIt).start();
                }
                catch (Exception e) {
                    meganeCase.showError();
                }
            }
        }.start();
        isStarted = true;
    }

    public void closeStreaming() {
        if (!isStarted) {
            return;
        }
        isStarted = false;
        try {
            if (twitterStream != null) {
                twitterStream.shutdown();
                Log.v(App.TAG, "close stream.");
            }
        }
        catch (Exception e) {
            meganeCase.showError();
        }
    }

    private class FirstAdder extends Thread {
        private final Iterator<Status> it;

        public FirstAdder(Iterator<Status> it) {
            this.it = it;
        }

        public void run() {
            while (it.hasNext()) {
                Status status = it.next();
                Info info = status2Info(status, screenName);
                data.addInfo(info);
            }
        }
    }

    protected static Info status2Info(Status status, String screenName) {
        Info info = new Info();
        info.setStatus(status);
        info.setCreated(status.getCreatedAt());

        if (status.isRetweet()) {
            info.setRetweet(true);
            info.setRtIconUrl(status.getUser().getProfileImageURL().toString());
            info.setRtUsername(status.getUser().getScreenName());
            status = status.getRetweetedStatus();
            info.setRtTime(status.getCreatedAt());
        }

        StringBuilder text = new StringBuilder(140);
        text.append(status.getUser().getScreenName()).append(" ").append(status.getText());
        info.setText(text.toString());
        info.setIconUrl(status.getUser().getProfileImageURL().toString());
        info.setUsername(status.getUser().getScreenName());
        info.setUserTextName(status.getUser().getName());
        info.setId(status.getId());
        info.setInReplyTo(status.getInReplyToStatusId());
        info.setProtected(status.getUser().isProtected());
        info.setVia(status.getSource().replaceAll("<.+?>", ""));
        info.setLang(status.getUser().getLang());

        if (screenName != null) {
            if (info.getUsername().equals(screenName)) {
                info.setMine(true);
            }
            else if (info.getText().toUpperCase().contains("@" + screenName.toUpperCase()) && !status.isRetweet()) {
                info.setMentions(true);
            }
        }

        List<String> urls = null;
        if (status.getURLEntities() != null && status.getURLEntities().length > 0) {
            urls = new ArrayList<String>(status.getURLEntities().length);
            for (int i = 0; i < status.getURLEntities().length; i++) {
                URLEntity urlEntity = status.getURLEntities()[i];
                String url = urlEntity.getExpandedURL();
                if (url == null) {
                    url = urlEntity.getURL();
                    if (url == null) {
                        continue;
                    }
                }
                else {
                    String before = urlEntity.getURL().toString();
                    info.setText(info.getText().replace(before, url.toString()));
                }
                String urlString = url.toString();
                if (Uri.parse(url).getHost().equals("bit.ly")) {
                    String expand = Bitly.expand(urlString);
                    if (expand != null && expand.length() > 0) {
                        info.setText(info.getText().replace(urlString, expand));
                        urlString = expand;
                    }
                }
                urls.add(urlString);
            }
            String[] array = new String[urls.size()];
            urls.toArray(array);
            info.setUrlStrings(array);
        }
        if (status.getMediaEntities() != null && status.getMediaEntities().length > 0) {
            if (urls == null) {
                urls = new ArrayList<String>(status.getMediaEntities().length);
            }
            for (int i = 0; i < status.getMediaEntities().length; i++) {
                MediaEntity mediaEntity = status.getMediaEntities()[i];
                String url = mediaEntity.getMediaURL();
                if (url == null) {
                    continue;
                }
                String before = mediaEntity.getURL().toString();
                info.setText(info.getText().replace(before, url.toString()));
                urls.add(url.toString());
            }
            String[] array = new String[urls.size()];
            urls.toArray(array);
            info.setUrlStrings(array);
        }
        return info;
    }

    public class SampleUserStreamListener extends UserStreamAdapter {
        @Override
        public void onStatus(Status status) {
            super.onStatus(status);
            if (isStarted) {
                Info info = status2Info(status, screenName);
                data.addInfo(info);
                if (voice && status.isRetweet()
                        && status.getRetweetedStatus().getUser().getScreenName().equals(screenName)) {
                    ((MainActivity) meganeCase).getMediaPlayerManager().voiceRt();
                }
                if (voice && info.isMentions()) {
                    ((MainActivity) meganeCase).getMediaPlayerManager().voiceReply();
                }
                if (vibe && info.isMentions()) {
                    ((Vibrator) meganeCase.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(500);
                }
            }
        }

        @Override
        public void onFavorite(User source, User target, Status favoritedStatus) {
            super.onFavorite(source, target, favoritedStatus);
            if (source.getScreenName().equals(screenName)) {
                return;
            }
            if (isStarted) {
                Info info = new Info();
                StringBuilder text = new StringBuilder(160);
                text.append(source.getScreenName()).append(meganeCase.getString(R.string.msg_favorite)).append("\n")
                        .append(favoritedStatus.getText());
                info.setText(text.toString());
                info.setUsername(source.getScreenName());
                info.setId(-1L);
                info.setIconUrl(source.getProfileImageURL().toString());
                info.setFav(true);
                data.addInfo(info);
                if (voice) {
                    ((MainActivity) meganeCase).getMediaPlayerManager().voiceFav();
                }
                Intent intent = new Intent("jp.rsn.meganekeesu.NOTICE_FAVORITE");
                intent.putExtra("screen_name", source.getScreenName());
                intent.putExtra("user_id", source.getId());
                intent.putExtra("status_id", favoritedStatus.getId());
                meganeCase.sendBroadcast(intent);
            }
        }

        @Override
        public void onUnfavorite(User source, User target, Status favoritedStatus) {
            super.onUnfavorite(source, target, favoritedStatus);
            if (source.getScreenName().equals(screenName)) {
                return;
            }
            if (isStarted) {
                Info info = new Info();
                StringBuilder text = new StringBuilder(160);
                text.append(source.getScreenName()).append(meganeCase.getString(R.string.msg_unfavorite)).append("\n")
                        .append(favoritedStatus.getText());
                info.setText(text.toString());
                info.setUsername(source.getScreenName());
                info.setId(-1L);
                info.setIconUrl(source.getProfileImageURL().toString());
                data.addInfo(info);
                Intent intent = new Intent("jp.rsn.meganekeesu.NOTICE_UNFAVORITE");
                intent.putExtra("screen_name", source.getScreenName());
                intent.putExtra("user_id", source.getId());
                intent.putExtra("status_id", favoritedStatus.getId());
                meganeCase.sendBroadcast(intent);
            }
        }

        @Override
        public void onFollow(User source, User target) {
            super.onFollow(source, target);
            if (source.getScreenName().equals(screenName)) {
                return;
            }
            if (isStarted) {
                Info info = new Info();
                info.setText(source.getScreenName() + meganeCase.getString(R.string.msg_follow));
                info.setUsername(source.getScreenName());
                info.setId(-1L);
                info.setIconUrl(source.getProfileImageURL().toString());
                data.addInfo(info);
                Intent intent = new Intent("jp.rsn.meganekeesu.NOTICE_FOLLOW");
                intent.putExtra("screen_name", source.getScreenName());
                intent.putExtra("user_id", source.getId());
                meganeCase.sendBroadcast(intent);
            }
        }

        @Override
        public void onBlock(User source, User blockedUser) {
            super.onBlock(source, blockedUser);
            if (source.getScreenName().equals(screenName)) {
                return;
            }
            if (isStarted) {
                Info info = new Info();
                info.setText(source.getScreenName() + meganeCase.getString(R.string.msg_block));
                info.setUsername(source.getScreenName());
                info.setId(-1L);
                info.setIconUrl(source.getProfileImageURL().toString());
                data.addInfo(info);
            }
        }

        @Override
        public void onDirectMessage(DirectMessage directMessage) {
            super.onDirectMessage(directMessage);
            if (directMessage.getSenderScreenName().equals(screenName)) {
                return;
            }
            if (isStarted) {
                Info info = new Info();
                StringBuilder text = new StringBuilder(140);
                text.append("D ").append(directMessage.getSenderScreenName()).append(" ")
                        .append(directMessage.getText());
                info.setText(text.toString());
                info.setUsername(directMessage.getSenderScreenName());
                info.setId(directMessage.getId());
                info.setCreated(directMessage.getCreatedAt());
                info.setIconUrl(directMessage.getSender().getProfileImageURL().toString());
                info.setDirectMessage(true);
                data.addInfo(info);
                if (voice) {
                    ((MainActivity) meganeCase).getMediaPlayerManager().voiceDm();
                }
                if (vibe) {
                    ((Vibrator) meganeCase.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(500);
                }
            }
        }

        @Override
        public void onException(Exception e) {
            super.onException(e);
            if (isStarted) {
                meganeCase.showError();
            }
        }
    }
}
