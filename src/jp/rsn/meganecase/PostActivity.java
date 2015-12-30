package jp.rsn.meganecase;

import java.io.IOException;
import java.io.InputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class PostActivity extends BaseActivity implements LocationListener, TaskErrorListener {

    private static final int REQUEST_GET_CONTENT = 1;
    private static final int REQUEST_PLUGIN = 2;
    private static final int TEXT_LEN = 140;

    private Long inReplyTo = null;
    private Uri uploadPic = null;
    private Bitmap uploadBitmap = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post);

        final TextView postLen = (TextView) findViewById(R.id.postLen);
        final EditText postText = (EditText) findViewById(R.id.postText);
        postText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (postText == null || postLen == null) {
                    return;
                }
                int len = TEXT_LEN - s.length();
                postLen.setText(Integer.toString(len));
                postLen.setTextColor(len < 0 ? Color.RED : Color.WHITE);
                postLen.invalidate();
            }
        });
        postText.requestFocus();
        if (getIntent().getData() != null) {
            trackEvent("from-intent");
            Uri uri = getIntent().getData();
            String text = uri.getQueryParameter("status");
            if (!Util.isEmpty(text)) {
                postText.setText(text.replace("+", " "));
            }
            else {
                text = uri.getQueryParameter("text");
                String url = uri.getQueryParameter("url");
                if (!Util.isEmpty(text)) {
                    if (!Util.isEmpty(url)) {
                        postText.setText(text + " " + url);
                    }
                    else {
                        postText.setText(text);
                    }
                }
            }
            String id = uri.getQueryParameter("in_reply_to_status_id");
            if (!Util.isEmpty(id)) {
                inReplyTo = Long.parseLong(id);
            }
            else {
                id = uri.getQueryParameter("in_reply_to");
                if (!Util.isEmpty(id)) {
                    inReplyTo = Long.parseLong(id);
                }
            }
            postText.setSelection(postText.getText().length());
        }
        else if (getIntent().getExtras() != null) {
            String postString = getIntent().getExtras().getString("POST_STRING");
            if (postString == null || postString.length() == 0) {
                postString = getIntent().getExtras().getString(Intent.EXTRA_TEXT);
            }
            postText.setText(postString);
            inReplyTo = getIntent().getExtras().getLong("POST_INREPRYTO");
            TextView inReplyText = (TextView) findViewById(R.id.inReplyText);
            inReplyText.setText(getIntent().getExtras().getString("POST_INREPRYTEXT"));
            if (inReplyText.getText() != null && inReplyText.getText().length() > 0) {
                inReplyText.setVisibility(View.VISIBLE);
            }
            if (getIntent().getExtras().getBoolean("POST_REPLY")) {
                postText.setSelection(postText.getText().length());
            }
        }
        postText.invalidate();
        postLen.invalidate();
    }

    @Override
    public void onDestroy() {
        if (uploadBitmap != null && !uploadBitmap.isRecycled()) {
            uploadBitmap.recycle();
        }
        super.onDestroy();
        inReplyTo = null;
        uploadPic = null;
        uploadBitmap = null;
        manager = null;
        latitude = null;
        longitude = null;
        provider = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (manager != null && provider != null) {
            manager.requestLocationUpdates(provider, 0, 0, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (manager != null) {
            manager.removeUpdates(this);
        }
    }

    public void clickTweet(View v) {
        EditText postText = (EditText) findViewById(R.id.postText);
        String tweet = postText.getText().toString();
        if (tweet.length() > TEXT_LEN) {
            return;
        }
        trackEvent("tweet");

        TweetTask task = new TweetTask(this, tweet);
        if (inReplyTo != null && inReplyTo > 0) {
            task.setInReplyTo(inReplyTo);
        }
        if (uploadPic != null) {
            task.setPicture(uploadPic);
            trackEvent("tweet-twitpic");
        }
        if (latitude != null && longitude != null) {
            task.setLatitude(latitude);
            task.setLongitude(longitude);
            trackEvent("tweet-location");
        }
        task.setOnErrorListener(this);
        task.start();

        setResult(RESULT_OK);
        finish();
    }

    public void clickTwitpic(View v) {
        trackEvent("twitpic");
        EditText postText = (EditText) findViewById(R.id.postText);
        String tweet = postText.getText().toString();
        if (tweet.length() > TEXT_LEN) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GET_CONTENT);
    }

    public void clickPlugin(View v) {
        trackEvent("plugin");
        Intent intent = new Intent();
        intent.setAction("jp.r246.twicca.ACTION_EDIT_TWEET");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        EditText postText = (EditText) findViewById(R.id.postText);
        String tweet = postText.getText().toString();
        intent.putExtra(Intent.EXTRA_TEXT, tweet);
        if (inReplyTo != null && inReplyTo > 0) {
            intent.putExtra("in_reply_to_status_id", Long.toString(inReplyTo));
        }
        intent.putExtra("user_input", tweet);
        intent.putExtra("cursor", postText.getSelectionStart());
        startActivityForResult(Intent.createChooser(intent, null), REQUEST_PLUGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        EditText postText = (EditText) findViewById(R.id.postText);
        Uri uri = null;

        switch (requestCode) {
        case REQUEST_GET_CONTENT:
            if (resultCode != RESULT_OK) {
                break;
            }
            uri = resultData.getData();
            if (uri == null) {
                break;
            }
            try {
                if (uploadBitmap != null && !uploadBitmap.isRecycled()) {
                    uploadBitmap.recycle();
                }

                InputStream in = getContentResolver().openInputStream(uri);
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in, null, opts);
                in.close();

                int maxHeight = (int) (getResources().getDisplayMetrics().density * 100);
                if (opts.outHeight > maxHeight) {
                    opts.inSampleSize = (int) Math.floor(opts.outHeight / maxHeight);
                }

                opts.inJustDecodeBounds = false;
                opts.inPurgeable = true;
                in = getContentResolver().openInputStream(uri);
                uploadBitmap = BitmapFactory.decodeStream(in, null, opts);
                ImageView picImage = (ImageView) findViewById(R.id.picImage);
                picImage.setImageBitmap(uploadBitmap);
                picImage.setVisibility(View.VISIBLE);
                in.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            uploadPic = uri;
            break;
        case REQUEST_PLUGIN:
            if (resultCode != RESULT_OK) {
                break;
            }
            postText.setText(resultData.getStringExtra(Intent.EXTRA_TEXT));
            inReplyTo = resultData.getLongExtra("in_reply_to_status_id", 0);
            postText.setSelection(resultData.getIntExtra("cursor", 0));
            break;
        default:
            break;
        }
    }

    public void onError(Exception e) {
        showError();
    }

    private LocationManager manager = null;
    private Double latitude = null;
    private Double longitude = null;
    private String provider = null;

    public void clickLocation(View v) {
        trackEvent("location");
        if (manager == null) {
            manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        }
        else if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        }
        else {
            return;
        }
        Log.v(App.TAG, "location provider is " + provider);
        manager.requestLocationUpdates(provider, 0, 0, this);
        TextView text = (TextView) findViewById(R.id.locationText);
        text.setVisibility(View.VISIBLE);
        text.setText("‘ªˆÊ’†...");
        text.setMovementMethod(null);
        text.invalidate();
    }

    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.v("Latitude", String.valueOf(latitude));
        Log.v("Longitude", String.valueOf(longitude));
        TextView text = (TextView) findViewById(R.id.locationText);
        text.setVisibility(View.VISIBLE);
        String latText = String.valueOf(latitude);
        String longText = String.valueOf(longitude);
        String geo = latText + "," + longText;
        Spannable spannable = Spannable.Factory.getInstance().newSpannable(geo);
        URLSpan urlSpan = new URLSpan("http://maps.google.co.jp/maps?ll=" + geo + "&q=" + geo);
        spannable.setSpan(urlSpan, 0, geo.length(), spannable.getSpanFlags(urlSpan));
        text.setText(spannable, TextView.BufferType.SPANNABLE);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        text.invalidate();
        manager.removeUpdates(this);
    }

    public void onProviderDisabled(String provider) {
        Log.v(App.TAG, "PROVIDER DISABLED");
    }

    public void onProviderEnabled(String provider) {
        Log.v(App.TAG, "PROVIDER ENABLED");
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
        case LocationProvider.OUT_OF_SERVICE:
            Log.v(App.TAG, "OUT OF SERVICE");
            break;
        case LocationProvider.TEMPORARILY_UNAVAILABLE:
            Log.v(App.TAG, "TEMPORARILY UNAVAILABLE");
            break;
        case LocationProvider.AVAILABLE:
            Log.v(App.TAG, "AVAILABLE");
            break;
        default:
            break;
        }
    }
}
