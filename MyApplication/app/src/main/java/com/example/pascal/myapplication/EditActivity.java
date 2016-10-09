package com.example.pascal.myapplication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

/**
 * Created by pascal on 02.10.16.
 */
public class EditActivity extends AppCompatActivity {

    private Song currentSong;
    private MenuItem autoScrollPlayPauseMenuItem;
    private Menu optionsMenu;
    AutoScroller autoScroller;


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

        final EditText editText = (EditText) findViewById(R.id.editText);
        autoScroller = new AutoScroller(editText) {
            public void stopAutoScroll() {
                super.stopAutoScroll();
                System.out.println("auto stop");
                updateAutoScrollStartPauseMenuItem(false);
            }
        };
        editText.setText(currentSong.getPattern());
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
    }

    public boolean onOptionsItemSelected(MenuItem item){
        returnToMain();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        autoScrollPlayPauseMenuItem = menu.findItem(R.id.AutoScrollPlayPause);
        optionsMenu = menu;

        // Edit Stuff
        menu.findItem(R.id.transposeUp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ChordPatternEdit cpe = (ChordPatternEdit) findViewById(R.id.editText);
                cpe.transpose(1);
                return true;
            }
        });
        menu.findItem(R.id.transposeDown).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ChordPatternEdit cpe = (ChordPatternEdit) findViewById(R.id.editText);
                cpe.transpose(-1);
                return true;
            }
        });
        menu.findItem(R.id.importPattern).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                importPattern();
                return true;
            }
        });
        menu.findItem(R.id.eliminate_empty_lines).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ChordPatternEdit cpe = (ChordPatternEdit) findViewById(R.id.editText);
                final int n = cpe.eliminateEmptyLines();
                final String text = getResources().getQuantityString(R.plurals.numberOfRemovedLines, n, n);
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        menu.findItem(R.id.add_empty_lines_before_chords).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
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
        menu.findItem(R.id.makeEditable).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                setReadOnly(false);
                return true;
            }
        });
        menu.findItem(R.id.makeReadOnly).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                setReadOnly(true);
                return true;
            }
        });

        // View stuff
        menu.findItem(R.id.AutoScrollPlayPause).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (autoScroller.isPlaying()) {
                    autoScroller.stopAutoScroll();
                } else {
                    autoScroller.startAutoScroll();
                }
                updateAutoScrollStartPauseMenuItem(autoScroller.isPlaying());
                return true;
            }
        });
        menu.findItem(R.id.AutoScrollSpeed).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Dialog dialog = new Dialog(EditActivity .this);
                Rect displayRectangle = new Rect();
                Window window = EditActivity.this.getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
                LayoutInflater inflater = (LayoutInflater)
                        EditActivity.this.getSystemService(
                                getApplicationContext().LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.autoscroll_set_speed_dialog, null);
                view.setMinimumWidth((int)(displayRectangle.width() * 0.9f));
                dialog.setContentView(view);
                dialog.setTitle("Set Auto Scroll Speed");
                SeekBar slider = (SeekBar) dialog.findViewById(R.id.seekBar);
                double percent = getScrollRatePercent(currentSong.getScrollRate());
                slider.setProgress((int) (percent * slider.getMax()));
                slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        final double scrollRate = getExpScrollRate(((double) progress) / seekBar.getMax());
                        setScrollRate(scrollRate);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) { }
                });
                dialog.show();

                return true;
            }
        });

        setReadOnly(true);
        return true;
    }

    void updateAutoScrollStartPauseMenuItem(boolean play) {
        if (play) {
            autoScrollPlayPauseMenuItem.setTitle(R.string.pause_auto_scroll);
            autoScrollPlayPauseMenuItem.setIcon(android.R.drawable.ic_media_pause);
        } else {
            autoScrollPlayPauseMenuItem.setTitle(R.string.start_auto_scroll);
            autoScrollPlayPauseMenuItem.setIcon(android.R.drawable.ic_media_play);
        }
    }

    private static final double maxRate = 5.0;
    private static final double minRate = 0.0;
    private static final double factor = 3.0;
    private double getExpScrollRate(double rate) {
        rate *= factor;
        rate = Math.exp(rate);
        rate -= Math.exp(0.0);
        rate /= (Math.exp(factor) - Math.exp(0.0));
        rate *= (maxRate - minRate);
        rate += minRate;
        return rate;
    }
    private double getScrollRatePercent(double rate) {
        rate -= minRate;
        rate /= (maxRate - minRate);
        rate *= (Math.exp(factor) - Math.exp(0.0));
        rate += Math.exp(0.0);
        rate = Math.log(rate);
        rate /= factor;
        return rate;
    }

    private void setScrollRate(double rate) {
        currentSong.setScrollRate(rate);
        autoScroller.setAutoScrollRate(rate);
    }

    public void setReadOnly(boolean ro) {
        optionsMenu.findItem(R.id.makeEditable).setVisible(ro);
        optionsMenu.findItem(R.id.AutoScrollPlayPause).setVisible(ro);
        optionsMenu.findItem(R.id.AutoScrollSpeed).setVisible(ro);
        optionsMenu.findItem(R.id.makeReadOnly).setVisible(!ro);
        optionsMenu.findItem(R.id.transposeDown).setVisible(!ro);
        optionsMenu.findItem(R.id.transposeUp).setVisible(!ro);
        optionsMenu.findItem(R.id.importPattern).setVisible(!ro);
        optionsMenu.findItem(R.id.eliminate_empty_lines).setVisible(!ro);
        optionsMenu.findItem(R.id.add_empty_lines_before_chords).setVisible(!ro);


        final ChordPatternEdit editText = (ChordPatternEdit) findViewById(R.id.editText);
        editText.setFocusableInTouchMode(!ro);
        editText.setFocusable(!ro);
        autoScroller.stopAutoScroll();

        onPrepareOptionsMenu(optionsMenu);
    }

    private void importPattern() {
        Intent intent = new Intent(EditActivity.this, ImportActivity.class);
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
