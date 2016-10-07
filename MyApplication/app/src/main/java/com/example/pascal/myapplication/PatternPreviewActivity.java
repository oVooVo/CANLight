package com.example.pascal.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class PatternPreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_preview);
        getSupportActionBar().setTitle(R.string.import_preview);

        final String pattern = getIntent().getStringExtra("pattern");
        final EditText editText = (EditText) findViewById(R.id.editTextPreview);
        editText.setText(pattern);
        editText.setFocusable(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.import_preview_menu, menu);

        menu.findItem(R.id.applyImport).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final EditText editText = (EditText) findViewById(R.id.editTextPreview);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("pattern", editText.getText().toString());
                setResult(RESULT_OK, resultIntent);
                finish();
                return true;
            }
        });
        menu.findItem(R.id.cancelImport).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent resultIntent = new Intent();
                setResult(RESULT_CANCELED, resultIntent);
                finish();
                return true;
            }
        });

        return true;
    }
}
