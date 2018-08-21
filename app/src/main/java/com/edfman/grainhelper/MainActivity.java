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
        currentDate=dateAndTime.getTimeInMillis()/1000;
        System.out.println(currentDate);
    }

    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener d=new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setInitialDateTime();
            Cursor oldCursor = userAdapter.swapCursor(db.getDocsByDate_Cursor(String.valueOf(currentDate),String.valueOf(currentDate + 24*60*60-1)));
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

class Result{
    List<TypicalRef> result_list;
}

class DocsResult{
    List<Document> invoices;
}