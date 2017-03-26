package com.example.pascal.canlight;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;

public class ChooseColorActivity extends AppCompatActivity {

    String mSongName;

    private final static Integer[] COLORS = {
            Color.RED,
            Color.BLUE,
            Color.YELLOW,
            Color.GRAY,
            Color.GREEN,
            Color.TRANSPARENT,
            Color.CYAN,
            Color.MAGENTA,
            Color.parseColor("#FF8800")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_color);
        mSongName = getIntent().getStringExtra("songName");

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mSongName);

        ListView lv = (ListView) findViewById(R.id.chooseColorListView);
        lv.setAdapter(new ArrayAdapter<Integer>(this, R.layout.color_list_layout, R.id.color_list_textview, COLORS) {
            @NonNull
            @Override
            public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent).findViewById(R.id.color_list_textview);
                Integer color = getItem(position);
                view.setBackgroundColor(color == null ? 0 : color);
                view.setText("");
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("color", COLORS[position]);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                });
                return view;
            }
        });
        lv.setSelection(0);
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        setResult(RESULT_CANCELED, resultIntent);
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }
}
