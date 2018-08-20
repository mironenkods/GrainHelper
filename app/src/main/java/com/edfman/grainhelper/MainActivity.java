package com.edfman.grainhelper;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ListActivity;
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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;

import java.io.Console;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends ListActivity {

    boolean login_successful;
    String lName;
    String lpass;
    private HTTPWorking.request_answer request;
    DatabaseHandler db;
    private static final String server_Ip = "http://178.92.47.54:8085";

    TextView currentDateTime;
    long currentDate;

    Calendar dateAndTime = Calendar.getInstance();

    private static final String TABLE_CARS = "cars";
    private static final String TABLE_DRIVERS = "drivers";
    private static final String TABLE_FIELDS = "fields";
    private static final String TABLE_WAREHOUSES = "warehouses";
    private static final String TABLE_CROPS = "crops";
    private static final String TABLE_DOCS = "docs";

    // имена атрибутов для Map
    final String ATTRIBUTE_NAME_id = "_id";
    final String ATTRIBUTE_NAME_car = "car";
    final String ATTRIBUTE_NAME_driver = "driver";

    SimpleCursorAdapter userAdapter;

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent intent = new Intent(MainActivity.this, ItemActivity.class);
        Document doc = db.getStringDoc(position);

        intent.putExtra("readOnly", true);
        intent.putExtra("id", doc.getId());
        intent.putExtra("car", doc.getCar_id());
        intent.putExtra("created_by", doc.getCreated_by());
        intent.putExtra("crop", doc.getCrop_id());
        // or you already have long value of date, use this instead of milliseconds variable.
        intent.putExtra("date", convertLongDate(doc.getDate(), "dd/MM/yyyy"));
        intent.putExtra("driver", doc.getDriver_id());
        intent.putExtra("field", doc.getField_id());
        intent.putExtra("warehouse", doc.getWarehouse_id());

        startActivity(intent);
    }

    public static String convertLongDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

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
                Intent intent = new Intent(MainActivity.this, ItemActivity.class);

                intent.putExtra("readOnly", false);
                startActivity(intent);
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
            db.getRefElements(TABLE_CARS);
            //get docs
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                String deviceId = telephonyManager.getDeviceId();
                System.out.println("EMAI" + deviceId);
                db.clearDocs();
                getDocs(deviceId);
            }

        }
        // вывод данных в List
        ListView userList = getListView();
        String[] headers = new String[] {ATTRIBUTE_NAME_car, ATTRIBUTE_NAME_driver, ATTRIBUTE_NAME_id};
        Cursor userCursor = db.getDocsByDate_Cursor(String.valueOf(currentDate),String.valueOf(currentDate + 24*60*60));
        userAdapter = new SimpleCursorAdapter(this, R.layout.item,
                userCursor, headers, new int[]{R.id.tvCar, R.id.tvDriver, R.id.tvInvoiceId}, 0);
        userList.setAdapter(userAdapter);
    }

    // установка начальных даты и времени
    private void setInitialDateTime() {
        currentDateTime.setText(DateUtils.formatDateTime(this,
                dateAndTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
        dateAndTime.set(Calendar.HOUR_OF_DAY, 0);
        dateAndTime.set(Calendar.MINUTE, 0);
        dateAndTime.set(Calendar.SECOND, 0);
        dateAndTime.set(Calendar.MILLISECOND, 0);
        currentDate=dateAndTime.getTimeInMillis();
        System.out.println(currentDate);
    }

    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setInitialDateTime();
            Cursor oldCursor = userAdapter.swapCursor(db.getDocsByDate_Cursor(String.valueOf(currentDate),String.valueOf(currentDate + 24*60*60*1000-1)));
            if (oldCursor != null) oldCursor.close();
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
            //clear all ref tables
            db.clearAllRefs();
            request = new HTTPWorking().callWebService(server_Ip + "/agro_ves/hs/Grain/ref/GetRef?login=" + lName + "&password=" + lpass + "", "GET");
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

    protected void getDocs(String deviceId){
        request = new HTTPWorking().callWebService(server_Ip + "/agro_ves/hs/Grain/GetInvoices/get?&emei=" + deviceId + "", "GET");
        if (request.code == 200) {
            Gson gson = new Gson();
            DocsResult result = gson.fromJson(request.Body, DocsResult.class);
            for (Document doc : result.invoices) {
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
    private static final String FIELD_1C_ID         = "id_1c";
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
        String CREATE_TABLE = "create table if not exists " + TABLE_CARS   + "(" + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_1C_ID + " TEXT," + FIELD_TITLE + " TEXT, " + FIELD_DRIVER_ID + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_DRIVERS       + "(" + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_1C_ID + " TEXT," + FIELD_TITLE + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_FIELDS        + "(" + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_1C_ID + " TEXT," + FIELD_TITLE + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_WAREHOUSES    + "(" + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_1C_ID + " TEXT," + FIELD_TITLE + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_CROPS         + "(" + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_1C_ID + " TEXT," + FIELD_TITLE + " TEXT)";
        db.execSQL(CREATE_TABLE);
        CREATE_TABLE = "create table if not exists " + TABLE_DOCS          + "(" + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
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
        values.put(FIELD_DATE, document.getDate());
        values.put(FIELD_CAR_ID, document.getCar_id());
        values.put(FIELD_DRIVER_ID, document.getDriver_id());
        values.put(FIELD_CROP_ID, document.getCrop_id());
        values.put(FIELD_FIELD_ID, document.getField_id());
        values.put(FIELD_WAREHOUSE_ID, document.getWarehouse_id());
        values.put(FIELD_CREATED_BY, document.getCreated_by());

        db.insert(TABLE_DOCS, null, values);
        db.close();
    }

    public void addRef(TypicalRef ref, String table_name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FIELD_1C_ID, ref.getId_1c());
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
                cursor.getString(2),
                cursor.getLong(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getString(8));

        return document;
    }

    public Document getStringDoc(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("select\n" +
                "docs.id,\n" +
                "docs.driver_id as driver,\n" +
                "docs.date,\n" +
                "cars.title as car,\n" +
                "crops.title as crop,\n" +
                "fields.title as field,\n" +
                "warehouses.title as warehouse,\n" +
                "docs.created_by\n" +
                "from docs as docs inner join cars as cars on docs.car_id = cars.id_1c " +
                "inner join fields as fields on docs.field_id = fields.id_1c left join crops as crops on docs.crop_id = crops.id_1c " +
                "left join warehouses as  warehouses on docs.warehouse_id = warehouses.id_1c\n" +
                "where docs.id>= ?", new String[] {String.valueOf(id)}, null);

        if (cursor != null){
            cursor.moveToFirst();
        }
       // int id, String driver_id, long date, String car_id, String crop_id, String field_id, String warehouse_id, String created_by) {
            Document document = new Document(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getLong(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7));

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
                        cursor.getString(2),
                        cursor.getLong(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8));
                docsList.add(doc);
            } while (cursor.moveToNext());
        }

        return docsList;
    }

    public List<TypicalRef> getRefElements(String table_name) {
        List<TypicalRef> refList = new ArrayList<TypicalRef>();
        String selectQuery = "SELECT  * FROM " + table_name;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        RefType curr_type = RefType.car;
        if(table_name==TABLE_CARS) curr_type = RefType.car;
        if(table_name==TABLE_CROPS) curr_type = RefType.crop;
        if(table_name==TABLE_DRIVERS) curr_type = RefType.driver;
        if(table_name==TABLE_FIELDS) curr_type = RefType.field;
        if(table_name==TABLE_WAREHOUSES) curr_type = RefType.warehouse;

        if (cursor.moveToFirst()) {
            do {
                TypicalRef ref = new TypicalRef(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        curr_type);
                refList.add(ref);
            } while (cursor.moveToNext());
        }

        return refList;
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
                        cursor.getString(2),
                        cursor.getLong(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8));
                docsList.add(doc);
            } while (cursor.moveToNext());
        }

        return docsList;
    }

    public Cursor getDocsByDate_Cursor(String start_date, String finish_date) {
        List<Document> docsList = new ArrayList<Document>();
        String selectQuery = "select\n" +
                "docs.id AS _id,\n" +
                "docs.driver_id AS driver,\n" +
                "cars.title AS car\n" +
                "from\n" +
                "docs inner join cars ON\n" +
                "cars.id_1c = docs.car_id\n" +
                "where docs.date>= ? and docs.date<= ?";
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery(selectQuery, new String[] {start_date, finish_date});
    }

    public void clearAllRefs(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CARS, null, null);
        db.delete(TABLE_CROPS, null, null);
        db.delete(TABLE_DRIVERS, null, null);
        db.delete(TABLE_FIELDS, null, null);
        db.delete(TABLE_WAREHOUSES, null, null);
    }

    public void clearDocs(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DOCS, null, null);
    }
}

