package com.edfman.grainhelper;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.database.sqlite.SQLiteOpenHelper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean login_suc;
    private String login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Intent intent = getIntent();

        boolean login_successful = intent.getBooleanExtra("login_suc", false);
        String lName = intent.getStringExtra("login");

        //if login successful check DB


    }

}

public class DatabaseHandler extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "grain_helper";
    private static final String TABLE_CARS = "cars";
    private static final String TABLE_DRIVERS = "drivers";
    private static final String TABLE_FIELDS = "fields";
    private static final String TABLE_WAREHOUSES = "warehouses";
    private static final String TABLE_CROPS = "crops";
    private static final String TABLE_DOCS = "docs";
    private static final String FIELD_ID = "id";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_DRIVER_ID = "driver_id";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_CAR_ID = "car_id";
    private static final String FIELD_CROP_ID = "crop_id";
    private static final String FIELD_FIELD_ID = "field_id";
    private static final String FIELD_WAREHOUSE_ID = "warehouse_id";
    private static final String FIELD_CREATED_BY = "created_by";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "create table if not exists " + TABLE_CARS   + "(" + FIELD_ID + " INTEGER PRIMARY KEY, " + FIELD_TITLE + " TEXT, " + FIELD_DRIVER_ID + " INTEGER)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_DRIVERS       + "(" + FIELD_ID + " INTEGER PRIMARY KEY, " + FIELD_TITLE + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_FIELDS        + "(" + FIELD_ID + " INTEGER PRIMARY KEY, " + FIELD_TITLE + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_WAREHOUSES    + "(" + FIELD_ID + " INTEGER PRIMARY KEY, " + FIELD_TITLE + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_CROPS         + "(" + FIELD_ID + " INTEGER PRIMARY KEY, " + FIELD_TITLE + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_DOCS          + "(" + FIELD_ID + " INTEGER PRIMARY KEY, "
                                                                                    + FIELD_TITLE + " TEXT,"
                                                                                    + FIELD_DATE + " DATE, "
                                                                                    + FIELD_CAR_ID + " INTEGER, "
                                                                                    + FIELD_DRIVER_ID + " INTEGER, "
                                                                                    + FIELD_CROP_ID + " INTEGER, "
                                                                                    + FIELD_FIELD_ID + " INTEGER, "
                                                                                    + FIELD_WAREHOUSE_ID + " INTEGER, "
                                                                                    + FIELD_CREATED_BY + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    public void addDocument(Document document) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FIELD_ID, document.getCar_id());
        values.put(FIELD_TITLE, document.getTitle());
        values.put(FIELD_DATE, document.getDate().toString());
        values.put(FIELD_CAR_ID, document.getCar_id());
        values.put(FIELD_DRIVER_ID, document.getCar_id());
        values.put(FIELD_CROP_ID, document.getCar_id());
        values.put(FIELD_FIELD_ID, document.getCar_id());
        values.put(FIELD_WAREHOUSE_ID, document.getCar_id());
        values.put(FIELD_CREATED_BY, document.getCar_id());

        db.insert(TABLE_DOCS, null, values);
        db.close();
    }

    public Document getDoc(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_DOCS, new String[] { FIELD_ID,
                        FIELD_TITLE, FIELD_DATE}, FIELD_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null){
            cursor.moveToFirst();
        }

        Document document = new Document(Integer.parseInt(cursor.getString(0)), cursor.getString(1), Date.from(cursor.getString(2)));

        return document;
    }

    public List<Document> getAllDocs() {
        List<Document> docsList = new ArrayList<Document>();
        String selectQuery = "SELECT  * FROM " + TABLE_DOCS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Document doc = new Document();
                doc.setId(Integer.parseInt(cursor.getString(0)));
                doc.setTitle(cursor.getString(1));
                doc.setDriver_id(Integer.parseInt(cursor.getString(2)));
                doc.setDate(Date.from());
                docsList.add(doc);
            } while (cursor.moveToNext());
        }

        return contactList;
    }


}

class Document {

    int id;
    String title;
    int driver_id;
    Date date;
    int car_id;
    int crop_id;
    int field_id;
    int warehouse_id;
    String created_by;

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDriver_id(int driver_id) {
        this.driver_id = driver_id;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setCar_id(int car_id) {
        this.car_id = car_id;
    }

    public void setCrop_id(int crop_id) {
        this.crop_id = crop_id;
    }

    public void setField_id(int field_id) {
        this.field_id = field_id;
    }

    public void setWarehouse_id(int warehouse_id) {
        this.warehouse_id = warehouse_id;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getDriver_id() {
        return driver_id;
    }

    public Date getDate() {
        return date;
    }

    public int getCar_id() {
        return car_id;
    }

    public int getCrop_id() {
        return crop_id;
    }

    public int getField_id() {
        return field_id;
    }

    public int getWarehouse_id() {
        return warehouse_id;
    }

    public String getCreated_by() {
        return created_by;
    }

    public Document(int id, String title, int driver_id, Date date, int car_id, int crop_id, int field_id, int warehouse_id, String created_by) {
        this.id = id;
        this.title = title;
        this.driver_id = driver_id;
        this.date = date;
        this.car_id = car_id;
        this.crop_id = crop_id;
        this.field_id = field_id;
        this.warehouse_id = warehouse_id;
        this.created_by = created_by;
    }
    public Document(int id, String title, Date date) {
        this.id = id;
        this.title = title;
        this.date = date;
    }

    public Document() {
    }
}