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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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

    IntentIntegrator scanIntegrator;


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

        scanIntegrator = new IntentIntegrator(this);


        ImageButton imgButton = (ImageButton) findViewById(R.id.imageButtonBarCode);
        imgButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                scanIntegrator.initiateScan();
            }
        });

//        parseContent("1/000000026/000000027/BAH 939068");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanningResult != null) {
            //we have a result
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            //parse srting

               parseContent(scanContent);
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void parseContent(String scanContent) {
        String[] massResult = scanContent.split("/");
        if (massResult.length == 4) {
            TypicalRef car = db.getRefById(TABLE_CARS, massResult[1]);
            TextView vtextView = (TextView) findViewById(R.id.tvcar);
            if (car == null){
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.CarNotFound, Toast.LENGTH_SHORT);
                toast.show();
                vtextView.setText(massResult[1]);
            } else vtextView.setText(car.title);
            new_doc.setCar_id(massResult[1]);
            vtextView = (TextView) findViewById(R.id.tvDriver);
            TypicalRef driver = db.getRefById(TABLE_DRIVERS, massResult[3]);
            if (driver == null){
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.DriverNotFound, Toast.LENGTH_SHORT);
                toast.show();
                vtextView.setText(massResult[3]);
            } else vtextView.setText(driver.title);
            new_doc.setdriver_id(massResult[3]);
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.NotRigData, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
