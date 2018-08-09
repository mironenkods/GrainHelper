package com.edfman.grainhelper;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.view.View;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;

import java.io.Console;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    boolean login_successful;
    String lName;
    String lpass;
    private HTTPWorking.request_answer request;
    DatabaseHandler db;

    TextView currentDateTime;

    Calendar dateAndTime = Calendar.getInstance();

    private static final String TABLE_CARS = "cars";
    private static final String TABLE_DRIVERS = "drivers";
    private static final String TABLE_FIELDS = "fields";
    private static final String TABLE_WAREHOUSES = "warehouses";
    private static final String TABLE_CROPS = "crops";
    private static final String TABLE_DOCS = "docs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        //security adding
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        currentDateTime = (TextView) findViewById(R.id.currentDateTime);
        setInitialDateTime();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Intent intent = getIntent();

        login_successful = intent.getBooleanExtra("login_suc", false);
        lName = intent.getStringExtra("login");
        lpass = intent.getStringExtra("password");

        //create DB
        db = new DatabaseHandler(this);

        //need to fill db from web-service
        if (login_successful) {
            renewRefs();
            //get docs
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                String deviceId = telephonyManager.getDeviceId();
                System.out.print("EMAI" + deviceId);
            }

        }
    }

    // установка начальных даты и времени
    private void setInitialDateTime() {

        currentDateTime.setText(DateUtils.formatDateTime(this,
                dateAndTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }

    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setInitialDateTime();
        }
    };

    // отображаем диалоговое окно для выбора даты
    public void setDate(View v) {
        new DatePickerDialog(MainActivity.this, d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    protected void renewRefs(){
            request = new HTTPWorking().callWebService("http://192.168.225.5/agro_ves/hs/Grain/ref/GetRef?login=" + lName + "&password=" + lpass + "", "GET");
            if (request.code == 200) {
                Gson gson = new Gson();
                Result result = gson.fromJson(request.Body, Result.class);
                for (TypicalRef ref : result.result_list) {
                    if (ref.type == RefType.car) db.addRef(ref, TABLE_CARS);
                    if (ref.type == RefType.crop) db.addRef(ref, TABLE_CROPS);
                    if (ref.type == RefType.driver) db.addRef(ref, TABLE_DRIVERS);
                    if (ref.type == RefType.field) db.addRef(ref, TABLE_FIELDS);
                    if (ref.type == RefType.warehouse) db.addRef(ref, TABLE_WAREHOUSES);
                }
            }
    }

    protected void getDocs(String date, String deviceId){
        request = new HTTPWorking().callWebService("http://192.168.225.5/agro_ves/hs/Grain/main/get?date=" + date + "&emei=" + deviceId + "", "PUT");
        if (request.code == 200) {
            Gson gson = new Gson();
            DocsResult result = gson.fromJson(request.Body, DocsResult.class);
            for (Document doc : result.result_list) {
                db.addDocument(doc);
            }
        }
    }

}

class DatabaseHandler extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION       = 1;
    private static final String DATABASE_NAME       = "grain_helper";
    private static final String TABLE_CARS          = "cars";
    private static final String TABLE_DRIVERS       = "drivers";
    private static final String TABLE_FIELDS        = "fields";
    private static final String TABLE_WAREHOUSES    = "warehouses";
    private static final String TABLE_CROPS         = "crops";
    private static final String TABLE_DOCS          = "docs";
    private static final String FIELD_ID            = "id";
    private static final String FIELD_TITLE         = "title";
    private static final String FIELD_DRIVER_ID     = "driver_id";
    private static final String FIELD_DATE          = "date";
    private static final String FIELD_CAR_ID        = "car_id";
    private static final String FIELD_CROP_ID       = "crop_id";
    private static final String FIELD_FIELD_ID      = "field_id";
    private static final String FIELD_WAREHOUSE_ID  = "warehouse_id";
    private static final String FIELD_CREATED_BY    = "created_by";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "create table if not exists " + TABLE_CARS   + "(" + FIELD_ID + " TEXT PRIMARY KEY, " + FIELD_TITLE + " TEXT, " + FIELD_DRIVER_ID + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_DRIVERS       + "(" + FIELD_ID + " TEXT PRIMARY KEY, " + FIELD_TITLE + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_FIELDS        + "(" + FIELD_ID + " TEXT PRIMARY KEY, " + FIELD_TITLE + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_WAREHOUSES    + "(" + FIELD_ID + " TEXT PRIMARY KEY, " + FIELD_TITLE + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_CROPS         + "(" + FIELD_ID + " TEXT PRIMARY KEY, " + FIELD_TITLE + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_DOCS          + "(" + FIELD_ID + " TEXT PRIMARY KEY, "
                                                                                    + FIELD_TITLE + " TEXT,"
                                                                                    + FIELD_DATE + " LONG, "
                                                                                    + FIELD_CAR_ID + " TEXT, "
                                                                                    + FIELD_DRIVER_ID + " TEXT, "
                                                                                    + FIELD_CROP_ID + " TEXT, "
                                                                                    + FIELD_FIELD_ID + " TEXT, "
                                                                                    + FIELD_WAREHOUSE_ID + " TEXT, "
                                                                                    + FIELD_CREATED_BY + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addDocument(Document document) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FIELD_ID, document.getCar_id());
        values.put(FIELD_TITLE, document.getTitle());
        values.put(FIELD_DATE, document.getDate());
        values.put(FIELD_CAR_ID, document.getCar_id());
        values.put(FIELD_DRIVER_ID, document.getCar_id());
        values.put(FIELD_CROP_ID, document.getCar_id());
        values.put(FIELD_FIELD_ID, document.getCar_id());
        values.put(FIELD_WAREHOUSE_ID, document.getCar_id());
        values.put(FIELD_CREATED_BY, document.getCar_id());

        db.insert(TABLE_DOCS, null, values);
        db.close();
    }

    public void addRef(TypicalRef ref, String table_name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FIELD_ID, ref.getId());
        values.put(FIELD_TITLE, ref.getTitle());
        if(ref.getType() == RefType.car) values.put(FIELD_DRIVER_ID, ref.getDriver_id());

        db.insert(table_name, null, values);
        db.close();
    }

    public Document getDoc(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_DOCS, new String[] {
                FIELD_ID,
                FIELD_TITLE,
                FIELD_DRIVER_ID,
                FIELD_DATE,
                FIELD_CAR_ID,
                FIELD_CROP_ID,
                FIELD_FIELD_ID,
                FIELD_WAREHOUSE_ID,
                FIELD_CREATED_BY}, FIELD_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null){
            cursor.moveToFirst();
        }
        //int id, String title, int driver_id, long date, int car_id, int crop_id, int field_id, int warehouse_id, String created_by
        Document document = new Document(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getInt(2),
                cursor.getLong(3),
                cursor.getInt(4),
                cursor.getInt(5),
                cursor.getInt(6),
                cursor.getInt(7),
                cursor.getString(8));

        return document;
    }

    public List<Document> getAllDocs() {
        List<Document> docsList = new ArrayList<Document>();
        String selectQuery = "SELECT  * FROM " + TABLE_DOCS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Document doc = new Document(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getLong(3),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getString(8));
                docsList.add(doc);
            } while (cursor.moveToNext());
        }

        return docsList;
    }

    public List<Document> getDocsByDate(String start_date, String finish_date) {
        List<Document> docsList = new ArrayList<Document>();
        String selectQuery = "SELECT  * FROM " + TABLE_DOCS
                + " WHERE " + FIELD_DATE + " >= ? AND "
                + FIELD_DATE + " <= ?";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{start_date, finish_date});

        if (cursor.moveToFirst()) {
            do {
                Document doc = new Document(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getLong(3),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getString(8));
                docsList.add(doc);
            } while (cursor.moveToNext());
        }

        return docsList;
    }

}

class Document {

    int id;
    String title;
    int driver_id;
    long date;
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

    public void setDate(long date) {
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

    public long getDate() {
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

    public Document(int id, String title, int driver_id, long date, int car_id, int crop_id, int field_id, int warehouse_id, String created_by) {
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
    public Document(int id, String title, long date) {
        this.id = id;
        this.title = title;
        this.date = date;
    }

    public Document() {
    }
}

enum RefType{car, driver, field, warehouse, crop}

class TypicalRef{
    String id;
    String title;
    String driver_id;
    RefType type;

    public TypicalRef(String id, String title, String driver_id, RefType type) {
        this.id = id;
        this.title = title;
        this.driver_id = driver_id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public RefType getType() {
        return type;
    }

    public void setType(RefType type) {
        this.type = type;
    }
}

class Result{
    List<TypicalRef> result_list;
}

class DocsResult{
    List<Document> result_list;
}