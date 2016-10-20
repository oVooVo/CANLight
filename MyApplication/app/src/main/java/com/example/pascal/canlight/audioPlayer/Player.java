package com.example.pascal.canlight.audioPlayer;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pascal.canlight.MainActivity;
import com.example.pascal.canlight.R;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.Player.NotificationCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.Locale;

/**
 * Created by pascal on 19.10.16.
 */
public class Player
        implements ConnectionStateCallback, NotificationCallback {
    private Activity mActivity;
    private Button mPlayPauseButton;
    private Button mGotoButton;
    private TextView mRemainingTimeLabel;
    private TextView mElapsedTimeLabel;
    private TextView mSongNameLabel;
    private SeekBar mSeekBar;
    private boolean mUpdateSeekBar = true;
    private String mId;
    private int mLastSeekPosition = 0;

    private Handler mHandler;
    private static com.spotify.sdk.android.player.Player mPlayer;

    public static final String CLIENT_ID = "8874e81dddd441fb8854482e4aafc634";
    private static final String REDIRECT_URI = "canlight-spotify://callback";

    public Player(Activity activity, Button playPauseButton, Button gotoButton, SeekBar seekBar,
           TextView remainingTimeLabel, TextView elapsedTimeLabel, TextView songNameLabel) {
        mActivity = activity;
        setPlayPauseButton(playPauseButton);
        setGotoButton(gotoButton);
        setSeekBar(seekBar);
        mRemainingTimeLabel = remainingTimeLabel;
        mElapsedTimeLabel = elapsedTimeLabel;
        mSongNameLabel = songNameLabel;
        updateTimeLabels(0);
        updateSongNameLabel();

        setPlayerEnabled(false);
        setPlayPauseButtonIcon(true);

        if (mPlayer != null) {
            mPlayer.addConnectionStateCallback(this);
            mPlayer.addNotificationCallback(this);
        }
    }

    public void deinit() {
        if (mPlayer != null) {
            mPlayer.removeConnectionStateCallback(this);
            mPlayer.removeNotificationCallback(this);
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    private void setGotoButton(Button button) {
        mGotoButton = button;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    mPlayer.seekToPosition(null, mLastSeekPosition);
                    mSeekBar.setProgress(mLastSeekPosition);
                    updateTimeLabels(mLastSeekPosition);
                }
            }
        });
    }

    private String formatTime(long ms) {
        boolean isNegative = false;
        if (ms < 0) {
            isNegative = true;
            ms = -ms;
        }
        long sec = ms / 1000;
        long min = sec / 60;
        sec %= 60;
        ms %= 1000;

        if (isNegative) {
            min = -min;
        }

        // ms / 100 is correct enough.
        return String.format(Locale.getDefault(), "%2d:%02d.%03d", min, sec, ms);
    }

    private void updateTimeLabels(long position) {
        long remaining = 0;
        if (mPlayer != null
                && mPlayer.getMetadata() != null
                && mPlayer.getMetadata().currentTrack != null) {
            long duration = mPlayer.getMetadata().currentTrack.durationMs;
            remaining = duration - position;
        }
        mElapsedTimeLabel.setText(formatTime(position));
        mRemainingTimeLabel.setText(formatTime(-remaining));
    }

    private void updateSongNameLabel() {
        String songName = "No Song";
        if (mPlayer != null
                && mPlayer.getMetadata() != null
                && mPlayer.getMetadata().currentTrack != null) {
            Metadata.Track track = mPlayer.getMetadata().currentTrack;
            songName = track.name + " - " + track.artistName + " (" + track.albumName + ")";
        }
        mSongNameLabel.setText(songName);
    }

    private void setPlayPauseButton(Button button) {
        mPlayPauseButton = button;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    if (mPlayer.getPlaybackState().isPlaying) {
                        mPlayer.pause(null);
                    } else {
                        mPlayer.resume(null);
                    }
                }
            }
        });
    }

    private void setSeekBar(SeekBar seekBar) {
        mSeekBar = seekBar;

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        } else {
            mHandler = new Handler();
        }

        new Runnable() {
            @Override
            public void run() {
                if (mPlayer != null && mPlayer.getPlaybackState() != null) {
                    final long ms = (int) mPlayer.getPlaybackState().positionMs;

                    // int is enough. 2147483647 milliseconds is about 24 days.
                    if (mUpdateSeekBar && mPlayer.getPlaybackState().isPlaying) {
                        mSeekBar.setProgress((int) ms);
                        updateTimeLabels(mPlayer.getPlaybackState().positionMs);
                    }
                }
                if (mPlayer != null && mPlayer.getMetadata() != null && mPlayer.getMetadata().currentTrack != null) {
                    mSeekBar.setMax((int) mPlayer.getMetadata().currentTrack.durationMs);
                }
                mHandler.postDelayed(this, 20);
            }
        }.run();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mUpdateSeekBar = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mLastSeekPosition = seekBar.getProgress();
                if (mPlayer != null) {
                    mPlayer.seekToPosition(null, mLastSeekPosition);
                }
                mUpdateSeekBar = true;
            }
        });
    }

    public void init(String id) {
        mId = id;
        if (mPlayer != null) {
            mPlayer.playUri(new com.spotify.sdk.android.player.Player.OperationCallback() {
                @Override
                public void onSuccess() {
                    mPlayer.pause(null);
                    mPlayer.seekToPosition(null, mSeekBar.getProgress());
                }

                @Override
                public void onError(com.spotify.sdk.android.player.Error error) {
                    mSeekBar.setMax(0);
                }
            }, "spotify:track:" + mId, 0, 0);
        } else {
            spotifyConnectRequest();
        }
    }

    private void spotifyConnectRequest() {
        if (mPlayer == null) {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                    AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
            builder.setScopes(new String[]{"user-read-private", "streaming"});
            AuthenticationRequest request = builder.build();

            AuthenticationClient.openLoginActivity(mActivity, MainActivity.LOGIN_SPOTIFY_REQUEST, request);
        }
    }

    public void onInitialized(SpotifyPlayer player) {
        mPlayer = player;
        mPlayer.addConnectionStateCallback(this);
        mPlayer.addNotificationCallback(this);
    }


    private void setPlayerEnabled(boolean isEnabled) {
        mSeekBar.setEnabled(isEnabled);
        mPlayPauseButton.setEnabled(isEnabled);
        mGotoButton.setEnabled(isEnabled);
        mRemainingTimeLabel.setEnabled(isEnabled);
        mElapsedTimeLabel.setEnabled(isEnabled);
        mSongNameLabel.setEnabled(isEnabled);
    }

    @Override
    public void onLoggedIn() {
        Toast.makeText(mActivity, R.string.spotify_logged_in, Toast.LENGTH_SHORT).show();
        init(mId);
    }

    @Override
    public void onLoggedOut() {

        Toast.makeText(mActivity, R.string.spotify_logged_out, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginFailed(int i) {
        Toast.makeText(mActivity, R.string.spotify_login_fail, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {
        Toast.makeText(mActivity, mActivity.getString(R.string.spotify_connection_message) + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        if (PlayerEvent.kSpPlaybackNotifyPause.equals(playerEvent)) {
            setPlayPauseButtonIcon(true);
        } else if (PlayerEvent.kSpPlaybackNotifyPlay.equals(playerEvent)) {
            setPlayPauseButtonIcon(false);
        } else if (PlayerEvent.kSpPlaybackNotifyMetadataChanged.equals(playerEvent)){
            if (mPlayer != null
                    && mPlayer.getMetadata() != null
                    && mPlayer.getPlaybackState() != null
                    && mPlayer.getMetadata().currentTrack != null) {
                setPlayerEnabled(true);
                updateTimeLabels(mPlayer.getPlaybackState().positionMs);
            } else {
                setPlayerEnabled(false);
                updateTimeLabels(0);
            }
            updateSongNameLabel();
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        if (Error.kSpErrorFailed.equals(error)) {
            Toast.makeText(mActivity, mActivity.getString(R.string.spotify_no_song_error), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mActivity, mActivity.getString(R.string.spotify_playback_error) + error, Toast.LENGTH_SHORT).show();
        }
    }

    private void setPlayPauseButtonIcon(boolean playIcon) {
        if (playIcon) {
            mPlayPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);
        } else {
            mPlayPauseButton.setBackgroundResource(android.R.drawable.ic_media_pause);
        }
    }

    public void pause() {
        if (mPlayer != null) {
            mPlayer.pause(null);
        }
    }
}
