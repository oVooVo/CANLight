package com.example.pascal.canlight;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by pascal on 02.10.16.
 */
public class EditActivity extends AppCompatActivity {

    private Song currentSong;
    private MenuItem autoScrollPlayPauseMenuItem;
    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final boolean keepScreenOn = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("pref_keep_screen_on", false);
        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        currentSong = getIntent().getParcelableExtra("song");

        final ChordPatternEdit editText = (ChordPatternEdit) findViewById(R.id.editText);

        editText.setText(currentSong.getPattern());
        editText.setTextSize((float) currentSong.getPatternTextSize());
        editText.setFocusable(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(currentSong.getName());

        if (currentSong.getPattern() == null) {
            importPattern();
        }
        setScrollRate(currentSong.getScrollRate());
        if (currentSong.getPatternIsUninitialized()) {
            importPattern();
        }

        ((AutoScrollView) findViewById(R.id.autoScrollView))
                .setOnAutoScrollStoppedListener(new AutoScrollView.OnAutoScrollStoppedListener() {
                    @Override
                    public void onAutoScrollStopped() {
                        updateAutoScrollStartPauseMenuItem(false);
                    }
                });
    }

    public boolean onOptionsItemSelected(MenuItem item){
        returnToMain();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        autoScrollPlayPauseMenuItem = menu.findItem(R.id.menu_auto_scroll_start_pause);
        optionsMenu = menu;

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
                }.setValue(currentSong.getScrollRate());
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
                }.setValue(currentSong.getPatternTextSize());
                return true;
            }
        });
        menu.findItem(R.id.menu_toogle_player_visibility).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                /*
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.playerLayout);
                if (layout.getVisibility() == View.INVISIBLE) {
                    layout.setVisibility(View.VISIBLE);
                } else {
                    layout.setVisibility(View.INVISIBLE);
                }
                */
                return true;
            }
        });


        setReadOnly(true);
        return true;
    }

    private void setTextSize(double s) {
        ChordPatternEdit cpe = (ChordPatternEdit) findViewById(R.id.editText);
        currentSong.setPatternTextSize(s);
        cpe.setTextSize((float) s);
    }

    void updateAutoScrollStartPauseMenuItem(boolean play) {
        if (play) {
            autoScrollPlayPauseMenuItem.setTitle(R.string.menu_auto_scroll_pause_title);
            autoScrollPlayPauseMenuItem.setIcon(android.R.drawable.ic_media_pause);
        } else {
            autoScrollPlayPauseMenuItem.setTitle(R.string.menu_auto_scroll_start_title);
            autoScrollPlayPauseMenuItem.setIcon(android.R.drawable.ic_media_play);
        }
    }

    private void setScrollRate(double rate) {
        currentSong.setScrollRate(rate);
        AutoScrollView autoScroller = (AutoScrollView) findViewById(R.id.autoScrollView);
        autoScroller.setScrollRate(rate);
    }

    public void setReadOnly(boolean ro) {
        AutoScrollView autoScroller = (AutoScrollView) findViewById(R.id.autoScrollView);
        optionsMenu.findItem(R.id.menu_edit_pattern).setVisible(ro);
        optionsMenu.findItem(R.id.menu_auto_scroll_start_pause).setVisible(ro);
        optionsMenu.findItem(R.id.menu_auto_scroll_speed).setVisible(ro);
        optionsMenu.findItem(R.id.menu_scale_pattern).setVisible(ro);
        optionsMenu.findItem(R.id.menu_toogle_player_visibility).setVisible(ro);
        optionsMenu.findItem(R.id.menu_view_pattern).setVisible(!ro);
        optionsMenu.findItem(R.id.menu_transpose_up).setVisible(!ro);
        optionsMenu.findItem(R.id.menu_transpose_down).setVisible(!ro);
        optionsMenu.findItem(R.id.menu_import_pattern).setVisible(!ro);
        optionsMenu.findItem(R.id.menu_eliminate_empty_lines).setVisible(!ro);
        optionsMenu.findItem(R.id.menu_add_empty_lines_before_chords).setVisible(!ro);

        final ChordPatternEdit editText = (ChordPatternEdit) findViewById(R.id.editText);
        editText.setIsEditable(!ro);
        autoScroller.endAutoScroll();
        if (ro) {
            editText.setTextSize((float) currentSong.getPatternTextSize());
        } else {
            editText.setTextSize(18);
        }

        onPrepareOptionsMenu(optionsMenu);
    }

    private void importPattern() {
        Intent intent = new Intent(EditActivity.this, ImportPatternActivity.class);
        intent.putExtra("name", currentSong.getName());
        EditActivity.this.startActivityForResult(intent, MainActivity.IMPORT_PATTERN_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.IMPORT_PATTERN_REQUEST) {
            if (resultCode == RESULT_OK) {
                final String pattern = data.getExtras().getString("pattern");
                ChordPatternEdit cpe = (ChordPatternEdit) findViewById(R.id.editText);
                cpe.setText(pattern);
                currentSong.setPattern(pattern);
            }
        }
    }

    @Override
    public void onBackPressed() {
        returnToMain();
    }

    private void returnToMain() {
        final EditText editText = (EditText) findViewById(R.id.editText);
        Intent resultIntent = new Intent();
        currentSong.setPattern(editText.getText().toString());
        resultIntent.putExtra("song", currentSong);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

}
