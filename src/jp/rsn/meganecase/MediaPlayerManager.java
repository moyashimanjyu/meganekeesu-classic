package jp.rsn.meganecase;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;

public class MediaPlayerManager {

    public static final int VOICE_NORMAL = 0;
    public static final int VOICE_MOE = 1;
    public static final int VOICE_KOREDEMO = 2;

    private static final int[] rawids0 = { R.raw.v1_start, R.raw.v1_reply, R.raw.v1_dm,
            R.raw.v1_fav, R.raw.v1_rt };
    private static final int[] rawids1 = { R.raw.v2_start, R.raw.v2_reply, R.raw.v2_dm,
            R.raw.v2_fav, R.raw.v2_rt };
    private static final int[] rawids2 = { 0, R.raw.voice, R.raw.voice, R.raw.voice, R.raw.voice };

    private MediaPlayer[] mps = new MediaPlayer[5];

    public MediaPlayerManager(Context context, int type) {
        if (type == VOICE_NORMAL) {
            for (int i = 0; i < mps.length; i++) {
                mps[i] = MediaPlayer.create(context, rawids0[i]);
            }
        }
        else if (type == VOICE_MOE) {
            for (int i = 0; i < mps.length; i++) {
                mps[i] = MediaPlayer.create(context, rawids1[i]);
            }
        }
        else if (type == VOICE_KOREDEMO) {
            for (int i = 0; i < mps.length; i++) {
                if (rawids2[i] != 0) {
                    mps[i] = MediaPlayer.create(context, rawids2[i]);
                }
            }
        }
    }

    public void release() {
        for (MediaPlayer mp : mps) {
            if (mp != null) {
                mp.release();
            }
        }
    }

    public void voiceStart() {
        if (mps[0] != null) {
            voice(mps[0]);
        }
    }

    public void voiceReply() {
        if (mps[1] != null) {
            voice(mps[1]);
        }
    }

    public void voiceDm() {
        if (mps[2] != null) {
            voice(mps[2]);
        }
    }

    public void voiceFav() {
        if (mps[3] != null) {
            voice(mps[3]);
        }
    }

    public void voiceRt() {
        if (mps[4] != null) {
            voice(mps[4]);
        }
    }

    private static final void voice(MediaPlayer mp) {
        if (mp.isPlaying()) {
            mp.stop();
            try {
                mp.prepare();
            }
            catch (IllegalStateException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        mp.start();
    }
}
