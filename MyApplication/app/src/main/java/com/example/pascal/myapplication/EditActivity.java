package com.example.pascal.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

/**
 * Created by pascal on 02.10.16.
 */
public class EditActivity extends AppCompatActivity {

    public static final int IMPORT_PATTERN_REQUEST = 1;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        final String pattern = getIntent().getStringExtra("pattern");
        name = getIntent().getStringExtra("name");
        final EditText editText = (EditText) findViewById(R.id.editText);
        editText.setText(pattern);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(name);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        returnToMain();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);

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

        return true;
    }

    private void importPattern() {
        Intent intent = new Intent(EditActivity.this, ImportActivity.class);
        intent.putExtra("name", name);
        EditActivity.this.startActivityForResult(intent, IMPORT_PATTERN_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMPORT_PATTERN_REQUEST) {
            if (resultCode == RESULT_OK) {
                final String pattern = data.getExtras().getString("pattern");
                ChordPatternEdit cpe = (ChordPatternEdit) findViewById(R.id.editText);
                cpe.setText(pattern);
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
        resultIntent.putExtra("pattern", editText.getText().toString());
        setResult(RESULT_OK, resultIntent);
        finish();
    }

}
