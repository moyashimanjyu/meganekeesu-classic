package jp.rsn.meganecase;

import java.util.Iterator;

import twitter4j.FilterQuery;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import android.util.Log;

public class MeganeCaseTwitterSecret extends MeganeCaseTwitter {

    private final MainBaseActivity meganeCase;
    private final TimelineData data;
    private final String queryString;

    private TwitterStream twitterStream = null;
    private String screenName = null;
    private Configuration config = null;
    private boolean isStarted = false;

    public MeganeCaseTwitterSecret(MainBaseActivity meganeCase, TimelineData data,
            String queryString) {
        super(meganeCase, data);
        this.meganeCase = meganeCase;
        this.data = data;
        this.queryString = queryString;
    }

    @Override
    public void startStreaming(App.Token token) {
        if (isStarted) {
            return;
        }
        config = getConfiguration(token);
        new Thread(new Runnable() {
            public void run() {
                try {
                    Twitter twitter = new TwitterFactory(config).getInstance();
                    screenName = twitter.getScreenName();
                    new Thread(new Runnable() {
                        public void run() {
                            twitterStream = new TwitterStreamFactory(config).getInstance();
                            twitterStream.addListener(new SampleStatusListener());
                            FilterQuery query = new FilterQuery();
                            query.track(new String[] { queryString });
                            twitterStream.filter(query);
                            Log.v(App.TAG, "open stream.");
                        }
                    }).start();
                    firstTimeline(twitter);
                }
                catch (Exception e) {
                    meganeCase.showError();
                }
            }
        }).start();
        isStarted = true;
    }

    @Override
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

    private void firstTimeline(Twitter twitter) throws TwitterException {
        QueryResult result = twitter.search(new Query(queryString));
        Iterator<Status> it = result.getTweets().iterator();
        while (it.hasNext()) {
            Status tweet = it.next();
            Info info = new Info();
            info.setId(tweet.getId());
            info.setUsername(tweet.getUser().getScreenName());
            info.setIconUrl(tweet.getUser().getProfileImageURL());
            info.setCreated(tweet.getCreatedAt());
            info.setVia(tweet.getSource());
            info.setText(info.getUsername() + " " + tweet.getText());
            if (info.getUsername().toUpperCase().equals(screenName.toUpperCase())) {
                info.setMine(true);
            }
            else if (info.getText().toUpperCase().contains("@" + screenName.toUpperCase())) {
                info.setMentions(true);
            }
            info.setUrlStrings(Util.searchUrls(info.getText()));
            data.addInfo(info);
        }
    }

    public class SampleStatusListener extends StatusAdapter {

        @Override
        public void onStatus(Status status) {
            super.onStatus(status);
            if (isStarted) {
                data.addInfo(status2Info(status, screenName));
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
