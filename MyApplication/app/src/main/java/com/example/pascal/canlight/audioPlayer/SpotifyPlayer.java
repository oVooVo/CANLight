package com.example.pascal.canlight.audioPlayer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.pascal.canlight.MainActivity;
import com.example.pascal.canlight.MySpotify;
import com.example.pascal.canlight.R;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.Player.NotificationCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlayerEvent;

/**
 * Created by pascal on 19.10.16.
 */
public class SpotifyPlayer extends Player
        implements ConnectionStateCallback, NotificationCallback {
    private Activity mActivity;
    private String mId;
    private int mLastSeekPosition = 0;
    private static final String TAG = "SpotifyPlayer";

    private static com.spotify.sdk.android.player.Player mPlayer;

    public SpotifyPlayer(Context context, Activity activity) {
        super(context);
        mActivity = activity;

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
        super.deinit();
    }

    public void togglePlayPause() {
        if (mPlayer != null) {
            if (mPlayer.getPlaybackState().isPlaying) {
                mPlayer.pause(null);
                Log.i(TAG, "toggle(pause)");
            } else {
                mPlayer.resume(null);
                Log.i(TAG, "toggle(play)");
            }
        }
    }
    public void seekLastPosition() {
        if (mPlayer != null) {
            mPlayer.seekToPosition(null, mLastSeekPosition);

            // in case player is paused.
            updateCurrentPosition(mLastSeekPosition);
        }
    }

    private void updateSong() {
        if (mPlayer != null
                && mPlayer.getMetadata() != null
                && mPlayer.getMetadata().currentTrack != null) {
            final Metadata.Track track = mPlayer.getMetadata().currentTrack;
            updateSong(track.name + " - " + track.artistName + " (" + track.albumName + ")");
        } else {
            updateSong(null);
        }
    }

    public void seek(long pos) {
        if (mPlayer != null) {
            mLastSeekPosition = (int) pos;
            mPlayer.seekToPosition(null, mLastSeekPosition);
        }
    }

    public void init(final String id, final long position) {
        super.init();
        mId = id;
        if (mPlayer != null) {
            mPlayer.playUri(new com.spotify.sdk.android.player.Player.OperationCallback() {
                @Override
                public void onSuccess() {
                    mPlayer.pause(null);
                    mPlayer.seekToPosition(null, (int) position); //todo apparently playUri has an argument for seek
                    Log.i(TAG, "Play: " + id);
                }

                @Override
                public void onError(com.spotify.sdk.android.player.Error error) {
                    Log.w(TAG, "error: " + error.toString());
                    updateSong(null);
                }
            }, "spotify:track:" + mId, 0, 0);
        } else {
            if (mPlayer == null) {
                MySpotify.spotifyConnectRequest(mActivity);
            }
        }
    }
    public void onInitialized(com.spotify.sdk.android.player.SpotifyPlayer player) {
        mPlayer = player;
        mPlayer.addConnectionStateCallback(this);
        mPlayer.addNotificationCallback(this);
    }

    @Override
    public void onLoggedIn() {
        Toast.makeText(mActivity, R.string.spotify_logged_in, Toast.LENGTH_SHORT).show();
        init(mId, 0);
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
            updatePlayState(false);
        } else if (PlayerEvent.kSpPlaybackNotifyPlay.equals(playerEvent)) {
            updatePlayState(true);
        } else if (PlayerEvent.kSpPlaybackNotifyMetadataChanged.equals(playerEvent)){
            if (mPlayer != null
                    && mPlayer.getMetadata() != null
                    && mPlayer.getPlaybackState() != null
                    && mPlayer.getMetadata().currentTrack != null) {
                updateCurrentPosition(mPlayer.getPlaybackState().positionMs);
            } else {
                updateCurrentPosition(0);
            }
            updateSong();
        } else {
            updateSong();
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

    public void pause() {
        if (mPlayer != null) {
            mPlayer.pause(null);
        }
    }

    public int getCurrentPosition() {
        if (mPlayer != null && mPlayer.getPlaybackState() != null) {
            // int is enough. 2147483647 milliseconds is about 24 days.
            if (mPlayer.getPlaybackState().isPlaying) {
                return (int) mPlayer.getPlaybackState().positionMs;
            }
        }
        return -1;
    }

    public int getDuration() {
        if (mPlayer != null
                && mPlayer.getMetadata() != null
                && mPlayer.getMetadata().currentTrack != null) {
            return (int) mPlayer.getMetadata().currentTrack.durationMs;
        } else {
            return 0;
        }
    }
}
