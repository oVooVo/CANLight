package com.example.pascal.canlight.audioPlayer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.pascal.canlight.R;

/**
 * Created by pascal on 20.10.16.
 */
public class PlayerView extends LinearLayout {
    private static final String TAG = "PlayerView";

    private boolean mSeekBarUpdatesEnabled = true;

    public PlayerView(Context context) {
        super(context);
        init();

    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.player_layout, this, true);
        final TextView songLabel = (TextView) findViewById(R.id.songNameLabel);
        songLabel.setSelected(true);

        final Button playPauseButton = (Button) findViewById(R.id.playPauseButton);
        playPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);
    }

    public void setPlayer(final Player player) {
        final Button playPauseButton = (Button) findViewById(R.id.playPauseButton);
        final TextView seekLastPositionButton = (TextView) findViewById(R.id.gotoButton);
        final SeekBar seekBar = (SeekBar) findViewById(R.id.playerSeekBar);
        final TextView songLabel = (TextView) findViewById(R.id.songNameLabel);
        final TextView remainigLabel = (TextView) findViewById(R.id.label_remaining_timeLabel);
        final TextView elapsedLabel = (TextView) findViewById(R.id.label_elapsed_timeLabel);

        player.setOnCurrentPositionChangeListener(new Player.OnCurrentPositionChangeListener() {
            @Override
            public void onCurrentPositionChange(long pos) {
                if (mSeekBarUpdatesEnabled) {
                    ((SeekBar) findViewById(R.id.playerSeekBar)).setProgress((int) pos);
                }
            }
        });
        player.setOnPlayStateChangeListener( new Player.OnPlayStateChangeListener() {
            @Override
            public void onPlayStateChange(boolean isPlaying) {
                if (isPlaying) {
                    playPauseButton.setBackgroundResource(android.R.drawable.ic_media_play);
                } else {
                    playPauseButton.setBackgroundResource(android.R.drawable.ic_media_pause);
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

        playPauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "toggle play pause");
                player.togglePlayPause();
            }
        });
        seekLastPositionButton.setOnClickListener(new OnClickListener() {
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
                player.seek(seekBar.getProgress());
                mSeekBarUpdatesEnabled = true;
            }
        });



    }
}
