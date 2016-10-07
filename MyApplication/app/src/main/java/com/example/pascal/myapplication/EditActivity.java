package com.example.pascal.myapplication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
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

        final EditText editText = (EditText) findViewById(R.id.editText);
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
        menu.findItem(R.id.AutoScrollPause).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ChordPatternEdit cpe = (ChordPatternEdit) findViewById(R.id.editText);
                cpe.stopAutoScroll();
                return true;
            }
        });
        menu.findItem(R.id.AutoScrollStart).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ChordPatternEdit cpe = (ChordPatternEdit) findViewById(R.id.editText);
                cpe.startAutoScroll();
                return true;
            }
        });
        menu.findItem(R.id.AutoScrollSpeed).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final String title = getString(R.string.auto_scroll_speed_slider_dialog_title);
                new SliderDialog.ExpSliderDialog(title,
                        0, 5, 3,
                        EditActivity.this) {
                    @Override
                    void onValueChanged(double value) {
                        setScrollRate(value);
                    }
                }.setValue(currentSong.getScrollRate());
                return true;
            }
        });
        menu.findItem(R.id.ScaleText).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final String title = getString(R.string.scale_text);
                new SliderDialog.ExpSliderDialog(title,
                        2, 30, 3,
                        EditActivity.this) {

                    @Override
                    void onValueChanged(double value) {
                        setTextSize(value);
                    }
                }.setValue(currentSong.getPatternTextSize());
                return true;
            }
        });

        setReadOnly(true);
        return true;
    }

    private void setScrollRate(double rate) {
        ChordPatternEdit cpe = (ChordPatternEdit) findViewById(R.id.editText);
        currentSong.setScrollRate(rate);
        cpe.setAutoScrollRate(rate);
    }

    private void setTextSize(double s) {
        ChordPatternEdit cpe = (ChordPatternEdit) findViewById(R.id.editText);
        currentSong.setPatternTextSize(s);
        cpe.setTextSize((float) s);
    }

    public void setReadOnly(boolean ro) {
        optionsMenu.findItem(R.id.makeEditable).setVisible(ro);
        optionsMenu.findItem(R.id.AutoScrollStart).setVisible(ro);
        optionsMenu.findItem(R.id.AutoScrollPause).setVisible(ro);
        optionsMenu.findItem(R.id.AutoScrollSpeed).setVisible(ro);
        optionsMenu.findItem(R.id.ScaleText).setVisible(ro);
        optionsMenu.findItem(R.id.makeReadOnly).setVisible(!ro);
        optionsMenu.findItem(R.id.transposeDown).setVisible(!ro);
        optionsMenu.findItem(R.id.transposeUp).setVisible(!ro);
        optionsMenu.findItem(R.id.importPattern).setVisible(!ro);
        optionsMenu.findItem(R.id.eliminate_empty_lines).setVisible(!ro);
        optionsMenu.findItem(R.id.add_empty_lines_before_chords).setVisible(!ro);

        final ChordPatternEdit editText = (ChordPatternEdit) findViewById(R.id.editText);
        editText.setFocusableInTouchMode(!ro);
        editText.setFocusable(!ro);
        editText.stopAutoScroll();

        if (ro) {
            setTextSize(currentSong.getPatternTextSize());
        } else {
            editText.setTextSize((float) Song.DEFAULT_PATTERN_TEXT_SIZE);
        }

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
