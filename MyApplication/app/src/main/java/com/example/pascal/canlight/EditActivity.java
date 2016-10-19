package com.example.pascal.canlight;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pascal on 02.10.16.
 */
public class EditActivity extends AppCompatActivity
        implements ConnectionStateCallback, Player.NotificationCallback {

    private Song mCurrentSong;
    private MenuItem mAutoScrollPlayPauseMenuItem;
    private Menu mOptionsMenu;

    static private Player mPlayer;
    private boolean mUpdateSeekBar = true;
    private static final String CLIENT_ID = "8874e81dddd441fb8854482e4aafc634";
    private static final String REDIRECT_URI = "canlight-spotify://callback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final boolean keepScreenOn = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("pref_keep_screen_on", false);
        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        mCurrentSong = getIntent().getParcelableExtra("song");

        final ChordPatternEdit editText = (ChordPatternEdit) findViewById(R.id.editText);

        editText.setText(mCurrentSong.getPattern());
        editText.setTextSize((float) mCurrentSong.getPatternTextSize());
        editText.setFocusable(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mCurrentSong.getName());

        if (mCurrentSong.getPattern() == null) {
            importPattern();
        }
        setScrollRate(mCurrentSong.getScrollRate());
        if (mCurrentSong.getPatternIsUninitialized()) {
            importPattern();
        }

        ((AutoScrollView) findViewById(R.id.autoScrollView))
                .setOnAutoScrollStoppedListener(new AutoScrollView.OnAutoScrollStoppedListener() {
                    @Override
                    public void onAutoScrollStopped() {
                        updateAutoScrollStartPauseMenuItem(false);
                    }
                });

        if (mCurrentSong.getSpotifyTrackId() == null || mCurrentSong.getSpotifyTrackId().isEmpty()) {
            initializeTrackId(mCurrentSong);
        }

        findViewById(R.id.playerPlayPauseButton).setOnClickListener(new View.OnClickListener() {
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

        final SeekBar playerSeekBar = (SeekBar) findViewById(R.id.playerSeekBar);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mPlayer != null && mPlayer.getPlaybackState() != null) {
                    final long ms = (int) mPlayer.getPlaybackState().positionMs;

                    // int is enough. 2147483647 milliseconds is about 24 days.
                    if (mUpdateSeekBar) {
                        playerSeekBar.setProgress((int) ms);
                    }
                }
                if (mPlayer != null && mPlayer.getMetadata() != null && mPlayer.getMetadata().currentTrack != null) {
                    playerSeekBar.setMax((int) mPlayer.getMetadata().currentTrack.durationMs);
                }
            }
        }, 0, 20);

        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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

        if (mPlayer != null) {
            // if mPlayer is null, it will be initialized onLoggedIn().
            initPlayer();
        } else {
            // the layout will be enabled onLoggedIn()
            setPlayerEnabled(false);
        }
        setPlayPauseButtonIcon(true);
    }

    void initPlayer() {
        final SeekBar playerSeekBar = (SeekBar) findViewById(R.id.playerSeekBar);
        mPlayer.playUri(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                mPlayer.pause(null);
            }

            @Override
            public void onError(Error error) {
                playerSeekBar.setMax(0);
            }
        }, "spotify:track:" + mCurrentSong.getSpotifyTrackId(), 0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.pause(null);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        returnToMain();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        mAutoScrollPlayPauseMenuItem = menu.findItem(R.id.menu_auto_scroll_start_pause);
        mOptionsMenu = menu;

        // Edit Stuff
        menu.findItem(R.id.menu_transpose_up).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ChordPatternEdit cpe = (ChordPatternEdit) findViewById(R.id.editText);
                cpe.transpose(1);
                return true;
            }
        });
        menu.findItem(R.id.menu_transpose_down).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ChordPatternEdit cpe = (ChordPatternEdit) findViewById(R.id.editText);
                cpe.transpose(-1);
                return true;
            }
        });
        menu.findItem(R.id.menu_import_pattern).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                importPattern();
                return true;
            }
        });
        menu.findItem(R.id.menu_eliminate_empty_lines).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ChordPatternEdit cpe = (ChordPatternEdit) findViewById(R.id.editText);
                final int n = cpe.eliminateEmptyLines();
                final String text = getResources().getQuantityString(R.plurals.numberOfRemovedLines, n, n);
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        menu.findItem(R.id.menu_add_empty_lines_before_chords).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ChordPatternEdit cpe = (ChordPatternEdit) findViewById(R.id.editText);
                final int n = cpe.addEmptyLinesBeforeChords();
                final String text = getResources().getQuantityString(R.plurals.numberOfInsertedLines, n, n);
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                return true;
            }
        });


        // Switch Edit/view stuff
        menu.findItem(R.id.menu_edit_pattern).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                setReadOnly(false);
                return true;
            }
        });
        menu.findItem(R.id.menu_view_pattern).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                setReadOnly(true);
                return true;
            }
        });

        // View stuff
        menu.findItem(R.id.menu_auto_scroll_start_pause).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AutoScrollView autoScroller = (AutoScrollView) findViewById(R.id.autoScrollView);
                if (autoScroller.isActive()) {
                    autoScroller.endAutoScroll();
                } else {
                    autoScroller.startAutoScroll();
                }
                updateAutoScrollStartPauseMenuItem(autoScroller.isActive());
                return true;
            }
        });
        menu.findItem(R.id.menu_auto_scroll_speed).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new SliderDialog.ExpSliderDialog(0, 6, 1, EditActivity.this) {
                    @Override
                    void onValueChanged(double value) {
                        setScrollRate(value);
                    }
                }.setValue(mCurrentSong.getScrollRate());
                return true;
            }
        });
        menu.findItem(R.id.menu_scale_pattern).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new SliderDialog.ExpSliderDialog(2, 30, 3, EditActivity.this) {
                    @Override
                    void onValueChanged(double value) {
                        setTextSize(value);
                    }
                }.setValue(mCurrentSong.getPatternTextSize());
                return true;
            }
        });
        menu.findItem(R.id.menu_toogle_player_visibility).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.playerLayout);
                setPlayerVisibility(layout.getVisibility() != View.VISIBLE);
                return true;
            }
        });
        menu.findItem(R.id.menu_config_player).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new AlertDialog(EditActivity.this) {
                    {
                        final SpotifySpinner editName = new SpotifySpinner(getContext());
                        editName.setMaxLines(1);
                        editName.setText(mCurrentSong.getSpotifyTrackDisplayName());
                        editName.selectAll();
                        editName.requestFocus();
                        setView(editName);
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                        setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.rename_dialog_cancel), new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing.
                            }
                        });
                        final AlertDialog d = this;
                        editName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                setSpotifyTrack(editName.getId(position), editName.getDisplayName(position));
                                if (mPlayer != null) {
                                    initPlayer();
                                }
                                d.cancel();
                            }
                        });
                    }
                }.show();
                return true;
            }
        });

        setReadOnly(true);
        setPlayerVisibility(false);
        return true;
    }

    private void setSpotifyTrack(String id, String displayName) {
        mCurrentSong.setSpotifyTrack(id, displayName);
    }

    private void setPlayerVisibility(boolean isVisible) {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.playerLayout);
        ViewGroup.LayoutParams params = layout.getLayoutParams();

        layout.setLayoutParams(params);
        if (isVisible) {
            layout.setVisibility(View.VISIBLE);
            mOptionsMenu.findItem(R.id.menu_config_player).setVisible(true);
            params.height = 140;
            spotifyConnectRequest();
        } else {
            layout.setVisibility(View.INVISIBLE);
            mOptionsMenu.findItem(R.id.menu_config_player).setVisible(false);
            params.height = 0;
        }
        layout.setLayoutParams(params);
    }

    private void setTextSize(double s) {
        ChordPatternEdit cpe = (ChordPatternEdit) findViewById(R.id.editText);
        mCurrentSong.setPatternTextSize(s);
        cpe.setTextSize((float) s);
    }

    void updateAutoScrollStartPauseMenuItem(boolean play) {
        if (play) {
            mAutoScrollPlayPauseMenuItem.setTitle(R.string.menu_auto_scroll_pause_title);
            mAutoScrollPlayPauseMenuItem.setIcon(android.R.drawable.ic_media_pause);
        } else {
            mAutoScrollPlayPauseMenuItem.setTitle(R.string.menu_auto_scroll_start_title);
            mAutoScrollPlayPauseMenuItem.setIcon(android.R.drawable.ic_media_play);
        }
    }

    private void setScrollRate(double rate) {
        mCurrentSong.setScrollRate(rate);
        AutoScrollView autoScroller = (AutoScrollView) findViewById(R.id.autoScrollView);
        autoScroller.setScrollRate(rate);
    }

    public void setReadOnly(boolean ro) {
        AutoScrollView autoScroller = (AutoScrollView) findViewById(R.id.autoScrollView);
        mOptionsMenu.findItem(R.id.menu_edit_pattern).setVisible(ro);
        mOptionsMenu.findItem(R.id.menu_auto_scroll_start_pause).setVisible(ro);
        mOptionsMenu.findItem(R.id.menu_auto_scroll_speed).setVisible(ro);
        mOptionsMenu.findItem(R.id.menu_scale_pattern).setVisible(ro);
        mOptionsMenu.findItem(R.id.menu_toogle_player_visibility).setVisible(ro);
        mOptionsMenu.findItem(R.id.menu_view_pattern).setVisible(!ro);
        mOptionsMenu.findItem(R.id.menu_transpose_up).setVisible(!ro);
        mOptionsMenu.findItem(R.id.menu_transpose_down).setVisible(!ro);
        mOptionsMenu.findItem(R.id.menu_import_pattern).setVisible(!ro);
        mOptionsMenu.findItem(R.id.menu_eliminate_empty_lines).setVisible(!ro);
        mOptionsMenu.findItem(R.id.menu_add_empty_lines_before_chords).setVisible(!ro);

        final ChordPatternEdit editText = (ChordPatternEdit) findViewById(R.id.editText);
        editText.setIsEditable(!ro);
        autoScroller.endAutoScroll();
        if (ro) {
            editText.setTextSize((float) mCurrentSong.getPatternTextSize());
        } else {
            editText.setTextSize(18);
        }

        onPrepareOptionsMenu(mOptionsMenu);
    }

    private void importPattern() {
        Intent intent = new Intent(EditActivity.this, ImportPatternActivity.class);
        intent.putExtra("name", mCurrentSong.getName());
        EditActivity.this.startActivityForResult(intent, MainActivity.IMPORT_PATTERN_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MainActivity.IMPORT_PATTERN_REQUEST:
                if (resultCode == RESULT_OK) {
                    final String pattern = data.getExtras().getString("pattern");
                    ChordPatternEdit cpe = (ChordPatternEdit) findViewById(R.id.editText);
                    cpe.setText(pattern);
                    mCurrentSong.setPattern(pattern);
                }
                break;
            case MainActivity.LOGIN_SPOTIFY_REQUEST:
                AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
                if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                    Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                    Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                        @Override
                        public void onInitialized(SpotifyPlayer spotifyPlayer) {
                            mPlayer = spotifyPlayer;
                            mPlayer.addConnectionStateCallback(EditActivity.this);
                            mPlayer.addNotificationCallback(EditActivity.this);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                        }
                    });
                }
            break;
        }
    }

    private void setPlayerEnabled(boolean isEnabled) {
        findViewById(R.id.playerSeekBar).setEnabled(isEnabled);
        findViewById(R.id.playerPlayPauseButton).setEnabled(isEnabled);
    }

    @Override
    public void onBackPressed() {
        returnToMain();
    }

    private void spotifyConnectRequest() {
        if (mPlayer == null) {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                    AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
            builder.setScopes(new String[]{"user-read-private", "streaming"});
            AuthenticationRequest request = builder.build();

            AuthenticationClient.openLoginActivity(this, MainActivity.LOGIN_SPOTIFY_REQUEST, request);
        }
    }

    private void returnToMain() {
        final EditText editText = (EditText) findViewById(R.id.editText);
        Intent resultIntent = new Intent();
        mCurrentSong.setPattern(editText.getText().toString());
        resultIntent.putExtra("song", mCurrentSong);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void initializeTrackId(Song song) {
        SpotifySpinner.findTrack(song);
    }

    @Override
    public void onLoggedIn() {
        Toast.makeText(this, R.string.spotify_logged_in, Toast.LENGTH_SHORT).show();
        initPlayer();
        setPlayerEnabled(true);
    }

    @Override
    public void onLoggedOut() {
        Toast.makeText(this, R.string.spotify_logged_out, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginFailed(int i) {
        Toast.makeText(this, R.string.spotify_login_fail, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {
        Toast.makeText(this, getString(R.string.spotify_connection_message) + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        if (PlayerEvent.kSpPlaybackNotifyPause.equals(playerEvent)) {
            setPlayPauseButtonIcon(true);
        } else if (PlayerEvent.kSpPlaybackNotifyPlay.equals(playerEvent)) {
            setPlayPauseButtonIcon(false);
        }
    }

    private void setPlayPauseButtonIcon(boolean playIcon) {
        Button playPauseButton = (Button) findViewById(R.id.playerPlayPauseButton);
        if (playIcon) {
            playPauseButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play, 0, 0, 0);
        } else {
            playPauseButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_pause, 0, 0, 0);
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Toast.makeText(this, getString(R.string.spotify_playback_error) + error, Toast.LENGTH_SHORT).show();
    }
}
