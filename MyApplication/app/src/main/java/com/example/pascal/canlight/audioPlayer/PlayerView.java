package com.example.pascal.canlight.audioPlayer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.pascal.canlight.R;

/**
 * Created by pascal on 20.10.16.
 */
public class PlayerView extends Fragment {
    private static final String TAG = "PlayerView";

    private boolean mSeekBarUpdatesEnabled = true;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.player_layout, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        final TextView songLabel = (TextView) view.findViewById(R.id.songNameLabel);
        songLabel.setSelected(true);

        final Button playPauseButton = (Button) view.findViewById(R.id.playPauseButton);
        playPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);
    }

    public void setPlayer(final Player player) {
        final Activity activity = getActivity();
        final Button playPauseButton = (Button) activity.findViewById(R.id.playPauseButton);
        final TextView seekLastPositionButton = (TextView) activity.findViewById(R.id.gotoButton);
        final SeekBar seekBar = (SeekBar) activity.findViewById(R.id.playerSeekBar);
        final TextView songLabel = (TextView) activity.findViewById(R.id.songNameLabel);
        final TextView remainigLabel = (TextView) activity.findViewById(R.id.label_remaining_timeLabel);
        final TextView elapsedLabel = (TextView) activity.findViewById(R.id.label_elapsed_timeLabel);

        player.setOnCurrentPositionChangeListener(new Player.OnCurrentPositionChangeListener() {
            @Override
            public void onCurrentPositionChange(long pos) {
                if (mSeekBarUpdatesEnabled) {
                    seekBar.setProgress((int) pos);
                }
            }
        });
        player.setOnPlayStateChangeListener( new Player.OnPlayStateChangeListener() {
            @Override
            public void onPlayStateChange(boolean isPlaying) {
                if (isPlaying) {
                    playPauseButton.setBackgroundResource(android.R.drawable.ic_media_pause);
                } else {
                    playPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);
                }
            }
        });
        player.setOnSongLabelChangeListener( new Player.OnSongChangeListener() {
            @Override
            public void onSongChange(String label, long duration) {
                seekBar.setMax((int) duration);
                songLabel.setText(label);
            }
        });
        player.setOnTimeLabelsTextChangeListener( new Player.OnTimeLabelsTextChangeListener() {
            @Override
            public void onTimeLabelsTextChange(String elapsed, String remaining) {
                elapsedLabel.setText(elapsed);
                remainigLabel.setText(remaining);
            }
        });

        playPauseButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "toggle play pause");
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
                mSeekBarUpdatesEnabled = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player. seek(seekBar.getProgress());
                mSeekBarUpdatesEnabled = true;
            }
        });
    }
}
