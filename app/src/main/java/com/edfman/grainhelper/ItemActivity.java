package com.edfman.grainhelper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ItemActivity extends AppCompatActivity {

    private static final String TABLE_CARS          = "cars";
    private static final String TABLE_DRIVERS       = "drivers";
    private static final String TABLE_FIELDS        = "fields";
    private static final String TABLE_WAREHOUSES    = "warehouses";
    private static final String TABLE_CROPS         = "crops";

    DatabaseHandler db;

    Document new_doc = new Document();

    ArrayList<TypicalRef> arrayListFields, arrayListCrops, arrayListWare;

    String lName, deviceId;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        db = new DatabaseHandler(this);

        Intent intent = getIntent();
        Boolean readOnly = intent.getBooleanExtra("readOnly", true);
        deviceId = intent.getStringExtra("deviceId");
        lName = intent.getStringExtra("login");
        if(readOnly) {
            TextView temp = (TextView) findViewById(R.id.tvId);
            temp.setText(String.valueOf(intent.getIntExtra("id", 0)));
            temp = (TextView) findViewById(R.id.tvDate);
            temp.setText(intent.getStringExtra("date"));
            temp = (TextView) findViewById(R.id.tvCreatedBy);
            temp.setText(intent.getStringExtra("created_by"));
            temp = (TextView) findViewById(R.id.tvcar);
            temp.setText(intent.getStringExtra("car"));
            temp = (TextView) findViewById(R.id.tvDriver);
            temp.setText(intent.getStringExtra("driver"));
            EditText tempEd = (EditText) findViewById(R.id.tvCrop);
            tempEd.setText(intent.getStringExtra("crop"));
//            tempEd.setEnabled(false);
            tempEd.setFocusable(false);
            tempEd.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
            tempEd.setClickable(false);
            tempEd.setBackgroundColor(Color.TRANSPARENT);
            tempEd = findViewById(R.id.tvField);
            tempEd.setText(intent.getStringExtra("field"));
//            tempEd.setEnabled(false);
            tempEd.setFocusable(false);
            tempEd.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
            tempEd.setClickable(false);
            tempEd.setBackgroundColor(Color.TRANSPARENT);
            tempEd = findViewById(R.id.tvWarehouse);
            tempEd.setText(intent.getStringExtra("warehouse"));
//            tempEd.setEnabled(false);
            tempEd.setFocusable(false);
            tempEd.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
            tempEd.setClickable(false);
            tempEd.setBackgroundColor(Color.TRANSPARENT);
            FloatingActionButton floatingActionButton = findViewById(R.id.fab);
            floatingActionButton.setVisibility(View.INVISIBLE);
            findViewById(R.id.imageButtonBarCode).setVisibility(View.GONE);
        } else {
            TextView temp = (TextView) findViewById(R.id.tvId);
            temp.setVisibility(View.GONE);
            temp = (TextView) findViewById(R.id.tvIdLabel);
            temp.setVisibility(View.GONE);
            temp = (TextView) findViewById(R.id.tvDate);
            temp.setVisibility(View.GONE);
            temp = (TextView) findViewById(R.id.tvDateLabel);
            temp.setVisibility(View.GONE);
            temp = (TextView) findViewById(R.id.tvCreatedBy);
            temp.setVisibility(View.GONE);
            temp = (TextView) findViewById(R.id.tvCreatedLabel);
            temp.setVisibility(View.GONE);
            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.tvField);
            arrayListFields = db.getRefElementsArrayList(TABLE_FIELDS);
            ArrayAdapter<String> adapter =  new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, TypicalRef.getTitlesArray(arrayListFields));
            autoCompleteTextView.setAdapter(adapter);
            autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    new_doc.setField_id(arrayListFields.get((int)id).id_1c);
                }
            });
            autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.tvCrop);
            arrayListCrops = db.getRefElementsArrayList(TABLE_CROPS);
            adapter =  new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, TypicalRef.getTitlesArray(arrayListCrops));
            autoCompleteTextView.setAdapter(adapter);
            autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    new_doc.setCrop_id(arrayListCrops.get((int)id).id_1c);
                }
            });
            autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.tvWarehouse);
            arrayListWare = db.getRefElementsArrayList(TABLE_WAREHOUSES);
            adapter =  new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, TypicalRef.getTitlesArray(arrayListWare));
            autoCompleteTextView.setAdapter(adapter);
            autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    new_doc.setWarehouse_id(arrayListWare.get((int)id).id_1c);
                }
            });

        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new_doc.setDate(System.currentTimeMillis()/1000);
                new_doc.setCreated_by(lName);
                db.addDocument(new_doc, deviceId);
                finish();
            }
        });

        ImageButton imgButton = (ImageButton) findViewById(R.id.imageButtonBarCode);
        imgButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "ONE_D_MODE");
                try {
                    startActivityForResult(intent, 0);
                } catch (Exception e) {
                    System.out.print("Need to install zxing");
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.zxing.client.android&hl=en")));
                }
            }



            public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                if (requestCode == 0) {
                    if (resultCode == RESULT_OK) {
                        String contents = intent.getStringExtra("SCAN_RESULT");
                        String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                        if (format == "Code128") {
                            //parse srting

                            String[] massResult = contents.split("/");
                            if (massResult.length == 4) {
                                TextView vtextView = (TextView) findViewById(R.id.tvcar);
                                vtextView.setText(db.getRefById(TABLE_CARS, massResult[1]).title);
                                new_doc.setCar_id(massResult[1]);
                                vtextView = (TextView) findViewById(R.id.tvDriver);
                                String s = db.getRefById(TABLE_DRIVERS, massResult[3]).title;
                                vtextView.setText(s);
                                new_doc.setdriver_id(s);
                            }

                        }


                    }
                }

            }
        });
    }

}
