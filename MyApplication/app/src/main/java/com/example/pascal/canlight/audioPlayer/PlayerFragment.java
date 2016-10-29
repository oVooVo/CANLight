
package com.example.pascal.canlight.audioPlayer;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.pascal.canlight.R;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import junit.framework.AssertionFailedError;

//TODO crash: close app during youtube play

/**
 * Created by pascal on 20.10.16.
 */
public class PlayerFragment extends Fragment {
    private static final String TAG = "PlayerFragment";
    private Player mPlayer;
    private static boolean mYouTubePlayerIsShown = true;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.player_layout, container, false);
        final TextView songLabel = (TextView) view.findViewById(R.id.songNameLabel);
        songLabel.setSelected(true);

        final Button playPauseButton = (Button) view.findViewById(R.id.playPauseButton);
        playPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);

        songLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mYouTubePlayerIsShown = !mYouTubePlayerIsShown;
                updateView();
            }
        });

        final Button fullscreenButton = (Button) view.findViewById(R.id.fullscreenButton);
        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((YouTubePlayer) mPlayer).setFullscreen(true);
            }
        });

        return view;
    }

    public void onResume() {
        super.onResume();
        updateView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // do not save the state.
    }

    public void setPlayer(final Player player) {
        mPlayer = player;
        final Activity activity = getActivity();
        final Button playPauseButton = (Button) activity.findViewById(R.id.playPauseButton);
        final TextView seekLastPositionButton = (TextView) activity.findViewById(R.id.gotoButton);
        final SeekBar seekBar = (SeekBar) activity.findViewById(R.id.playerSeekBar);
        final TextView songLabel = (TextView) activity.findViewById(R.id.songNameLabel);
        final TextView remainigLabel = (TextView) activity.findViewById(R.id.label_remaining_timeLabel);
        final TextView elapsedLabel = (TextView) activity.findViewById(R.id.label_elapsed_timeLabel);

        player.setOnPlayStateChangeListener(new Player.OnPlayStateChangeListener() {
            @Override
            public void onPlayStateChange(boolean isPlaying) {
                if (isPlaying) {
                    playPauseButton.setBackgroundResource(android.R.drawable.ic_media_pause);
                } else {
                    playPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);
                }
            }
        });
        player.setOnSongLabelChangeListener(new Player.OnSongChangeListener() {
            @Override
            public void onSongChange(String label, long duration) {
                seekBar.setMax((int) duration);
                songLabel.setText(label);
            }
        });
        player.setOnCurrentPositionChangeListener(new Player.OnCurrentPositionChangeListener() {
            @Override
            public void onCurrentPositionChange(long pos, String elapsed, String remaining) {
                seekBar.setProgress((int) pos);
                elapsedLabel.setText(elapsed);
                remainigLabel.setText(remaining);
            }
        });

        playPauseButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.togglePlayPause();
            }
        });
        seekLastPositionButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.seekLastPosition();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.seek(seekBar.getProgress());
            }
        });
        updateView();
    }

    private void updateView() {

        final YouTubePlayerSupportFragment playerSupportFragment = getYouTubePlayerSupportFragment();
        assert getView() != null;
        final Button fullscreenButton = (Button) getView().findViewById(R.id.fullscreenButton);
        final boolean showYouTubePlayer;

        if (mPlayer == null) {
            showYouTubePlayer = false;
        } else if (mPlayer instanceof YouTubePlayer) {
            showYouTubePlayer = true;
        } else if (mPlayer instanceof SpotifyPlayer) {
            showYouTubePlayer = false;
        } else {
            throw new AssertionFailedError();
        }
        if (showYouTubePlayer) {
            getFragmentManager().beginTransaction().show(playerSupportFragment).commitAllowingStateLoss();
            fullscreenButton.setVisibility(View.VISIBLE);
        } else {
            getFragmentManager().beginTransaction().hide(playerSupportFragment).commitAllowingStateLoss();
            fullscreenButton.setVisibility(View.GONE);
        }

        if (getView() != null) {
            updatePlayerSize(getActivity().getWindow().getDecorView().getWidth());
        }
    }

    private void updatePlayerSize(int width) {
        YouTubePlayerSupportFragment playerSupportFragment = getYouTubePlayerSupportFragment();
        if (playerSupportFragment.getView() != null) {
            playerSupportFragment.getView().setLayoutParams(new LinearLayout.LayoutParams(width, 350));
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final int widthDp = newConfig.screenWidthDp;
        Resources r = getResources();
        float widthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthDp, r.getDisplayMetrics());
        updatePlayerSize((int) widthPx);
    }

    public YouTubePlayerSupportFragment getYouTubePlayerSupportFragment() {
        return (YouTubePlayerSupportFragment) getChildFragmentManager().findFragmentById(R.id.youtube_fragment);
    }

}