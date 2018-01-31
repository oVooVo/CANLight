package com.example.pascal.canlight.audioPlayer;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.example.pascal.canlight.R;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

/**
 * Created by pascal on 20.10.16.
 */
public class YouTubePlayer extends Player implements
        com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener,
        com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener {
    private static final String TAG = "YouTubePlayer";
    private int mLastSeekPosition;
    private final YouTubePlayerSupportFragment mPlayerFragment;
    private com.google.android.youtube.player.YouTubePlayer mPlayer;
    private final Activity mActivity;
    private String mVideoId;
    private String mLabel;
    private boolean mIsInFullscreenMode = false;

    public YouTubePlayer(Activity activity, YouTubePlayerSupportFragment playerFragment) {
        super(activity);
        mActivity = activity;
        mPlayerFragment = playerFragment;
        Log.i(TAG, "initialize player ...");
        mPlayerFragment.initialize(activity.getString(R.string.google_developer_key),
            new com.google.android.youtube.player.YouTubePlayer.OnInitializedListener() {
                    @Override
                    public void onInitializationSuccess(com.google.android.youtube.player.YouTubePlayer.Provider provider,
                                                        com.google.android.youtube.player.YouTubePlayer youTubePlayer,
                                                        boolean b) {
                        Log.i(TAG, "successfully initialized youtube player");
                        mPlayer = youTubePlayer;
                        mPlayer.setPlayerStyle(com.google.android.youtube.player.YouTubePlayer.PlayerStyle.CHROMELESS);
                        mPlayer.setPlayerStateChangeListener(YouTubePlayer.this);
                        mPlayer.setPlaybackEventListener(YouTubePlayer.this);
                        mPlayer.loadVideo(mVideoId, 0);
                        mPlayer.setFullscreenControlFlags(0);
                        mPlayer.setOnFullscreenListener(isInFullscreenMode -> {
                            if (isInFullscreenMode) {
                                mPlayer.setPlayerStyle(com.google.android.youtube.player.YouTubePlayer.PlayerStyle.DEFAULT);
                            } else {
                                mPlayer.setPlayerStyle(com.google.android.youtube.player.YouTubePlayer.PlayerStyle.CHROMELESS);
                            }
                            mIsInFullscreenMode = isInFullscreenMode;
                        });
                    }

                    @Override
                    public void onInitializationFailure(com.google.android.youtube.player.YouTubePlayer.Provider provider,
                                                        YouTubeInitializationResult youTubeInitializationResult) {
                        Log.w(TAG, "youtube player initialization error.");
                    }
            });
    }

    @Override
    public void pause() {
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    @Override
    public void togglePlayPause() {
        Log.i(TAG, "toggle");
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                Log.i(TAG, "toggle(pause)");
            } else {
                mPlayer.play();
                Log.i(TAG, "toggle(play)");
            }
        }
    }

    @Override
    public void seekLastPosition() {
        if (mPlayer != null) {
            mPlayer.seekToMillis(mLastSeekPosition);
        }
    }

    @Override
    public void seek(long pos) {
        if (mPlayer != null) {
            mLastSeekPosition = (int) pos;
            mPlayer.seekToMillis(mLastSeekPosition);
        }
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    @Override
    public void init(String id, long position) {
        super.init();
        mVideoId = id;
        if (mPlayer != null) {
            mPlayer.loadVideo(id, (int) position);
        }
        // else mPlayer.loadVideo is called once player is initialized (using mVideoId)
    }

    @Override
    public void deinit() {
        mPlayer = null;
        super.deinit();
    }

    @Override
    public void onLoading() {
    }

    @Override
    public void onLoaded(String s) {
        updateSong(mLabel);
    }

    @Override
    public void onAdStarted() {
    }

    @Override
    public void onVideoStarted() {
    }

    @Override
    public void onVideoEnded() {
    }

    @Override
    public void onError(com.google.android.youtube.player.YouTubePlayer.ErrorReason errorReason) {
        Toast.makeText(mActivity, "YouTube Error: " + errorReason, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPlaying() {
        updatePlayState(true);
    }

    @Override
    public void onPaused() {
        updatePlayState(false);
    }

    @Override
    public void onStopped() {
        updatePlayState(false);
    }

    @Override
    public void onBuffering(boolean b) {
    }

    @Override
    public void onSeekTo(int i) {
        updateCurrentPosition(i);
    }

    public int getCurrentPosition() {
        if (mPlayer != null) {
            return mPlayer.getCurrentTimeMillis();
        } else {
            return -1;
        }
    }

    @Override
    public int getDuration() {
        if (mPlayer != null) {
            return mPlayer.getDurationMillis();
        } else {
            return 0;
        }
    }

    public void setFullscreen(boolean f) {
        mPlayer.setFullscreen(f);
    }

    public boolean isFullscreen() {
        return mIsInFullscreenMode;
    }
}