class Document {

    int id;
//    String title;
    String driver_id;
    long date;
    String car_id;
    String crop_id;
    String field_id;
    String warehouse_id;
    String created_by;

    public void setId(int id) {
        this.id = id;
    }

//    public void setTitle(String title) {
//        this.title = title;
//    }

    public void setdriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setCar_id(String car_id) {
        this.car_id = car_id;
    }

    public void setCrop_id(String crop_id) {
        this.crop_id = crop_id;
    }

    public void setField_id(String field_id) {
        this.field_id = field_id;
    }

    public void setWarehouse_id(String warehouse_id) {
        this.warehouse_id = warehouse_id;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public int getId() {
        return id;
    }

//    public String getTitle() {
//        return title;
//    }

    public String getDriver_id() {
        return driver_id;
    }

    public long getDate() {
        return date;
    }

    public String getCar_id() {
        return car_id;
    }

    public String getCrop_id() {
        return crop_id;
    }

    public String getField_id() {
        return field_id;
    }

    public String getWarehouse_id() {
        return warehouse_id;
    }

    public String getCreated_by() {
        return created_by;
    }

    public Document(int id, String driver_id, long date, String car_id, String crop_id, String field_id, String warehouse_id, String created_by) {
        this.id = id;
//        this.title = title;
        this.driver_id = driver_id;
        this.date = date;
        this.car_id = car_id;
        this.crop_id = crop_id;
        this.field_id = field_id;
        this.warehouse_id = warehouse_id;
        this.created_by = created_by;
    }
    public Document(int id, long date) {
        this.id = id;
        this.date = date;
    }

    public Document() {
    }
}

enum RefType{car, driver, field, warehouse, crop}

class TypicalRef{
    String id_1c;
    String title;
    String driver_id;
    RefType type;

    public TypicalRef(String id_1c, String title, String driver_id, RefType type) {
        this.id_1c = id_1c;
        this.title = title;
        this.driver_id = driver_id;
        this.type = type;
    }

    public String getId_1c() {
        return id_1c;
    }

    public void setId_1c(String id_1c) {
        this.id_1c = id_1c;
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
    List<Document> invoices;
}