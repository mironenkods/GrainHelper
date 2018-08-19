package com.edfman.grainhelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        Boolean readOnly = intent.getBooleanExtra("readOnly", true);
        if(readOnly) {
            TextView temp = (TextView) findViewById(R.id.tvId);
            temp.setText(intent.getStringExtra("id"));
            EditText tempEd = (EditText) findViewById(R.id.tvCrop);
            tempEd.setText(intent.getStringExtra("crop"));
            tempEd.setEnabled(false);
            tempEd = findViewById(R.id.tvDriver);
            tempEd.setText(intent.getStringExtra("driver"));
            tempEd.setEnabled(false);
            tempEd = findViewById(R.id.tvField);
            tempEd.setText(intent.getStringExtra("field"));
            tempEd.setEnabled(false);
            tempEd = findViewById(R.id.tvWarehouse);
            tempEd.setText(intent.getStringExtra("warehouse"));
            tempEd.setEnabled(false);
            FloatingActionButton floatingActionButton = findViewById(R.id.fab);
            floatingActionButton.setVisibility(View.INVISIBLE);
        } else {
            TextView temp = (TextView) findViewById(R.id.tvId);
            temp.setVisibility(View.INVISIBLE);
            temp = (TextView) findViewById(R.id.tvDate);
            temp.setVisibility(View.INVISIBLE);
            temp = (TextView) findViewById(R.id.tvCreatedBy);
            temp.setVisibility(View.INVISIBLE);
        }
    }

}
