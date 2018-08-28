package com.edfman.grainhelper;

import android.Manifest;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class SyncActivity extends ListActivity {

    // имена атрибутов для Map
    final String ATTRIBUTE_NAME_id = "_id";
    final String ATTRIBUTE_NAME_car = "car";
    final String ATTRIBUTE_NAME_driver = "date";

    long currentDate;
    DatabaseHandler db;
    SimpleCursorAdapter userAdapter;
    String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        deviceId = getIntent().getStringExtra("deviceId");
        //create DB
        db = new DatabaseHandler(this);

        // вывод данных в List
        ListView userList = getListView();
        String[] headers = new String[] {ATTRIBUTE_NAME_car, ATTRIBUTE_NAME_driver, ATTRIBUTE_NAME_id};
        Cursor userCursor = db.getMessages_Cursor();
        userAdapter = new SimpleCursorAdapter(this, R.layout.item,
                userCursor, headers, new int[]{R.id.tvCar, R.id.tvDriver, R.id.tvInvoiceId}, 0);
        userList.setAdapter(userAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.sendAllMessages(deviceId);
            }
        });
    }
}
