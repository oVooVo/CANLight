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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        final String pattern = getIntent().getStringExtra("pattern");
        final EditText editText = (EditText) findViewById(R.id.editText);
        editText.setText(pattern);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        returnToMain();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
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
