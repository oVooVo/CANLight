package com.example.pascal.canlight;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player.NotificationCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pascal on 19.10.16.
 */
public class Player
        implements ConnectionStateCallback, NotificationCallback {
    private Activity mActivity;
    private Button mPlayPauseButton;
    private SeekBar mSeekBar;
    private boolean mUpdateSeekBar = true;
    private String mId;

    private static com.spotify.sdk.android.player.Player mPlayer;

    public static final String CLIENT_ID = "8874e81dddd441fb8854482e4aafc634";
    private static final String REDIRECT_URI = "canlight-spotify://callback";

    Player(Activity activity, Button playPauseButton, SeekBar seekBar) {
        mActivity = activity;
        setPlayPauseButton(playPauseButton);
        setSeekBar(seekBar);

        setPlayerEnabled(false);
        setPlayPauseButtonIcon(true);

        if (mPlayer != null) {
            mPlayer.addConnectionStateCallback(this);
            mPlayer.addNotificationCallback(this);
        }
    }

    public void deinit() {
        mPlayer.removeConnectionStateCallback(this);
        mPlayer.removeNotificationCallback(this);
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
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mPlayer != null && mPlayer.getPlaybackState() != null) {
                    final long ms = (int) mPlayer.getPlaybackState().positionMs;

                    // int is enough. 2147483647 milliseconds is about 24 days.
                    if (mUpdateSeekBar && mPlayer.getPlaybackState().isPlaying) {
                        mSeekBar.setProgress((int) ms);
                    }
                }
                if (mPlayer != null && mPlayer.getMetadata() != null && mPlayer.getMetadata().currentTrack != null) {
                    mSeekBar.setMax((int) mPlayer.getMetadata().currentTrack.durationMs);
                }
            }
        }, 0, 20);

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
                if (mPlayer != null) {
                    mPlayer.seekToPosition(null, seekBar.getProgress());
                }
                mUpdateSeekBar = true;
            }
        });
    }

    public void init(String id) {
        mId = id;
        if (mPlayer != null) {
            setPlayerEnabled(true);
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
